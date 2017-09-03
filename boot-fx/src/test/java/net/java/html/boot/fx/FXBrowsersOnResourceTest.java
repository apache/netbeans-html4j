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
package net.java.html.boot.fx;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class FXBrowsersOnResourceTest {
    
    public FXBrowsersOnResourceTest() {
    }
    
    @BeforeClass public void initFX() throws Throwable {
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

    @Test
    public void behaviorOfTwoWebViewsAtOnce() throws Throwable {
        class R implements Runnable {
            CountDownLatch DONE = new CountDownLatch(1);
            Throwable t;

            @Override
            public void run() {
                try {
                    doTest();
                } catch (Throwable ex) {
                    t = ex;
                } finally {
                    DONE.countDown();
                }
            }
            
            private void doTest() throws Throwable {
                URL u = FXBrowsersOnResourceTest.class.getResource("/org/netbeans/html/boot/fx/empty.html");
                assertNotNull(u, "URL found");
                FXBrowsers.load(App.getV1(), u, OnPages.class, "first");
                
            }
        }
        R run = new R();
        Platform.runLater(run);
        run.DONE.await();
        for (int i = 0; i < 100; i++) {
            if (run.t != null) {
                throw run.t;
            }
            if (System.getProperty("finalSecond") == null) {
                Thread.sleep(100);
            }
        }
        
        
        
        assertEquals(Integer.getInteger("finalFirst"), Integer.valueOf(3), "Three times in view one");
        assertEquals(Integer.getInteger("finalSecond"), Integer.valueOf(2), "Two times in view one");
    }

    @JavaScriptResource("wnd.js")
    public static class OnPages {
        static Class<?> first;
        static Object firstWindow;
        
        public static void first() {
            first = OnPages.class;
            firstWindow = window();
            assertNotNull(firstWindow, "First window found");
            
            assertEquals(increment(), 1, "Now it is one");
            
            URL u = FXBrowsersOnResourceTest.class.getResource("/org/netbeans/html/boot/fx/empty.html");
            assertNotNull(u, "URL found");
            FXBrowsers.load(App.getV2(), u, OnPages.class, "second", "Hello");
            
            assertEquals(increment(), 2, "Now it is two and not influenced by second view");
            System.setProperty("finalFirst", "" + increment());
        }
        
        public static void second(String... args) {
            assertEquals(args.length, 1, "One string argument");
            assertEquals(args[0], "Hello", "It is hello");
            assertEquals(first, OnPages.class, "Both views share the same classloader");
            
            Object window = window();
            assertNotNull(window, "Some window found");
            assertNotNull(firstWindow, "First window is known");
            assertNotSame(firstWindow, window, "The window objects should be different");
            
            assertEquals(increment(), 1, "Counting starts from zero");
            System.setProperty("finalSecond", "" + increment());
        }
        
        @JavaScriptBody(args = {}, body = "return wnd;")
        private static native Object window();
        
        @JavaScriptBody(args = {}, body = ""
            + "if (wnd.cnt) return ++wnd.cnt;"
            + "return wnd.cnt = 1;"
        )
        private static native int increment();
    }
    
    public static class App extends Application {
        static final CountDownLatch CDL = new CountDownLatch(1);
        private static BorderPane pane;

        /**
         * @return the v1
         */
        static WebView getV1() {
            return (WebView)System.getProperties().get("v1");
        }

        /**
         * @return the v2
         */
        static WebView getV2() {
            return (WebView)System.getProperties().get("v2");
        }

        @Override
        public void start(Stage stage) {
            pane= new BorderPane();
            Scene scene = new Scene(pane, 800, 600);
            stage.setScene(scene);
            
            System.getProperties().put("v1", new WebView());
            System.getProperties().put("v2", new WebView());

            pane.setCenter(getV1());
            pane.setBottom(getV2());

            CDL.countDown();
        }
        
        
    }
}
