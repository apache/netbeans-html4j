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

import org.apidesign.html.json.spi.PropertyBinding;
import net.java.html.BrwsrCtx;
import org.apidesign.html.json.impl.PropertyBindingAccessor.FBData;
import org.apidesign.html.json.impl.PropertyBindingAccessor.PBData;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.Technology;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bindings<Data> {
    private Data data;
    private final Technology<Data> bp;

    private Bindings(Technology<Data> bp) {
        this.bp = bp;
    }
    
    public <M> PropertyBinding registerProperty(String propName, M model, SetAndGet<M> access, boolean readOnly) {
        return PropertyBindingAccessor.create(new PBData<M>(this, propName, model, access, readOnly));
    }

    public <M> FunctionBinding registerFunction(String name, M model, Callback<M> access) {
        return PropertyBindingAccessor.createFunction(new FBData<M>(name, model, access));
    }
    
    public static Bindings<?> apply(BrwsrCtx c, Object model) {
        Technology<?> bp = JSON.findTechnology(c);
        return apply(bp);
    }
    
    private static <Data> Bindings<Data> apply(Technology<Data> bp) {
        return new Bindings<Data>(bp);
    }
    
    public final void finish(Object model, PropertyBinding[] propArr, FunctionBinding[] funcArr) {
        assert data == null;
        if (bp instanceof Technology.BatchInit) {
            Technology.BatchInit<Data> bi = (Technology.BatchInit<Data>)bp;
            data = bi.wrapModel(model, propArr, funcArr);
        } else {
            data = bp.wrapModel(model);
            for (PropertyBinding b : propArr) {
                bp.bind(b, model, data);
            }
            for (FunctionBinding b : funcArr) {
                bp.expose(b, model, data);
            }
        }
    }
    
    
    public Data koData() {
        return data;
    }

    public void valueHasMutated(String firstName) {
        bp.valueHasMutated(data, firstName);
    }
    
    public void applyBindings() {
        bp.applyBindings(data);
    }

    Object wrapArray(Object[] arr) {
        return bp.wrapArray(arr);
    }
}
