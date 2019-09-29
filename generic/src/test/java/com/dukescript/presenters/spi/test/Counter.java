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
package com.dukescript.presenters.spi.test;

import net.java.html.js.JavaScriptBody;

public final class Counter {
    static int calls;
    static int callbacks;

    static int count() {
        return ++callbacks;
    }
    
    public static final void registerCounter() {
        if (rCounter()) {
            callbacks = 0;
        }
    }

    @JavaScriptBody(args = {}, javacall = true, body
            = "if (!this.counter) {\n"
            + "  this.counter = function() { return @com.dukescript.presenters.spi.test.Counter::count()(); };\n"
            + "  return true;\n"
            + "} else {\n"
            + "  return false;\n"
            + "}\n"
    )
    private static native boolean rCounter();
    
}
