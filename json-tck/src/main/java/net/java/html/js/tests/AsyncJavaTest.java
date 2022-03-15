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
import org.netbeans.html.json.tck.KOTest;

public class AsyncJavaTest {
    private Object objWithX;
    @KOTest
    public void dontWaitForJavaFactorial() throws InterruptedException {
        if (objWithX == null) {
            objWithX = AsyncJava.computeInAsyncJava(5, (n) -> {
                return new Factorial().factorial(n);
            }, () -> {});
            int initialValue = Bodies.readIntX(objWithX);
            assertEquals(-1, initialValue, "Java code shall only be called when the JavaScript ends");
        }

        int result = Bodies.readIntX(objWithX);
        if (result < 0) {
            throw new InterruptedException();
        }
        assertEquals(result, 120);
    }
}
