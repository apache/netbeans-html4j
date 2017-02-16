/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
