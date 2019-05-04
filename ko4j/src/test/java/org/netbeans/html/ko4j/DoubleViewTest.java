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

import java.awt.FlowLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import net.java.html.boot.fx.FXBrowsers;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.junit.NbTestCase;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Model(className = "DoubleView", targetId = "", properties = {
    @Property(name = "message", type = String.class)
})
public class DoubleViewTest {
    private static String set;

    @Function
    static void change(DoubleView model) {
        log("changing ").append(model).append(" to ").append(set);
        assertNotNull(set);
        model.setMessage(set);
        log("changing done");
        set = null;
    }

    DoubleView doubleView;
    private WebView view1;
    private WebView view2;

    private static final StringBuffer LOG = new StringBuffer();
    private JFrame frame;
    private Fn.Presenter presenter1;
    private Fn.Presenter presenter2;

    @BeforeMethod
    public void initializeViews() throws Exception {
        LOG.setLength(0);

        final JFXPanel panel = new JFXPanel();
        final JFXPanel p2 = new JFXPanel();

        final CountDownLatch initViews = new CountDownLatch(1);
        Platform.runLater(() -> {
            displayFrame(panel, p2);
            initViews.countDown();
        });
        initViews.await();

        doubleView = new DoubleView();
        doubleView.setMessage("Initialized");

        final URL page = DoubleViewTest.class.getResource("double.html");
        assertNotNull(page, "double.html found");



        final CountDownLatch view1Init = new CountDownLatch(1);
        final CountDownLatch view2Init = new CountDownLatch(1);
        Platform.runLater(() -> {
            FXBrowsers.load(view1, page, () -> {
                presenter1 = Fn.activePresenter();
                doubleView.applyBindings();
                log("applyBindings view One");
                view1Init.countDown();
            });

            FXBrowsers.load(view2, page, () -> {
                presenter2 = Fn.activePresenter();
                doubleView.applyBindings();
                log("applyBindings view Two");
                view2Init.countDown();
            });
        });
        view1Init.await();
        view2Init.await();
        log("initializeViews - done");
        assertNotNull(presenter1, "presenter for view1 found");
        assertNotNull(presenter2, "presenter for view2 found");
    }

    private void displayFrame(JFXPanel panel, JFXPanel p2) {
        view1 = displayWebView(panel);
        view2 = displayWebView(p2);

        frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(panel);
        frame.getContentPane().add(p2);
        frame.pack();
        frame.setVisible(true);
    }

    private WebView displayWebView(JFXPanel panel) {
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, 800, 600);
        WebView webView = new WebView();
        pane.setCenter(webView);
        panel.setScene(scene);
        return webView;
    }

    @Test
    public void synchronizationOfViews() throws Throwable {
        final CountDownLatch cdl = new CountDownLatch(1);
        final Throwable[] arr = new Throwable[1];
        Platform.runLater(() -> {
            try {
                assertMessages("In view one", view1, "Initialized");
                assertMessages("In view two", view2, "Initialized");
                set = "Change1";
                clickButton("View one", view1);
                assertMessages("In view one", view1, "Change1");
                assertMessages("In view two", view2, "Change1");
                set = "Change2";
                clickButton("View two", view2);
                assertMessages("In view one", view1, "Change2");
                assertMessages("In view two", view2, "Change2");
            } catch (Throwable t) {
                arr[0] = t;
            } finally {
                cdl.countDown();
            }
        });
        cdl.await();
        if (arr[0] != null) {
            LOG.insert(0, arr[0].getMessage() + "\n\n");
            throw new AssertionError(LOG.toString(), arr[0]);
        }
    }

    @AfterMethod
    public void waitABit() throws Exception {
        final CountDownLatch cdl = new CountDownLatch(1);
        Platform.runLater(() -> {
            Parent p1 = view1.getParent();
            ((BorderPane)p1).setCenter(new Label("Searching for GC root"));
            Parent p2 = view2.getParent();
            ((BorderPane)p2).setCenter(new Label("Searching for GC root"));
            cdl.countDown();
        });
        cdl.await();

        assertGCPresenters();
        assertGCViews();

        Platform.runLater(() -> {
            Platform.setImplicitExit(false);
            frame.dispose();
        });
    }

    private void assertGCPresenters() {
        Reference<?> r1 = new WeakReference<>(presenter1);
        Reference<?> r2 = new WeakReference<>(presenter2);

        presenter1 = null;
        presenter2 = null;

        NbTestCase.assertGC("Clearing reference 1", r1);
        NbTestCase.assertGC("Clearing reference 2", r2);
    }

    private void assertGCViews() {
        try {
            Class.forName("java.lang.Module");
            // skip the test on JDK11 and more
            return;
        } catch (ClassNotFoundException ex) {
            // OK on JDK8
        }

        Reference<?> r1 = new WeakReference<>(view1);
        Reference<?> r2 = new WeakReference<>(view2);

        view1 = null;
        view2 = null;

        NbTestCase.assertGC("Clearing reference 1", r1);
        NbTestCase.assertGC("Clearing reference 2", r2);
    }

    private void assertMessages(String msg, WebView v, String expected) {
        Object func = v.getEngine().executeScript("document.getElementById('function').innerHTML");
        final String functionMsg = msg + " 'function' check";
        log(functionMsg).append(" got: ").append(func);
        assertEquals(func, expected, functionMsg);

        Object prop = v.getEngine().executeScript("document.getElementById('property').innerHTML");
        final String propertyMsg = msg + " 'property' check";
        log(propertyMsg).append(" got: ").append(prop);
        assertEquals(prop, expected, propertyMsg);
    }

    private void clickButton(String id, WebView v) {
        log("clickButton in ").append(id);
        v.getEngine().executeScript("document.getElementById('change').click()");
        log("clickButton finished in ").append(id);
    }

    private static StringBuffer log(String msg) {
        LOG.append("\n[").append(Thread.currentThread().getName()).append("] ").append(msg);
        return LOG;
    }
}
