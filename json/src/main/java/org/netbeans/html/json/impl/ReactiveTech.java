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
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Technology;

final class ReactiveTech implements Technology<Reactive> {
    static final BrwsrCtx CTX = Contexts.newBuilder().register(Technology.class, new ReactiveTech(), 10).build();

    private ReactiveTech() {
    }

    @Override
    public Reactive wrapModel(Object model) {
        return (Reactive) model;
    }

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        return modelClass.cast(data);
    }

    @Override
    public void bind(PropertyBinding b, Object model, Reactive data) {
    }

    @Override
    public void valueHasMutated(Reactive data, String propertyName) {
        data.valueHasMutated(propertyName);
    }

    @Override
    public void expose(FunctionBinding fb, Object model, Reactive d) {
    }

    @Override
    public void applyBindings(Reactive data) {
    }

    @Override
    public Object wrapArray(Object[] arr) {
        return arr;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void runSafe(Runnable r) {
    }
}
