/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.context.spi;

import net.java.html.BrwsrCtx;
import org.apidesign.html.context.impl.CtxImpl;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public final class Contexts {
    private Contexts() {
    }

    /** Creates new, empty builder for creation of {@link BrwsrCtx}. At the
     * end call the {@link #build()} method to generate the context.
     *
     * @return new instance of the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Seeks for the specified technology in the provided context.
     * 
     * @param <Tech> type of the technology
     * @param context the context to seek in 
     *    (previously filled with ({@link Builder#register(java.lang.Class, java.lang.Object, int)})
     * @param technology class that identifies the technology
     * @return 
     */
    public static <Tech> Tech find(BrwsrCtx context, Class<Tech> technology) {
        return CtxImpl.find(context, technology);
    }

    /** Implementors of various HTML technologies should
     * register their implementation via {@link ServiceProvider} so
     * {@link ServiceProvider} can find them, when their JARs are included
     * on the classpath of the running application.
     *
     * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
     * @author Jaroslav Tulach <jtulach@netbeans.org>
     */
    public static final class Builder {
        private final CtxImpl impl = new CtxImpl();

        Builder() {
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
