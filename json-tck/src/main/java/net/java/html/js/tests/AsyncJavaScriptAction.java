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

import java.util.List;
import java.util.function.Function;
import net.java.html.js.JavaScriptBody;
import static net.java.html.js.tests.JavaScriptBodyTest.assertEquals;

final class AsyncJavaScriptAction {
    private List<Integer> collected = new java.util.ArrayList<>();
    private boolean successStoringLater;

    @JavaScriptBody(args = { "n" }, javacall = true, body = """
        return this.@net.java.html.js.tests.AsyncJavaScriptAction::performIteration(I)(n);
    """)
    private native int enterJavaScriptAndPerformIteration(int n);

    @JavaScriptBody(args = {}, javacall = true, body = """
        var self = this;
        var global = (0 || eval)("this");
        global.storeLater = function(s) {
            self.@net.java.html.js.tests.AsyncJavaScriptAction::storeLater(I)(s);
        };
    """)
    private native void defineStore();

    @JavaScriptBody(args = { "store" }, javacall = true, wait4js = false, body = """
        storeLater(store);
    """)
    private static native void jsStore(int store);

    void storeLater(int value) {
        collected.add(value);
    }

    int performIteration(int from) {
        for (int i = 0; i < 5; i++) {
            jsStore(from++);
        }
        final String n = "" + from++;
        successStoringLater = JsUtils.executeNow(AsyncJavaScriptAction.class, "storeLater(" + n + ");");
        for (int i = 6; i < 11; i++) {
            jsStore(from++);
        }
        return from;
    }

    private void performTheTest(Function<Integer,Integer> iteration) {
        defineStore();
        assertEquals(iteration.apply(0), 11);
        if (!successStoringLater) {
            return;
        }
        assertEquals(collected.size(), 11, "11 items: " + collected);
        for (int i = 0; i < 11; i++) {
            assertEquals(collected.get(i).intValue(), i, i + "th out of order: " + collected);
        }
        assertEquals(iteration.apply(11), 22);
        if (!successStoringLater) {
            return;
        }
        assertEquals(collected.size(), 22, "22 items: " + collected);
        for (int i = 0; i < 22; i++) {
            assertEquals(collected.get(i).intValue(), i, i + "th out of order: " + collected);
        }
    }

    public void testWithCallback() {
        performTheTest(this::enterJavaScriptAndPerformIteration);
    }

    public void testWithoutCallback() {
        performTheTest(this::performIteration);
    }

}
