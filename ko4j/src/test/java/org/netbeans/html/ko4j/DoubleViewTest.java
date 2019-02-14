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
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import net.java.html.boot.fx.FXBrowsers;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;
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
        assertNotNull(set);
        model.setMessage(set);
        set = null;
    }

    DoubleView doubleView;
    private WebView view1;
    private WebView view2;

    @BeforeMethod
    public void initializeViews() throws Exception {
        final JFXPanel panel = new JFXPanel();
        final JFXPanel p2 = new JFXPanel();

        final CountDownLatch initViews = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                displayFrame(panel, p2);
                initViews.countDown();
            }
        });
        initViews.await();

        doubleView = new DoubleView();
        doubleView.setMessage("Initialized");

        final URL page = DoubleViewTest.class.getResource("double.html");
        assertNotNull(page, "double.html found");



        final CountDownLatch view1Init = new CountDownLatch(1);
        final CountDownLatch view2Init = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FXBrowsers.load(view1, page, new Runnable() {
                    @Override
                    public void run() {
                        doubleView.applyBindings();
                        view1Init.countDown();
                    }
                });

                FXBrowsers.load(view2, page, new Runnable() {
                    @Override
                    public void run() {
                        doubleView.applyBindings();
                        view2Init.countDown();
                    }
                });
            }
        });
        view1Init.await();
        view2Init.await();
    }

    private void displayFrame(JFXPanel panel, JFXPanel p2) {
        view1 = displayWebView(panel);
        view2 = displayWebView(p2);

        JFrame f = new JFrame();
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(panel);
        f.getContentPane().add(p2);
        f.pack();
        f.setVisible(true);
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    assertMessages("In view one", view1, "Initialized");
                    assertMessages("In view two", view2, "Initialized");
                    set = "Change1";
                    clickButton(view1);
                    assertMessages("In view one", view1, "Change1");
                    assertMessages("In view two", view2, "Change1");
                    set = "Change2";
                    clickButton(view2);
                    assertMessages("In view one", view1, "Change2");
                    assertMessages("In view two", view2, "Change2");
                } catch (Throwable t) {
                    arr[0] = t;
                } finally {
                    cdl.countDown();
                }
            }
        });
        cdl.await();
        if (arr[0] != null) {
            throw arr[0];
        }
    }

    @AfterMethod
    public void waitABit() throws Exception {
    }

    private void assertMessages(String msg, WebView v, String expected) {
        Object func = v.getEngine().executeScript("document.getElementById('function').innerHTML");
        assertEquals(func, expected, msg + " 'function' check");

        Object prop = v.getEngine().executeScript("document.getElementById('property').innerHTML");
        assertEquals(prop, expected, msg + " 'property' check");
    }

    private void clickButton(WebView v) {
        v.getEngine().executeScript("document.getElementById('change').click()");
    }
}
