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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.script.ScriptException;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.JavaScriptTCK;
import org.netbeans.html.presenters.spi.ProtoPresenter;
import static org.testng.Assert.assertNotNull;

final class GenericTCK extends JavaScriptTCK {
    static final GenericTCK INSTANCE = new GenericTCK();

    private final Map<ProtoPresenter, Testing> MAP = new HashMap<>();
    private GenericTCK() {
    }

    @Override
    public boolean executeNow(String script) throws Exception {
        Testing t = MAP.get(Fn.activePresenter());
        assertNotNull(t, "Testing framework found");
        if (t.sync) {
            t.eng.eval(script);
            return true;
        } else {
            CountDownLatch cdl = new CountDownLatch(1);
            Exception[] error = { null };
            boolean[] queueEntered = { false };
            t.QUEUE.execute(() -> {
                if (cdl.getCount() == 1) {
                    try {
                        queueEntered[0] = true;
                        t.eng.eval(script);
                    } catch (ScriptException ex) {
                        error[0] = ex;
                    } finally {
                        cdl.countDown();
                    }
                }
            });
            cdl.await(3, TimeUnit.SECONDS);
            if (error[0] != null) {
                throw error[0];
            }
            return queueEntered[0];
        }
    }

    public static Class[] tests() {
        return testClasses();
    }

    void register(ProtoPresenter presenter, Testing testing) {
        MAP.put(presenter, testing);
    }

}
