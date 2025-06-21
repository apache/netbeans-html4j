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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.fail;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public final class KOFx implements ITest, IHookable, Runnable {
    private static final Timer SCHEDULER = new Timer("Fx Scheduler");

    private final Fn.Presenter p;
    private final Method m;
    private Object result;
    private Object inst;

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

    @Override
    public synchronized void run() {
        boolean notify = true;
        try (var ctx = Fn.activate(p)) {
            assert ctx != null;
            if (inst == null) {
                inst = m.getDeclaringClass().getConstructor().newInstance();
            }
            result = m.invoke(inst);
            if (result == null) {
                result = this;
            }
        } catch (InvocationTargetException ex) {
            Throwable r = ex.getTargetException();
            if (r instanceof InterruptedException) {
                notify = false;
                schedule(this, 10);
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

    private static void schedule(Runnable task, long delay) {
        SCHEDULER.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(task);
            }
        }, delay);
    }

    static void assertTitle(Stage s, String expTitle, String msg) {
        for (var i = 0; i < 100; i++) {
            var title = s.getTitle();
            if (expTitle.equals(title)) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
        fail(msg + " expecting: " + expTitle + " but was: " + s.getTitle());
    }
}
