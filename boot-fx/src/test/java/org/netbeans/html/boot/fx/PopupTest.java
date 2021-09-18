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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.scene.web.WebView;
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

        final WebView[] lastWebView = { null };
        final BrowserBuilder bb = BrowserBuilder.newBrowser(new FXGCPresenter(lastWebView)).loadClass(PopupTest.class).
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

        assertNotNull(lastWebView[0], "A WebView created");
        Stage s = (Stage) lastWebView[0].getScene().getWindow();
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
