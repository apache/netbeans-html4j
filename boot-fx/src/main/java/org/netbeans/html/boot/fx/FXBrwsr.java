/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** This is an implementation class, use {@link BrowserBuilder} API. Just
 * include this JAR on classpath and the {@link BrowserBuilder} API will find
 * this implementation automatically.
 */
public class FXBrwsr extends Application {
    private static final Logger LOG = Logger.getLogger(FXBrwsr.class.getName());
    private static FXBrwsr INSTANCE;
    private static final CountDownLatch FINISHED = new CountDownLatch(1);
    private BorderPane root;

    public static synchronized WebView findWebView(final URL url, final FXPresenter onLoad) {
        if (INSTANCE == null) {
            Executors.newFixedThreadPool(1).submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        FXBrwsr.launch(FXBrwsr.class);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    } finally {
                        FINISHED.countDown();
                    }
                }
            });
        }
        while (INSTANCE == null) {
            try {
                FXBrwsr.class.wait();
            } catch (InterruptedException ex) {
                // wait more
            }
        }
        if (!Platform.isFxApplicationThread()) {
            final WebView[] arr = {null};
            final CountDownLatch waitForResult = new CountDownLatch(1);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    arr[0] = INSTANCE.newView(url, onLoad);
                    waitForResult.countDown();
                }
            });
            for (;;) {
                try {
                    waitForResult.await();
                    break;
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
            return arr[0];
        } else {
            return INSTANCE.newView(url, onLoad);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        synchronized (FXBrwsr.class) {
            INSTANCE = this;
            FXBrwsr.class.notifyAll();
        }
        BorderPane r = new BorderPane();
        Scene scene = new Scene(r, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        this.root = r;
    }

    private WebView newView(final URL url, final FXPresenter onLoad) {
        final WebView view = new WebView();
        view.setContextMenuEnabled(false);
        view.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> t) {
                final Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setTitle("Warning");
                final Button button = new Button("Close");
                final Text text = new Text(t.getData());
                VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                box.setSpacing(10);
                box.setPadding(new Insets(10));
                box.getChildren().addAll(text, button);
                dialogStage.setScene(new Scene(box));
                button.setCancelButton(true);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        dialogStage.close();
                    }
                });
                dialogStage.centerOnScreen();
                dialogStage.showAndWait();
            }
        });
        root.setCenter(view);
        final Worker<Void> w = view.getEngine().getLoadWorker();
        w.stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State newState) {
                if (newState.equals(Worker.State.SUCCEEDED)) {
                    onLoad.onPageLoad();
                }
                if (newState.equals(Worker.State.FAILED)) {
                    throw new IllegalStateException("Failed to load " + url);
                }
            }
        });
        return view;
    }

    static void waitFinished() {
        for (;;) {
            try {
                FINISHED.await();
                break;
            } catch (InterruptedException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }
    }
    
}
