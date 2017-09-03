/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.html.context.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import net.java.html.BrwsrCtx;
import org.netbeans.html.context.impl.CtxImpl;

/** Factory class to assign various technologies 
 * to a {@link BrwsrCtx browser context}. Start with {@link #newBuilder()}
 * and then assign technologies with {@link Builder#register(java.lang.Class, java.lang.Object, int)}
 * method.
 *
 * @author Jaroslav Tulach
 */
public final class Contexts {
    private Contexts() {
    }

    /** Creates new, empty builder for creation of {@link BrwsrCtx}. At the
     * end call the {@link Builder#build()} method to generate the context.
     * 
     * @param context instances of various classes or names of {@link Id technologies} 
     *    to be preferred and used in the built {@link BrwsrCtx context}.
     * @return new instance of the builder
     * @since 1.1
     */
    public static Builder newBuilder(Object... context) {
        return new Builder(context);
    }
    /** Creates new, empty builder for creation of {@link BrwsrCtx}. At the
     * end call the {@link Builder#build()} method to generate the context.
     * Simply calls {@link #newBuilder(java.lang.Object...) newBuilder(new Object[0])}.
     * 
     * @return new instance of the builder
     */
    public static Builder newBuilder() {
        return newBuilder(new Object[0]);
    }

    /** Seeks for the specified technology in the provided context.
     * 
     * @param <Tech> type of the technology
     * @param context the context to seek in 
     *    (previously filled with ({@link Builder#register(java.lang.Class, java.lang.Object, int)})
     * @param technology class that identifies the technology
     * @return the technology in the context or <code>null</code>
     */
    public static <Tech> Tech find(BrwsrCtx context, Class<Tech> technology) {
        return CtxImpl.find(context, technology);
    }

    /** Seeks {@link ServiceLoader} for all registered instances of
     * {@link Provider} and asks them to {@link Provider#fillContext(org.netbeans.html.context.spi.Contexts.Builder, java.lang.Class) fill
     * the builder}.
     * 
     * @param requestor the application class for which to find the context
     * @param cb the context builder to register technologies into
     * @return <code>true</code>, if some instances of the provider were
     *    found, <code>false</code> otherwise
     * @since 0.7.6
     */
    public static boolean fillInByProviders(Class<?> requestor, Contexts.Builder cb) {
        boolean found = false;
        ClassLoader l;
        try {
            l = requestor.getClassLoader();
        } catch (SecurityException ex) {
            l = null;
        }
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Provider cp : ServiceLoader.load(Provider.class, l)) {
            if (!classes.add(cp.getClass())) {
                continue;
            }
            cp.fillContext(cb, requestor);
            found = true;
        }
        try {
            for (Provider cp : ServiceLoader.load(Provider.class, Provider.class.getClassLoader())) {
                if (!classes.add(cp.getClass())) {
                    continue;
                }
                cp.fillContext(cb, requestor);
                found = true;
            }
        } catch (SecurityException ex) {
            if (!found) {
                throw ex;
            }
        }
        if (!found) {
            for (Provider cp : ServiceLoader.load(Provider.class)) {
                if (!classes.add(cp.getClass())) {
                    continue;
                }
                cp.fillContext(cb, requestor);
                found = true;
            }
        }
        return found;
    }
    
    /** Identifies the technologies passed to {@link Builder context builder}
     * by a name. Each implementation of a technology 
     * {@link Builder#register(java.lang.Class, java.lang.Object, int) registered into a context}
     * can be annotated with a name (or multiple names). Such implementation
     * will later be 
     * {@link Contexts#fillInByProviders(java.lang.Class, org.netbeans.html.context.spi.Contexts.Builder)  preferred during lookup}
     * if their name(s) has been requested in when 
     * {@link Contexts#newBuilder(java.lang.Object...)  creating a context}.
     * @since 1.1
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    public @interface Id {
        /** Identifier(s) for the implementation. 
         * 
         * @return one of more identifiers giving this implementation name(s)
         */
        public String[] value();
    }

    /** Implementors of various HTML technologies should
     * register their implementation via <code>org.openide.util.lookup.ServiceProvider</code>, so
     * {@link ServiceLoader} can find them, when their JARs are included
     * on the classpath of the running application.
     *
     * @author Jaroslav Tulach
     */
    public static interface Provider {

        /** Register into the context if suitable technology is
         * available for the requesting class.
         * The provider should check if its own technology is available in current
         * scope (e.g. proper JDK, proper browser, etc.). The provider
         * can also find the right context depending on requestor's classloader, etc.
         * <p>
         * Providers should use {@link Builder} to enrich appropriately
         * the context.
         *
         * @param context the context builder to fill with technologies
         * @param requestor the application class requesting access the the HTML page
         * @see BrwsrCtx#findDefault(java.lang.Class)
         */
        void fillContext(Builder context, Class<?> requestor);
    }

    /** Support for providers of new {@link BrwsrCtx}. Providers of different
     * technologies should be of particular interest in this class. End users
     * designing their application with existing technologies should rather
     * point their attention to {@link BrwsrCtx} and co.
     *
     * @author Jaroslav Tulach
     */
    public static final class Builder {
        private final CtxImpl impl;

        public Builder(Object[] context) {
            this.impl = new CtxImpl(context);
        }
        
        /** Registers new technology into the context. Each technology is
         * exactly identified by its implementation class and can be associated
         * with (positive) priority. In case of presence of multiple technologies
         * with the same class, the one with higher lower priority takes precedence.
         * @param <Tech> type of technology to register
         * @param type the real class of the technology type
         * @param impl an instance of the technology class
         * @param position the lower position (but higher than zero), the more important implementation
         *    which will be consulted sooner when seeking for a {@link Contexts#find(net.java.html.BrwsrCtx, java.lang.Class)}
         *    an implementation
         * @return this builder
         * @throws IllegalStateException if the position isn't higher than <code>0</code>
         */
        public <Tech> Builder register(Class<Tech> type, Tech impl, int position) {
            if (impl == null) {
                return this;
            }
            if (position <= 0) {
                throw new IllegalStateException();
            }
            this.impl.register(type, impl, position);
            return this;
        }

        /** Generates context based on values previously inserted into
         * this builder.
         *
         * @return new, immutable instance of {@link BrwsrCtx}
         */
        public BrwsrCtx build() {
            return impl.build();
        }
    }
}
