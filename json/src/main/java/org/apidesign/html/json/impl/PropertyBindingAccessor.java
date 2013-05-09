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
package org.apidesign.html.json.impl;

import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.PropertyBinding;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class PropertyBindingAccessor {
    private static PropertyBindingAccessor DEFAULT;

    protected PropertyBindingAccessor() {
        if (DEFAULT != null) throw new IllegalStateException();
        DEFAULT = this;
    }
    
    static {
        JSON.initClass(PropertyBinding.class);
    }

    protected abstract <M> PropertyBinding newBinding(PBData<M> d);
    protected abstract <M> FunctionBinding newFunction(FBData<M> d);
    protected abstract JSONCall newCall(
        Runnable whenDone, Object[] result, String urlBefore, String urlAfter, String method);

    
    static <M> PropertyBinding create(PBData<M> d) {
        return DEFAULT.newBinding(d);
    }
    static <M> FunctionBinding createFunction(FBData<M> d) {
        return DEFAULT.newFunction(d);
    }
    static JSONCall createCall(
        Runnable whenDone, Object[] result, String urlBefore, String urlAfter, String method) {
        return DEFAULT.newCall(whenDone, result, urlBefore, urlAfter, method);
    }

    public static final class PBData<M> {
        public final String name;
        public final boolean readOnly;
        private final M model;
        private final SetAndGet<M> access;

        public PBData(String name, M model, SetAndGet<M> access, boolean readOnly) {
            this.name = name;
            this.model = model;
            this.access = access;
            this.readOnly = readOnly;
        }

        public void setValue(Object v) {
            access.setValue(model, v);
        }

        public Object getValue() {
            return access.getValue(model);
        }

        public boolean isReadOnly() {
            return readOnly;
        }
    } // end of PBData
    
    public static final class FBData<M> {
        public final String name;
        private final M model;
        private final Callback<M> access;

        public FBData(String name, M model, Callback<M> access) {
            this.name = name;
            this.model = model;
            this.access = access;
        }


        public void call(Object data, Object ev) {
            access.call(model, data, ev);
        }
    } // end of FBData
}
