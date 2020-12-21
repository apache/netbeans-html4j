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
package net.java.html.js.tests;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import net.java.html.js.JavaScriptBody;

/**
 */
public final class Receiver {
    private final Object fn;
    Object value;
    final Reference<Object> ref;

    public Receiver(Object v) {
        this.fn = initFn(v);
        this.ref = new WeakReference<Object>(v);
        this.value = this;
    }
    
    public void apply() {
        fnApply(fn, this);
    }
    
    void set(Object v) {
        value = v;
    }
    
    @JavaScriptBody(args = { "v" }, keepAlive = false, javacall = true, 
        body = """
               return function(rec) {
                 rec.@net.java.html.js.tests.Receiver::set(Ljava/lang/Object;)(v);
               };
               """)
    private static native Object initFn(Object v);
    
    @JavaScriptBody(args = { "fn", "thiz" }, body =
        "fn(thiz);"
    )
    private static native void fnApply(Object fn, Receiver thiz);
}
