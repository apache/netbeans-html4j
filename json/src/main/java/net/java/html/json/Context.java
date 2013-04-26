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
package net.java.html.json;

import java.util.ServiceLoader;
import org.apidesign.html.json.impl.ContextAccessor;
import org.apidesign.html.json.spi.ContextBuilder;
import org.apidesign.html.json.spi.ContextProvider;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;

/** Represents context where the {@link Model} and other objects
 * operate in. The context is usually a particular HTML page in a browser.
 * The context is also associated with the actual HTML rendering technology
 * in the HTML page - there is likely to be different context for 
 * <a href="http://knockoutjs.com">knockout.js</a> and different one
 * for <a href="http://angularjs.org">angular</a>.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Context {
    private final Technology<?> t;
    private final Transfer r;
    
    private Context(Technology<?> t, Transfer r) {
        t.getClass();
        r.getClass();
        this.t = t;
        this.r = r;
    }
    static {
        new ContextAccessor() {
            @Override
            protected Context newContext(Technology<?> t, Transfer r) {
                return new Context(t, r);
            }
            
            @Override
            protected Technology<?> technology(Context c) {
                return c.t;
            }

            @Override
            protected Transfer transfer(Context c) {
                return c.r;
            }
        };
    }
    /** Dummy context without binding to any real browser or technology. 
     * Useful for simple unit testing of behavior of model classes.
     */
    public static final Context EMPTY = ContextBuilder.create().build();
    
    /** Seeks for the default context that is associated with the requesting
     * class. If no suitable context is found, a warning message is
     * printed and {@link #EMPTY} context is returned.
     * 
     * @param requestor the class that makes the request
     * @return appropriate context for the request
     */
    public static Context findDefault(Class<?> requestor) {
        for (ContextProvider cp : ServiceLoader.load(ContextProvider.class)) {
            Context c = cp.findContext(requestor);
            if (c != null) {
                return c;
            }
        }
        for (ContextProvider cp : ServiceLoader.load(ContextProvider.class, ContextProvider.class.getClassLoader())) {
            Context c = cp.findContext(requestor);
            if (c != null) {
                return c;
            }
        }
        // XXX: print out a warning
        return Context.EMPTY;
    }
    
}
