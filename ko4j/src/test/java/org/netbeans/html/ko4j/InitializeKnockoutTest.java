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

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.java.html.BrwsrCtx;
import net.java.html.boot.fx.FXBrowsers;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Models;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertNotNull;

/**
 *
 * @author Jaroslav Tulach
 */
public class InitializeKnockoutTest {
    public InitializeKnockoutTest() {
    }
    
    @BeforeClass
    public void initFX() throws Throwable {
        new Thread("initFX") {
            @Override
            public void run() {
                if (Platform.isFxApplicationThread()) {
                    new App().start(new Stage());
                } else {
                    try {
                        App.launch(App.class);
                    } catch (IllegalStateException ex) {
                        Platform.runLater(this);
                    }
                }
            }
        }.start();
        App.CDL.await();
    }

    @JavaScriptBody(args = {}, body = "return typeof ko !== 'undefined' ? ko : null;")
    static native Object ko();
    
    @Test
    public void brwsrCtxExecute() throws Throwable {
        final CountDownLatch init = new CountDownLatch(1);
        final BrwsrCtx[] ctx = { null };
        FXBrowsers.runInBrowser(App.webView(), new Runnable() {
            @Override
            public void run() {
                ctx[0] = BrwsrCtx.findDefault(InitializeKnockoutTest.class);
                init.countDown();
            }
        });
        init.await();

        final CountDownLatch cdl = new CountDownLatch(1);
        FXBrowsers.runInBrowser(App.webView(), new Runnable() {
            @Override
            public void run() {
                assertNull(ko(), "Knockout isn't yet defined");
                Models.toRaw(null);
                assertNotNull(ko(), "After call to toRaw, ko is defined");

                cdl.countDown();
            }
        });

        cdl.await();
    }

    public static class App extends Application {
        static final CountDownLatch CDL = new CountDownLatch(1);
        private static BorderPane pane;

        static WebView webView() {
            try {
                CDL.await();
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
            return (WebView)System.getProperties().get("v1");
        }

        @Override
        public void start(Stage stage) {
            pane= new BorderPane();
            Scene scene = new Scene(pane, 800, 600);
            stage.setScene(scene);
            final WebView w1 = new WebView();
            System.getProperties().put("v1", w1);
            pane.setCenter(w1);

            
            URL url = InitializeKnockoutTest.class.getResource("test.html");
            assertNotNull(url);
            FXBrowsers.load(w1, url, new Runnable() {
                @Override
                public void run() {
                    CDL.countDown();
                }
            });

        }
        
        
    }
}
