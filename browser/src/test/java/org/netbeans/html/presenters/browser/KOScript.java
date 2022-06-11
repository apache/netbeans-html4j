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
package org.netbeans.html.presenters.browser;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public final class KOScript implements ITest, IHookable, Runnable {
    private final Fn.Presenter p;
    private CountDownLatch finished;
    private final Method m;
    private Object result;
    private Object inst;
    private int cnt;
    private final String prefix;
    private final Fn updateName;

    KOScript(Fn updateName, String prefix, Fn.Presenter p, Method m) {
        this.updateName = updateName;
        this.prefix = prefix;
        this.p = p;
        this.m = m;
    }

    @Override
    public String getTestName() {
        return prefix + ":" + m.getDeclaringClass().getSimpleName() + "." + m.getName();
    }

    @Test(groups = "BrowserTest")
    public void executeTest() throws Exception {
        for (;;) {
            if (p instanceof Executor) {
                finished = new CountDownLatch(1);
                ((Executor)p).execute(this);
                finished.await();
            } else {
                run();
            }
            if (result instanceof InterruptedException && cnt++ < 100) {
                Thread.sleep(100);
                result = null;
                continue;
            }
            break;
        }
        if (result instanceof Exception) {
            throw (Exception) result;
        } else if (result instanceof Error) {
            throw (Error) result;
        }
    }
    
    @Override
    public void run() {
        Closeable c = Fn.activate(p);
        try {
            if (updateName != null) {
                updateName.invoke(null, getTestName(), cnt);
            }
            if (inst == null) {
                inst = m.getDeclaringClass().newInstance();
            }
            result = m.invoke(inst);
            if (result == null) {
                result = this;
            }
            if (updateName != null) {
                updateName.invoke(null, getTestName(), "Done");
            }
        } catch (InvocationTargetException ex) {
            Throwable r = ex.getTargetException();
            result = r;
        } catch (Exception ex) {
            result = ex;
        } finally {
            try {
                c.close();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            if (finished != null) {
                finished.countDown();
            }
        }
    }

    @Override
    public void run(IHookCallBack ihcb, ITestResult itr) {
        ihcb.runTestMethod(itr);
    }
    
}
