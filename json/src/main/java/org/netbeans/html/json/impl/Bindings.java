/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.html.json.impl;

import net.java.html.BrwsrCtx;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Proto;
import org.netbeans.html.json.spi.Technology;

/**
 *
 * @author Jaroslav Tulach
 */
public final class Bindings<Data> {
    private Data data;
    private final Technology<Data> bp;

    private Bindings(Technology<Data> bp) {
        this.bp = bp;
    }
    
    public <M> PropertyBinding registerProperty(String propName, int index, M model, Proto.Type<M> access, byte propertyType) {
        return PropertyBindingAccessor.create(access, this, propName, index, model, propertyType);
    }

    public static Bindings<?> apply(BrwsrCtx c) {
        Technology<?> bp = JSON.findTechnology(c);
        return apply(bp);
    }
    
    private static <Data> Bindings<Data> apply(Technology<Data> bp) {
        return new Bindings<Data>(bp);
    }
    
    public final void finish(Object model, Object copyFrom, PropertyBinding[] propArr, FunctionBinding[] funcArr) {
        assert data == null;
        if (bp instanceof Technology.BatchCopy) {
            Technology.BatchCopy<Data> bi = (Technology.BatchCopy<Data>)bp;
            data = bi.wrapModel(model, copyFrom, propArr, funcArr);
        } else if (bp instanceof Technology.BatchInit) {
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
    
    
    final Object jsObj() {
        if (bp instanceof Technology.ToJavaScript) {
            Technology.ToJavaScript<Data> toJS = (Technology.ToJavaScript<Data>) bp;
            return toJS.toJavaScript(data);
        }
        return data;
    }

    public void valueHasMutated(String firstName, Object oldValue, Object newValue) {
        if (bp instanceof Technology.ValueMutated) {
            Technology.ValueMutated<Data> vm = (Technology.ValueMutated<Data>)bp;
            Object ov = JSON.find(oldValue, this);
            Object nv = JSON.find(newValue, this);
            vm.valueHasMutated(data, firstName, ov, nv);
        } else {
            bp.valueHasMutated(data, firstName);
        }
    }
    
    public void applyBindings(String id) {
        if (bp instanceof Technology.ApplyId) {
            Technology.ApplyId<Data> ai = (Technology.ApplyId<Data>) bp;
            ai.applyBindings(id, data);
            return;
        }
        bp.applyBindings(data);
    }

    Object wrapArray(Object[] arr) {
        return bp.wrapArray(arr);
    }
}
