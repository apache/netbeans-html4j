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
package net.java.html.boot.script;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

final class Sanitizer {
    private Sanitizer() {
    }

    private static final String[] ALLOWED_GLOBALS = (""
    + "Object,Function,Array,String,Date,Number,BigInt,"
    + "Boolean,RegExp,Math,JSON,NaN,Infinity,undefined,"
    + "isNaN,isFinite,parseFloat,parseInt,encodeURI,"
    + "encodeURIComponent,decodeURI,decodeURIComponent,eval,"
    + "escape,unescape,"
    + "Error,EvalError,RangeError,ReferenceError,SyntaxError,"
    + "TypeError,URIError,ArrayBuffer,Int8Array,Uint8Array,"
    + "Uint8ClampedArray,Int16Array,Uint16Array,Int32Array,"
    + "Uint32Array,Float32Array,Float64Array,BigInt64Array,"
    + "BigUint64Array,DataView,Map,Set,WeakMap,"
    + "WeakSet,Symbol,Reflect,Proxy,Promise,SharedArrayBuffer,"
    + "Atomics,console,performance,"
    + "arguments,load").split(",");


    static void clean(ScriptEngine engine) throws ScriptException {
        try {
            Object cleaner = engine.eval(""
                + "(function(allowed) {\n"
                + "   var names = Object.getOwnPropertyNames(this);\n"
                + "   MAIN: for (var i = 0; i < names.length; i++) {\n"
                + "     for (var j = 0; j < allowed.length; j++) {\n"
                + "       if (names[i] === allowed[j]) {\n"
                + "         continue MAIN;\n"
                + "       }\n"
                + "     }\n"
                + "     delete this[names[i]];\n"
                + "   }\n"
                + "})"
            );
            ((Invocable) engine).invokeMethod(cleaner, "call", null, ALLOWED_GLOBALS);
        } catch (NoSuchMethodException ex) {
            throw new ScriptException(ex);
        }
    }

    static void defineAlert(ScriptEngine engine) throws ScriptException {
        try {
            Object defineAlert = engine.eval(""
                + "(function(out) {\n"
                + "  this.alert = function(msg) {\n"
                + "    out.println(msg);\n"
                + " };"
                + "});"
            );
            ((Invocable) engine).invokeMethod(defineAlert, "call", null, System.out);
        } catch (NoSuchMethodException ex) {
            throw new ScriptException(ex);
        }
    }
}
