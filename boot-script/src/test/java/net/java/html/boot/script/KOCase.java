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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.netbeans.html.boot.spi.Fn;
import org.testng.ITest;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public final class KOCase implements ITest, Runnable {
    static final Executor JS = Executors.newSingleThreadExecutor();
    private final Fn.Presenter p;
    private final Method m;
    private final String skipMsg;
    private Object result;
    private Object inst;
    private int count;

    KOCase(Fn.Presenter p, Method m, String skipMsg) {
        this.p = p;
        this.m = m;
        this.skipMsg = skipMsg;
    }

    @Override
    public String getTestName() {
        return m.getName();
    }

    @Test
    public synchronized void executeTest() throws Exception {
        if (skipMsg != null) {
            throw new SkipException(skipMsg);
        }
        if (result == null) {
            JS.execute(this);
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
        Closeable a = Fn.activate(p);
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
                if (count++ < 1000) {
                    notify = false;
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException ignore) {
                        // just go on
                    }
                    JS.execute(this);
                    return;
                }
            }
            result = r;
        } catch (Exception ex) {
            result = ex;
        } finally {
            if (notify) {
                notifyAll();
            }
            try {
                a.close();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    
}
