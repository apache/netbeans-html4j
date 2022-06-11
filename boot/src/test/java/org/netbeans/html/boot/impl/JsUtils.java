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
package org.netbeans.html.boot.impl;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

final class JsUtils {
    public static ScriptEngine initializeEngine() throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        final ScriptEngine eng = sem.getEngineByMimeType("text/javascript");
        Bindings bindings = eng.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true); // NOI18N

        eng.eval("""
                 function checkArray(arr, to) {
                   if (to === null) {
                     if (Object.prototype.toString.call(arr) === '[object Array]') return arr.length;
                     else return -1;
                   } else {
                     var l = arr.length;
                     for (var i = 0; i < l; i++) {
                       to[i] = arr[i] === undefined ? null : arr[i];
                     }
                     return l;
                   }
                 }
                 """);

        return eng;
    }

    static java.lang.Object toJava(ScriptEngine eng, java.lang.Object js) {
        if (js instanceof Boolean || js instanceof String || js instanceof Number) {
            return js;
        }
        try {
            Number len = (Number) ((Invocable) eng).invokeFunction("checkArray", js, null);
            if (len != null && len.intValue() >= 0) {
                java.lang.Object[] arr = new java.lang.Object[len.intValue()];
                ((Invocable) eng).invokeFunction("checkArray", js, arr);
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = toJava(eng, arr[i]);
                }
                return arr;
            }
            return js;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

}
