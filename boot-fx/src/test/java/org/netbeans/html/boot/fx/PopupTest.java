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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.stage.Stage;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class PopupTest {
    public PopupTest() {
    }

    @JavaScriptBody(args = { "page" }, body =
        "return window.open(page, 'secondary', 'width=300,height=150');"
    )
    private static native Object openSecondaryWindow(String page);

    @Test public void checkReload() throws Throwable {
        final Throwable[] arr = { null };

        class WhenInitialized implements Runnable {
            CountDownLatch cdl = new CountDownLatch(1);
            AbstractFXPresenter p;
            BrwsrCtx ctx;

            @Override
            public void run() {
                try {
                    p = (AbstractFXPresenter) Fn.activePresenter();
                    assertNotNull(p, "Presenter is defined");
                    ctx = BrwsrCtx.findDefault(WhenInitialized.class);
                } catch (Throwable ex) {
                    arr[0] = ex;
                } finally {
                    cdl.countDown();
                }
            }
        }
        WhenInitialized when = new WhenInitialized();

        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(PopupTest.class).
                loadPage("empty.html").
                loadFinished(when);

        class ShowBrowser implements Runnable {
            @Override
            public void run() {
                bb.showAndWait();
            }
        }

        Executors.newSingleThreadExecutor().submit(new ShowBrowser());
        when.cdl.await();
        if (arr[0] != null) throw arr[0];

        Stage s = FXBrwsr.findStage();
        assertEquals(s.getTitle(), "FX Presenter Harness");

        final Object[] window = new Object[1];
        final CountDownLatch openWindow = new CountDownLatch(1);
        when.ctx.execute(new Runnable() {
            @Override
            public void run() {
                TitleTest.changeTitle("First window");
                window[0] = openSecondaryWindow("second.html");
                openWindow.countDown();
            }
        });

        openWindow.await(5, TimeUnit.SECONDS);

        assertNotNull(window[0], "Second window opened");

        assertEquals(s.getTitle(), "First window", "The title is kept");
    }
}
