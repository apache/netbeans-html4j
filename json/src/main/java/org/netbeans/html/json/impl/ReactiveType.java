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
import org.netbeans.html.json.spi.Proto;

final class ReactiveType extends Proto.Type<Reactive> {

    public static final ReactiveType TYPE = new ReactiveType();

    private ReactiveType() {
        super(Reactive.class, Reactive.class, 1, 0);
    }

    @Override
    protected void setValue(Reactive model, int index, Object value) {
    }

    @Override
    protected Object getValue(Reactive model, int index) {
        return null;
    }

    @Override
    protected void call(Reactive model, int index, Object data, Object event) throws Exception {
    }

    @Override
    protected Reactive cloneTo(Reactive model, BrwsrCtx ctx) {
        return model;
    }

    @Override
    protected Reactive read(BrwsrCtx c, Object json) {
        return null;
    }

    @Override
    protected void onChange(Reactive model, int index) {
    }

    @Override
    protected Proto protoFor(Object object) {
        return ((Reactive)object).getProto();
    }

}
