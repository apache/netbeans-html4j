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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package net.java.html.boot.fx;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.java.html.BrwsrCtx;
import net.java.html.js.JavaScriptBody;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class FXBrowsersTest {
    private static CountDownLatch PROPERTY_SET;

    public FXBrowsersTest() {
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
    
    @JavaScriptBody(args = {  }, body = "return true;")
    static boolean inJS() {
        return false;
    }

    @Test
    public void brwsrCtxExecute() throws Throwable {
        assertFalse(inJS(), "We aren't in JS now");
        final CountDownLatch init = new CountDownLatch(1);
        final BrwsrCtx[] ctx = { null };
        FXBrowsers.runInBrowser(App.getV1(), new Runnable() {
            @Override
            public void run() {
                assertTrue(inJS(), "We are in JS context now");
                ctx[0] = BrwsrCtx.findDefault(FXBrowsersTest.class);
                init.countDown();
            }
        });
        init.await();

        final CountDownLatch cdl = new CountDownLatch(1);
        class R implements Runnable {
            @Override
            public void run() {
                if (Platform.isFxApplicationThread()) {
                    assertTrue(inJS());
                    cdl.countDown();
                } else {
                    ctx[0].execute(this);
                }
            }
        }
        new Thread(new R(), "Background thread").start();

        cdl.await();
    }

    @Test
    public void behaviorOfTwoWebViewsAtOnce() throws Throwable {
        class R implements Runnable {
            Throwable t;

            @Override
            public void run() {
                try {
                    doTest();
                } catch (Throwable ex) {
                    t = ex;
                }
            }
            
            private void doTest() throws Throwable {
                URL u = FXBrowsersTest.class.getResource("/org/netbeans/html/boot/fx/empty.html");
                assertNotNull(u, "URL found");
                FXBrowsers.load(App.getV1(), u, OnPages.class, "first");
            }
        }
        R run = new R();
        PROPERTY_SET = new CountDownLatch(2);
        Platform.runLater(run);
        PROPERTY_SET.await();
        
        assertEquals(Integer.getInteger("finalFirst"), Integer.valueOf(3), "Three times in view one");
        assertEquals(Integer.getInteger("finalSecond"), Integer.valueOf(2), "Two times in view one");

        final CountDownLatch finish = new CountDownLatch(1);
        final Object[] three = { 0 };
        assertFalse(Platform.isFxApplicationThread());
        FXBrowsers.runInBrowser(App.getV1(), new Runnable() {
            @Override
            public void run() {
                assertTrue(Platform.isFxApplicationThread());
                three[0] = App.getV1().getEngine().executeScript("window.cntBrwsr");
                finish.countDown();
            }
        });
        finish.await();
        
        assertEquals(three[0], Integer.valueOf(3));
    }
    
    public static class OnPages {
        static Class<?> first;
        static Object firstWindow;
        
        public static void first() {
            first = OnPages.class;
            firstWindow = window();
            assertNotNull(firstWindow, "First window found");
            
            assertEquals(increment(), 1, "Now it is one");
            
            URL u = FXBrowsersTest.class.getResource("/org/netbeans/html/boot/fx/empty.html");
            assertNotNull(u, "URL found");
            FXBrowsers.load(App.getV2(), u, new Runnable() {
                @Override
                public void run() {
                    OnPages.second("Hello");
                }
            });
            
            assertEquals(increment(), 2, "Now it is two and not influenced by second view");
            System.setProperty("finalFirst", "" + increment());
            PROPERTY_SET.countDown();
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
            PROPERTY_SET.countDown();
        }
        
        @JavaScriptBody(args = {}, body = "return window;")
        private static native Object window();
        
        @JavaScriptBody(args = {}, body = ""
            + "if (window.cntBrwsr) return ++window.cntBrwsr;"
            + "return window.cntBrwsr = 1;"
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
