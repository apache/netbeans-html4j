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

import org.apidesign.html.json.impl.PropertyBindingAccessor;
import org.apidesign.html.json.impl.PropertyBindingAccessor.PBData;
import org.apidesign.html.json.impl.WrapperObject;

/** Describes a property when one is asked to 
 * bind it 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class PropertyBinding {
    private final PBData<?> data;
    
    private PropertyBinding(PBData<?> p) {
        this.data = p;
    }

    static {
        new PropertyBindingAccessor() {
            @Override
            protected <M> PropertyBinding newBinding(PBData<M> d) {
                return new PropertyBinding(d);
            }

            @Override
            protected <M> FunctionBinding newFunction(FBData<M> d) {
                return new FunctionBinding(d);
            }

            @Override
            protected JSONCall newCall(Runnable whenDone, Object[] result, String urlBefore, String urlAfter, String method, Object data) {
                return new JSONCall(whenDone, result, urlBefore, urlAfter, method, data);
            }
        };
    }

    public String getPropertyName() {
        return data.name;
    }

    public void setValue(Object v) {
        data.setValue(v);
    }
    
    public Object getValue() {
        Object v = data.getValue();
        Object r = WrapperObject.find(v);
        return r == null ? v : r;
    }
    
    public boolean isReadOnly() {
        return data.isReadOnly();
    }
}
