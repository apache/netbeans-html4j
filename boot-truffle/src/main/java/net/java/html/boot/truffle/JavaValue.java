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
package net.java.html.boot.truffle;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.java.JavaInterop;

abstract class JavaValue {
    public abstract Object get();

    static Object toJavaScript(Object conv, TrufflePresenter.WrapArray wrap) {
        if (conv instanceof TruffleObject) {
            return conv;
        }
        if (JavaArray.isArray(conv)) {
            conv = wrap.copy(new JavaArray(wrap, conv));
        }
        if (conv instanceof Character) {
            conv = (int) (Character) conv;
        }
        if (conv == null || conv.getClass().getName().endsWith(".$JsCallbacks$")) { // NOI18N
            conv = JavaInterop.asTruffleObject(conv);
        } else if (!isJSReady(conv)) {
            conv = new JavaObject(conv);
        }
        return conv;
    }

    private static boolean isJSReady(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return true;
        }
        if (obj instanceof Number) {
            return true;
        }
        if (obj instanceof Character) {
            return true;
        }
        if (obj instanceof Boolean) {
            return true;
        }
        if (obj instanceof TruffleObject) {
            return true;
        }
        return false;
    }

}
