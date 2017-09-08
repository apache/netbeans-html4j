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
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Proto;

/**
 *
 * @author Jaroslav Tulach
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

    protected abstract <M> PropertyBinding newBinding(
        Proto.Type<M> access, Bindings<?> bindings, String name, int index, M model, byte propertyType);
    protected abstract JSONCall newCall(
        BrwsrCtx ctx, RcvrJSON callback,
        String headers, String urlBefore, String urlAfter,
        String method, Object data
    );

    protected abstract Bindings bindings(Proto proto, boolean initialize, Object copyFrom);
    protected abstract void notifyChange(Proto proto, int propIndex);
    protected abstract Proto findProto(Proto.Type<?> type, Object object);
    protected abstract <Model> Model cloneTo(Proto.Type<Model> type, Model model, BrwsrCtx c);
    protected abstract Object read(Proto.Type<?> from, BrwsrCtx c, Object data);

    static Bindings getBindings(Proto proto, boolean initialize, Object copyFrom) {
        return DEFAULT.bindings(proto, initialize, copyFrom);
    }

    static void notifyProtoChange(Proto proto, int propIndex) {
        DEFAULT.notifyChange(proto, propIndex);
    }

    static <M> PropertyBinding create(
        Proto.Type<M> access, Bindings<?> bindings, String name, int index, M model , byte propertyType
    ) {
        return DEFAULT.newBinding(access, bindings, name, index, model, propertyType);
    }
    public static JSONCall createCall(
        BrwsrCtx ctx, RcvrJSON callback,
        String headers, String urlBefore, String urlAfter,
        String method, Object data
    ) {
        return DEFAULT.newCall(ctx, callback, headers, urlBefore, urlAfter, method, data);
    }
    static Proto protoFor(Proto.Type<?> type, Object object) {
        return DEFAULT.findProto(type, object);
    }
    static <Model> Model clone(Proto.Type<Model> type, Model model, BrwsrCtx c) {
        return DEFAULT.cloneTo(type, model, c);
    }
    static Object readFrom(Proto.Type<?> from, BrwsrCtx c, Object data) {
        return DEFAULT.read(from, c, data);
    }
}
