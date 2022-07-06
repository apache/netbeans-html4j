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
package org.netbeans.html.presenters.spi.test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.java.html.js.tests.AsyncJavaTest;
import org.netbeans.html.json.tck.KOTest;
import static org.netbeans.html.presenters.spi.test.GenericTest.createTests;
import org.testng.annotations.Factory;

public class AsyncJavaOnlyTest {
    @Factory
    public static Object[] compatibilityTests() throws Exception {
        return createTests(new PromisesOnly(), (m) -> {
            if (m.getDeclaringClass() == AsyncJavaTest.class) {
                return m.getAnnotation(KOTest.class) != null;
            }
            return false;
        });
    }

    private static final class PromisesOnly extends Testing {

        public PromisesOnly() {
            super(false, Executors.newSingleThreadExecutor((r) -> {
                return new Thread(r, "PromisesOnly Executor");
            }));
        }
        @Override
        protected String js2java(String method, Object a1, Object a2, Object a3, Object a4) throws Exception {
            switch (method) {
                case "p":
                    // promise is OK
                    break;
                case "r":
                    // result of JavaScript evaluation
                    break;
                default:
                    throw new IllegalStateException("Unexpected method " + method + "(" + a1 + ", " + a2 + ", " + a3 + ", " + a4 + "). Expecting only promises!");
            }
            return super.js2java(method, a1, a2, a3, a4);
        }
    }
}
