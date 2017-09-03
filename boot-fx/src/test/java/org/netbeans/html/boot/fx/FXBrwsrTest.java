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
package org.netbeans.html.boot.fx;

import org.sample.app.pkg.SampleApp;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class FXBrwsrTest {

    public FXBrwsrTest() {
    }

    @Test
    public void testFindCalleeClassName() throws InterruptedException {
        String callee = invokeMain();
        assertEquals(callee, SampleApp.class.getName(), "Callee is found correctly");
    }

    synchronized static String invokeMain() throws InterruptedException {
        new Thread("starting main") {
            @Override
            public void run() {
                SampleApp.main();
            }
        }.start();
        for (;;) {
            String callee = System.getProperty("callee");
            if (callee != null) {
                return callee;
            }
            FXBrwsrTest.class.wait();
        }
    }


    public static void computeCalleeClassName() {
        String name = FXBrwsr.findCalleeClassName();
        System.setProperty("callee", name);
        synchronized (FXBrwsrTest.class) {
            FXBrwsrTest.class.notifyAll();
        }
    }
}
