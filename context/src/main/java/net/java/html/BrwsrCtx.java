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
package net.java.html;

import java.util.ServiceLoader;
import org.apidesign.html.context.impl.CtxAccssr;
import org.apidesign.html.context.impl.CtxImpl;
import org.apidesign.html.context.spi.Contexts;

/** Represents context where the {@link Model} and other objects
 * operate in. The context is usually a particular HTML page in a browser.
 * The context is also associated with the actual HTML rendering technology
 * in the HTML page - there is likely to be different context for 
 * <a href="http://knockoutjs.com">knockout.js</a> and different one
 * for <a href="http://angularjs.org">angular</a>.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class BrwsrCtx {
    private final CtxImpl impl;
    private BrwsrCtx(CtxImpl impl) {
        this.impl = impl;
    }
    static {
        new CtxAccssr() {
            @Override
            protected BrwsrCtx newContext(CtxImpl impl) {
                return new BrwsrCtx(impl);
            }

            @Override
            protected CtxImpl find(BrwsrCtx context) {
                return context.impl;
            }
        };
    }
    /** Dummy context without binding to any real browser or technology. 
     * Useful for simple unit testing of behavior of various business logic
     * code.
     */
    public static final BrwsrCtx EMPTY = Contexts.newBuilder().build();
    
    /** Seeks for the default context that is associated with the requesting
     * class. If no suitable context is found, a warning message is
     * printed and {@link #EMPTY} context is returned.
     * 
     * @param requestor the class that makes the request
     * @return appropriate context for the request
     */
    public static BrwsrCtx findDefault(Class<?> requestor) {
        org.apidesign.html.context.spi.Contexts.Builder cb = Contexts.newBuilder();
        boolean found = false;
        for (org.apidesign.html.context.spi.Contexts.Provider cp : ServiceLoader.load(org.apidesign.html.context.spi.Contexts.Provider.class)) {
            cp.fillContext(cb, requestor);
            found = true;
        }
        for (org.apidesign.html.context.spi.Contexts.Provider cp : ServiceLoader.load(org.apidesign.html.context.spi.Contexts.Provider.class, org.apidesign.html.context.spi.Contexts.Provider.class.getClassLoader())) {
            cp.fillContext(cb, requestor);
            found = true;
        }
        if (!found) {
            // XXX: print out a warning
            return EMPTY;
        }
        return cb.build();
    }
    
}
