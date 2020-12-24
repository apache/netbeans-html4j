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
package org.netbeans.html.presenters.webkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.Test;

public final class Case implements ITest, IHookable, Runnable {
    private static final Timer T = new Timer("Interrupted Exception Handler");
    private final Fn.Presenter p;
    private final Method m;
    private final String skipMsg;
    private Object result;
    private Object inst;

    Case(Fn.Presenter p, Method m, String skipMsg) {
        this.p = p;
        this.m = m;
        this.skipMsg = skipMsg;
    }

    @Override
    public String getTestName() {
        return m != null ? m.getName() : skipMsg;
    }

    @Test
    public synchronized void executeTest() throws Exception {
        if (skipMsg != null) {
            throw new SkipException(skipMsg);
        }
        if (result == null) {
            Executor exec = (Executor) p;
            exec.execute(this);
            wait();
        }
        if (result instanceof Exception) {
            throw (Exception)result;
        }
        if (result instanceof Error) {
            throw (Error)result;
        }
    }

    @Override
    public synchronized void run() {
        boolean notify = true;
        try {
            if (inst == null) {
                inst = m.getDeclaringClass().newInstance();
            }
            result = m.invoke(inst);
            if (result == null) {
                result = this;
            }
        } catch (InvocationTargetException ex) {
            Throwable r = ex.getTargetException();
            if (r instanceof InterruptedException) {
                notify = false;
                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                        ((Executor)p).execute(Case.this);
                    }
                };
                T.schedule(tt, 100);
                return;
            }
            result = r;
        } catch (Exception ex) {
            result = ex;
        } finally {
            if (notify) {
                notifyAll();
            }
        }
    }

    @Override
    public void run(IHookCallBack ihcb, ITestResult itr) {
        ihcb.runTestMethod(itr);
    }
    
}
