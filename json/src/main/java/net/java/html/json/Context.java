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

import org.apidesign.html.json.impl.ContextAccessor;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;

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
    
    private Context(Technology<?> t) {
        t.getClass();
        this.t = t;
    }
    static {
        new ContextAccessor() {
            @Override
            protected Context newContext(Technology<?> t) {
                return new Context(t);
            }

            @Override
            protected Technology<?> technology(Context c) {
                return c.t;
            }
        };
    }
    /** Dummy context without binding to any real browser or technology. 
     * Useful for simple unit testing of behavior of model classes.
     */
    public static final Context EMPTY = new Context(new EmptyTech());
    
    private static final class EmptyTech implements Technology<Object> {
        @Override
        public Object wrapModel(Object model) {
            return model;
        }

        @Override
        public void valueHasMutated(Object data, String propertyName) {
        }

        @Override
        public void bind(PropertyBinding b, Object model, Object data) {
        }

        @Override
        public void expose(FunctionBinding fb, Object model, Object d) {
        }

        @Override
        public void applyBindings(Object data) {
        }

        @Override
        public Object wrapArray(Object[] arr) {
            return arr;
        }

        @Override
        public void extract(Object obj, String[] props, Object[] values) {
            for (int i = 0; i < values.length; i++) {
                values[i] = null;
            }
        }
    }
}
