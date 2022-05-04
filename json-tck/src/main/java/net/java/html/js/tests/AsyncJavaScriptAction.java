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

final class AsyncJavaScriptAction {
    private int result;

    private AsyncJavaScriptAction() {
    }

    static AsyncJavaScriptAction defineCallback() {
        AsyncJavaScriptAction action = new AsyncJavaScriptAction();
        action.defineCallbackImpl();
        return action;
    }

    @JavaScriptBody(args = {}, javacall = true, wait4java = false, body = """
        var self = this;
        var global = (0 || eval)("this");
        global.callJava = function(s) {
            self.@net.java.html.js.tests.AsyncJavaScriptAction::callJava(I)(s);
        };
    """)
    private native void defineCallbackImpl();

    void callJava(int i) {
        this.result = i;
    }

    static boolean invokeCallbackLater(int n) {
        return JsUtils.executeNow(AsyncJavaScriptAction.class, """
            if (typeof setTimeout === 'function') {
                setTimeout(function() {
                    callJava($n);
                }, 5);
            } else {
                callJava($n);
            }
        """.replaceAll("\\$n", "" + n));
    }

    int getResult() {
        return result;
    }
}