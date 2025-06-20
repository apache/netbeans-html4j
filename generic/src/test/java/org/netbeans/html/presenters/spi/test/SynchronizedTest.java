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

import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import static org.netbeans.html.presenters.spi.test.GenericTest.createTests;
import static org.netbeans.html.presenters.spi.test.Testing.LOG;
import org.testng.annotations.Factory;

public class SynchronizedTest {
    @Factory public static Object[] compatibilityTests() throws Exception {
        return createTests(new Synchronized());
    }

    private static class Synchronized extends Testing {
        public Synchronized() {
            super(true, new SynchronousExecutor());
        }

        @Override
        protected void loadJS(final String js) {
            try {
                Object res = eng.eval(js);
                LOG.log(Level.FINE, "Result: {0}", res);
            } catch (Throwable ex) {
                LOG.log(Level.SEVERE, "Can't process " + js, ex);
            }
        }

        @Override
        public void displayPage(URL url, Runnable r) {
            r.run();
        }

        @Override
        public void dispatch(Runnable r) {
            r.run();
        }

        @Override
        void beforeTest(Class<?> declaringClass) throws Exception {
        }
    } // end of Synchronized

    private static class SynchronousExecutor implements Executor {
        private LinkedList<Runnable> pending;

        SynchronousExecutor() {
        }

        @Override
        public void execute(Runnable command) {
            if (pending != null) {
                pending.add(command);
                return;
            }
            try {
                pending = new LinkedList<>();
                pending.add(command);
                for (;;) {
                    Runnable toRun = pending.pollFirst();
                    if (toRun == null) {
                        break;
                    }
                    try {
                        toRun.run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } finally {
                pending = null;
            }
        }
    }
}
