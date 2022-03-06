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

import net.java.html.js.JavaScriptBody;

public class AsyncJava {
    private AsyncJava() {
    }

    @JavaScriptBody(args = { "n", "fac", "done" }, javacall = true, wait4java = false, body = """
    let result = {
        x : -1
    };
    let facN = fac.@net.java.html.js.tests.AsyncJava.Fac::fac(I)(n);
    facN.then((res) => {
        result.x = res;
        done.@java.lang.Runnable::run()()
    });
    return result;
    """)
    static native Object computeInAsyncJava(int n, Fac fac, Runnable done);

    interface Fac {
        int fac(int n);
    }

}
