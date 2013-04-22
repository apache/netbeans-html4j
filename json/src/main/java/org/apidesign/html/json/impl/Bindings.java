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

import java.util.Arrays;
import java.util.List;
import org.apidesign.html.json.spi.PropertyBinding;
import net.java.html.json.Context;
import org.apidesign.html.json.impl.PropertyBindingAccessor.PBData;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.Technology;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bindings<Data> {
    private final Data data;
    private final Technology<Data> bp;

    private Bindings(Data data, Technology<Data> bp) {
        this.data = data;
        this.bp = bp;
    }
    
    public <M> PropertyBinding registerProperty(String propName, M model, SetAndGet<M> access, boolean readOnly) {
        PropertyBinding pb = PropertyBindingAccessor.create(new PBData<>(propName, model, access, readOnly));
        bp.bind(pb, model, data);
        return pb;
    }
    
    public static Bindings<?> apply(Context c, Object model, String[] functions) {
        Technology<?> bp = ContextAccessor.findTechnology(c);
        return apply(bp, model, null, functions);
    }
    
    private static <Data> Bindings<Data> apply(
        Technology<Data> bp, Object model, 
        PropertyBinding[] propBindings, String[] methodsAndSignatures
    ) {
        Data d = bp.wrapModel(model);
        
        if (propBindings != null) {
            for (int i = 0; i < propBindings.length; i++) {
                PropertyBinding pb = propBindings[i];
                bp.bind(pb, model, d);
            }
        }
        
        List<String> arr = Arrays.asList(methodsAndSignatures);
        for (int i = 0; i < methodsAndSignatures.length; i += 2) {
            FunctionBinding fb = PropertyBindingAccessor.createFunction(arr.subList(i, i + 2));
            bp.expose(fb, model, d);
        }
        
        return new Bindings<>(d, bp);
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
