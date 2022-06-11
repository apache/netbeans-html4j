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
package org.netbeans.html.ko4j;

import java.util.List;
import net.java.html.json.Models;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Technology;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 *
 * @author Jaroslav Tulach
 */
@Contexts.Id("ko4j")
final class KOTech
implements Technology.BatchCopy<Knockout>, Technology.ValueMutated<Knockout>,
Technology.ApplyId<Knockout>, Technology.ToJavaScript<Knockout> {
    public KOTech() {
    }
    
    @Override
    public Knockout wrapModel(Object model, Object copyFrom, PropertyBinding[] propArr, FunctionBinding[] funcArr) {
        return createKO(model, copyFrom, propArr, funcArr, null);
    }

    final Knockout createKO(Object model, Object copyFrom, PropertyBinding[] propArr, FunctionBinding[] funcArr, Knockout[] ko) {
        Knockout newKO = new Knockout(model, copyFrom, propArr, funcArr);
        if (ko != null) {
            ko[0] = newKO;
        }
        return newKO;
    }
    
    @Override
    public Knockout wrapModel(Object model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(PropertyBinding b, Object model, Knockout data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void valueHasMutated(Knockout data, String propertyName) {
        valueHasMutated(data, propertyName, null, null);
    }
    
    @Override
    public void valueHasMutated(Knockout data, String propertyName, Object oldValue, Object newValue) {
        Knockout.cleanUp();
        if (data != null) {
            if (newValue instanceof Enum) {
                newValue = newValue.toString();
            }
            data.valueHasMutated(propertyName, oldValue, newValue);
        }
    }

    @Override
    public void expose(FunctionBinding fb, Object model, Knockout data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyBindings(Knockout data) {
        applyBindings(null, data);
    }
    @Override
    public void applyBindings(String id, Knockout data) {
        Object ko = data.applyBindings(id);
        if (ko instanceof Knockout) {
            ((Knockout)ko).hold();
            applied.add((Knockout) ko);
        }
    }

    private static final List<Knockout> applied = Models.asList();

    @Override
    public Object wrapArray(Object[] arr) {
        return arr;
    }
    
    @Override
    public void runSafe(final Runnable r) {
        r.run();
    }    

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        return modelClass.cast(Knockout.toModel(data));
    }

    @Override
    public Object toJavaScript(Knockout data) {
        return data.js();
    }
}
