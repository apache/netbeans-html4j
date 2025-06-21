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
package net.java.html.boot.script;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public final class ScriptEngineCase implements ITest, IHookable {
    private final Fn.Presenter p;
    private final Method method;
    private final String prefix;

    ScriptEngineCase(String prefix, Fn.Presenter p, Method m) {
        this.prefix = prefix;
        this.p = p;
        this.method = m;
    }

    @Override
    public String getTestName() {
        return prefix + method.getName();
    }

    @Test
    public synchronized void executeTest() throws Throwable {
        skipAsyncJavaTestWhenNoPromise();
        // BEGIN: net.java.html.boot.script.ScriptEngineCase#run
        Object instance = method.getDeclaringClass().getConstructor().newInstance();
        for (int round = 0;; round++) {
            try (var ctx = Fn.activate(p)) {
                assert ctx != null;
                method.invoke(instance);
                return;
            } catch (InvocationTargetException ite) {
                final Throwable ex = ite.getTargetException();
                if (ex instanceof InterruptedException && round < 100) {
                    continue;
                }
                throw ex;
            }
        }
        // END: net.java.html.boot.script.ScriptEngineCase#run
    }

    @Override
    public void run(IHookCallBack ihcb, ITestResult itr) {
        ihcb.runTestMethod(itr);
    }

    private void skipAsyncJavaTestWhenNoPromise() throws IOException, SkipException {
        if (method.getDeclaringClass().getSimpleName().equals("AsyncJavaTest")) {
            try (var ctx = Fn.activate(p)) {
                if (!typeOfPromise().equals("function")) {
                    throw new SkipException("Skipping " + method.getName() + " no Promise found");
                }
            }
        }
    }

    @JavaScriptBody(args = {}, body = "return typeof Promise")
    private static native String typeOfPromise();
}
