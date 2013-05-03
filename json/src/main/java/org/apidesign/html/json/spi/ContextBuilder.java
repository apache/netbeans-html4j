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
package org.apidesign.html.json.spi;

import java.io.IOException;
import java.io.InputStream;
import net.java.html.json.Context;
import org.apidesign.html.json.impl.ContextAccessor;

/** Support for providers of new {@link Context}. Providers of different
 * technologies should be of particular interest in this class. End users
 * designing their application with existing technologies should rather
 * point their attention to {@link Context} and co.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ContextBuilder {
    private Technology<?> t;
    private Transfer r;
    
    private ContextBuilder() {
        EmptyTech et = new EmptyTech();
        t = et;
        r = et;
    }
    
    /** Creates new, empty builder for creation of {@link Context}. At the
     * end call the {@link #build()} method to generate the context.
     * 
     * @return new instance of the builder
     */
    public static ContextBuilder create() {
        return new ContextBuilder();
    }
    
    /** Provides technology for the context.
     * 
     * @param technology
     * @return this
     */
    public ContextBuilder withTechnology(Technology<?> technology) {
        this.t = technology;
        return this;
    }

    /** Provides transfer for the context.
     * 
     * @param transfer
     * @return this
     */
    public ContextBuilder withTransfer(Transfer transfer) {
        this.r = transfer;
        return this;
    }
    
    /** Generates context based on values previously inserted into
     * this builder.
     * 
     * @return new, immutable instance of {@link Context}
     */
    public Context build() {
        return ContextAccessor.create(t, r);
    }
    
    private static final class EmptyTech
    implements Technology<Object>, Transfer {
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

        @Override
        public void loadJSON(JSONCall call) {
            call.notifyError(new UnsupportedOperationException());
        }

        @Override
        public <M> M toModel(Class<M> modelClass, Object data) {
            return modelClass.cast(data);
        }

        @Override
        public Object toJSON(InputStream is) throws IOException {
            throw new IOException("Not supported");
        }
    }
    
}
