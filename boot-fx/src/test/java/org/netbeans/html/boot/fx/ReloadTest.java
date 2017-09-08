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
import javafx.application.Platform;
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
public class ReloadTest {
    private static Runnable whenInitialized;
    
    public ReloadTest() {
    }

    @JavaScriptBody(args = { "a", "b"  }, body = "return a + b;")
    private static native int plus(int a, int b);
    
    @Test public void checkReload() throws Throwable {
        final Throwable[] arr = { null };
        
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(ReloadTest.class).
                loadPage("empty.html").
                invoke("initialized");
        
        class ShowBrowser implements Runnable {
            @Override
            public void run() {
                bb.showAndWait();
            }
        }
        
        class WhenInitialized implements Runnable {
            CountDownLatch cdl = new CountDownLatch(1);
            AbstractFXPresenter p;
            BrwsrCtx ctx;
            
            @Override
            public void run() {
                try {
                    whenInitialized = null;
                    doCheckReload();
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
        whenInitialized = when;
        Executors.newSingleThreadExecutor().submit(new ShowBrowser());
        when.cdl.await();
        if (arr[0] != null) throw arr[0];
        
        class ReloadPage implements Runnable {
            final CountDownLatch cdl = new CountDownLatch(1);
            private final AbstractFXPresenter p;

            public ReloadPage(AbstractFXPresenter p) {
                this.p = p;
            }

            @Override
            public void run() {
                p.engine.reload();
                cdl.countDown();
            }
        }
        ReloadPage relPage = new ReloadPage(when.p);
        
        class SecondInit implements Runnable {
            CountDownLatch cdl = new CountDownLatch(1);
            
            @Override
            public void run() {
                try {
                    whenInitialized = null;
                    doCheckReload();
                } catch (Throwable ex) {
                    arr[0] = ex;
                } finally {
                    cdl.countDown();
                }
                
            }
        }
        SecondInit second = new SecondInit();
        whenInitialized = second;
        
        Platform.runLater(relPage);
        
        second.cdl.await();
        if (arr[0] != null) throw arr[0];
    }
    
    final void doCheckReload() throws Exception {
        int res = plus(30, 12);
        assertEquals(res, 42, "Meaning of world computed");
    }
    
    public static synchronized void initialized() throws Exception {
        whenInitialized.run();
    }
}
