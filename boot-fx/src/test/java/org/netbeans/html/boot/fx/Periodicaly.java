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

import java.util.Timer;
import java.util.TimerTask;
import net.java.html.BrwsrCtx;
import net.java.html.js.JavaScriptBody;

// BEGIN: org.netbeans.html.boot.fx.Periodicaly
public final class Periodicaly extends TimerTask {
    private final BrwsrCtx ctx;
    private int counter;

    private Periodicaly(BrwsrCtx ctx) {
        // remember the browser context and use it later
        this.ctx = ctx;
        this.counter = 0;
    }

    @Override
    public void run() {
        // arrives on wrong thread, needs to be re-scheduled
        ctx.execute(new Runnable() {
            @Override
            public void run() {
                codeThatNeedsToBeRunInABrowserEnvironment();
            }
        });
    }

    // called when your page is ready
    public static void onPageLoad(String... args) throws Exception {
        // the context at the time of page initialization
        BrwsrCtx initialCtx = BrwsrCtx.findDefault(Periodicaly.class);
        // the task that is associated with context
        Periodicaly task = new Periodicaly(initialCtx);
        // creates a new timer
        Timer t = new Timer("Move the box");
        // run the task every 100ms
        t.scheduleAtFixedRate(task, 0, 100);
    }

    @JavaScriptBody(args = { "a", "b" }, body = "return a + b")
    private static native int plus(int a, int b);

    void codeThatNeedsToBeRunInABrowserEnvironment() {
        // invokes JavaScript function in the browser environment
        counter = plus(counter, 1);
// FINISH: org.netbeans.html.boot.fx.Periodicaly

        synchronized (Periodicaly.class) {
            globalCounter = counter;
            Periodicaly.class.notifyAll();
        }
    }
    static int globalCounter;
    static synchronized void assertTen() throws InterruptedException {
        while (globalCounter < 10) {
            Periodicaly.class.wait();
        }
    }
}
