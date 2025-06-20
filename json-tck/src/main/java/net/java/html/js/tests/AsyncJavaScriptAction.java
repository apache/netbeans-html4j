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
    private final String name;
    private int result;

    private AsyncJavaScriptAction(String name) {
        this.name = name;
    }

    static AsyncJavaScriptAction defineCallback(boolean wait4js) {
        AsyncJavaScriptAction action;
        if (wait4js) {
            action = new AsyncJavaScriptAction("callJava");
            action.defineCallbackImpl();
        } else {
            action = new AsyncJavaScriptAction("callJavaNoWait");
            action.defineCallbackImplNoWait4js();
        }
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

    @JavaScriptBody(args = {}, javacall = true, wait4js = false, wait4java = false, body = """
        var self = this;
        var global = (0 || eval)("this");
        global.callJavaNoWait = function(s) {
            self.@net.java.html.js.tests.AsyncJavaScriptAction::callJava(I)(s);
        };
    """)
    private native void defineCallbackImplNoWait4js();

    void callJava(int i) {
        this.result = i;
    }

    boolean invokeCallbackLater(int n) {
        return JsUtils.executeNow(AsyncJavaScriptAction.class, """
            if (typeof setTimeout === 'function') {
                setTimeout(function() {
                    $name($n);
                }, 5);
            } else {
                $name($n);
            }
        """.replaceAll("\\$name", name).replaceAll("\\$n", "" + n));
    }

    int getResult() {
        return result;
    }
}