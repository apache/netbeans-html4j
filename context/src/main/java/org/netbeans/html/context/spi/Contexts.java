/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.html.context.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ServiceLoader;
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
        for (Provider cp : ServiceLoader.load(Provider.class, l)) {
            cp.fillContext(cb, requestor);
            found = true;
        }
        try {
            for (Provider cp : ServiceLoader.load(Provider.class, Provider.class.getClassLoader())) {
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
         * @param position the lower position, the more important implementation 
         *    which will be consulted sooner when seeking for a {@link Contexts#find(net.java.html.BrwsrCtx, java.lang.Class)}
         *    an implementation
         * @return this builder
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
