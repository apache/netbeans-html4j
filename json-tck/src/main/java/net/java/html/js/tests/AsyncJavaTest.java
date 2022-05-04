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
import static net.java.html.js.tests.JavaScriptBodyTest.assertEquals;
import static net.java.html.js.tests.JavaScriptBodyTest.assertFalse;
import net.java.html.json.tests.PhaseExecutor;
import org.netbeans.html.json.tck.KOTest;

public class AsyncJavaTest {
    private final PhaseExecutor[] phases = { null };

    @KOTest
    public void dontWaitForJavaFactorial() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            boolean[] javaExecuted = { false };
            Object objWithX = AsyncJava.computeInAsyncJava(5, (n) -> {
                javaExecuted[0] = true;
                return new Factorial().factorial(n);
            }, () -> {});
            int initialValue = Bodies.readIntX(objWithX);
            assertFalse(javaExecuted[0], "Java code shall only be called when the JavaScript ends");
            assertEquals(-1, initialValue, "Promise shall only be resolved 1when the JavaScript ends");
            return objWithX;
        }).then((objWithX) -> {
            int result = Bodies.readIntX(objWithX);
            assertEquals(result, 120);
        }).start();
    }

    @KOTest
    public void initializedFromJavaScript() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            return AsyncJavaScriptAction.defineCallback();
        }).then((action) -> {
            AsyncJavaScriptAction.invokeCallbackLater(33);
        }).then((action) -> {
            assertEquals(action.getResult(), 33, "Set to 33");
        }).then((action) -> {
            AsyncJavaScriptAction.invokeCallbackLater(42);
        }).then((action) -> {
            assertEquals(action.getResult(), 42, "Set to 42");
        }).start();
    }

}
