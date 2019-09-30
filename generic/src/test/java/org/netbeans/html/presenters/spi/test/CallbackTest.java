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

import java.lang.reflect.Method;
import java.util.logging.Level;
import javax.script.ScriptException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;

public class CallbackTest {
    @Factory public static Object[] deadlockTests() throws Exception {
        return GenericTest.createTests(new CBP());
    }
    
    @AfterClass public static void countCallbacks() {
        assertEquals(Counter.callbacks, Counter.calls, "Every call to loadJS is prefixed with a callback");
    }
    
    
    private static final class CBP extends Testing {

        @Override
        protected void loadJS(String js) {
            dispatch(new Runnable () {
                @Override
                public void run() {
                    try {
                        Object res = eng.eval("if (this.counter) this.counter()");
                        LOG.log(Level.FINE, "counter res: {0}", res);
                    } catch (ScriptException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            });
            super.loadJS(js);
        }

        @Override void beforeTest(Class<?> testClass) throws Exception {
            Class<?> cntr = testClass.getClassLoader().loadClass(Counter.class.getName());
            Method rc = cntr.getMethod("registerCounter");
            rc.invoke(null);
        }
        
    }
}
