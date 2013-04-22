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

import java.util.List;
import org.apidesign.html.json.spi.FunctionBinding;
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
        try {
            // run initializers
            Class.forName(PropertyBinding.class.getName(), 
                true, PropertyBinding.class.getClassLoader());
        } catch (Exception ex) {
            // OK
            throw new IllegalStateException(ex);
        }
    }

    protected abstract <M> PropertyBinding newBinding(PBData<M> d);
    protected abstract FunctionBinding newFunction(List<String> params);
    
    static <M> PropertyBinding create(PBData<M> d) {
        return DEFAULT.newBinding(d);
    }
    static FunctionBinding createFunction(List<String> subList) {
        return DEFAULT.newFunction(subList);
    }

    public static final class PBData<M> {
        public final String name;
        public final M model;
        public final SetAndGet<M> access;
        public final boolean readOnly;

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
    }
}
