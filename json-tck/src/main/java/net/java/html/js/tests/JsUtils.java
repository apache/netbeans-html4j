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

import java.io.StringReader;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.JavaScriptTCK;

public class JsUtils {
    private JsUtils() {
    }

    private static JavaScriptTCK instantiatedJsTCK;

    public static void registerTCK(JavaScriptTCK tck) {
        instantiatedJsTCK = tck;
    }

    static void execute(Class<?> clazz, String script) throws Exception {
        Fn.Presenter p = Fn.activePresenter();
        p.loadScript(new StringReader(script));
    }

    static boolean executeNow(Class<?> clazz, String script) {
        try {
            if (instantiatedJsTCK != null) {
                return instantiatedJsTCK.executeNow(script);
            } else {
                execute(clazz, script);
                return true;
            }
        } catch (Exception ex) {
            throw raise(RuntimeException.class, ex);
        }
    }

    private static <E extends Throwable> E raise(Class<E> e, Throwable t) throws E {
        throw (E)t;
    }
}
