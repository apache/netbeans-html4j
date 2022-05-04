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
package org.netbeans.html.ko4j;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javafx.application.Platform;
import org.netbeans.html.boot.spi.Fn;
import org.testng.ITest;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public final class KOFx implements ITest, Runnable {
    private final Fn.Presenter p;
    private final Method m;
    private Object result;
    private Object inst;
    private int count;

    KOFx(Fn.Presenter p, Method m) {
        this.p = p;
        this.m = m;
    }

    @Override
    public String getTestName() {
        return m.getName();
    }

    @Test
    public synchronized void executeTest() throws Exception {
        if (result == null) {
            Platform.runLater(this);
            wait();
        }
        if (result instanceof Exception) {
            throw (Exception)result;
        }
        if (result instanceof Error) {
            throw (Error)result;
        }
    }

    // BEGIN: org.netbeans.html.ko4j.KOFx
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
                if (count++ < 300) {
                    notify = false;
                    try {
                        Thread.sleep(30);
                    } catch (Exception ex1) {
                        // ignore and continue
                    }
                    Platform.runLater(this);
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
    // END: org.netbeans.html.ko4j.KOFx
    
}
