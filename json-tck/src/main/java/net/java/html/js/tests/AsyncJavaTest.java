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

import static net.java.html.js.tests.JavaScriptBodyTest.assertEquals;
import net.java.html.json.tests.PhaseExecutor;
import org.netbeans.html.json.tck.KOTest;

public class AsyncJavaTest {
    private final PhaseExecutor[] phases = { null };

    @KOTest
    public void dontWaitForJavaFactorial() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            boolean[] javaExecuted = { false };
            Object objWithX = AsyncJava.computeInAsyncJava(5, (n) -> {
                int acc = 1;
                for (int i = 1; i <= n; i++) {
                    acc *= i;
                }
                return acc;
            }, () -> {});
            int initialValue = Bodies.readIntX(objWithX);
            assertEquals(-1, initialValue, "Promise.then shall only be called when the code ends");
            return objWithX;
        }).then((objWithX) -> {
            int result = Bodies.readIntX(objWithX);
            assertEquals(result, 120);
        }).start();
    }

    @KOTest
    public void initializedFromJavaScript() throws Exception {
        initializedFromJavaScript(true);
    }

    @KOTest
    public void initializedFromJavaScriptNoWait4js() throws Exception {
        initializedFromJavaScript(false);
    }

    private void initializedFromJavaScript(boolean wait4js) throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            return AsyncJavaScriptAction.defineCallback(wait4js);
        }).then((action) -> {
            action.invokeCallbackLater(33);
        }).then((action) -> {
            final int r = action.getResult();
            assertEquals(r, 33, "Set to 33");
        }).then((action) -> {
            action.invokeCallbackLater(42);
        }).then((action) -> {
            assertEquals(action.getResult(), 42, "Set to 42");
        }).start();
    }

}
