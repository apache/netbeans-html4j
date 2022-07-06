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

public final class Case implements ITest, IHookable, Runnable {
    private final Fn.Presenter p;
    private final Testing t;
    private CountDownLatch finished;
    private final Method m;
    private Object result;
    private Object inst;
    private int cnt;

    Case(Testing t, Method m) {
        this.t = t;
        this.p = t.presenter;
        this.m = m;
    }

    @Override
    public String getTestName() {
        return t.getClass().getSimpleName() + ":" + m.getDeclaringClass().getSimpleName() + "." + m.getName();
    }

    @Test
    public void executeTest() throws Exception {
        for (;;) {
            finished = new CountDownLatch(1);
            t.CODE.execute(this);
            finished.await();
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
            if (p instanceof Testing) {
                Testing tp = (Testing) p;
                tp.beforeTest(m.getDeclaringClass());
            }
            if (inst == null) {
                inst = m.getDeclaringClass().getConstructor().newInstance();
            }
            result = m.invoke(inst);
            if (result == null) {
                result = this;
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
