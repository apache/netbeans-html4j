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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/** This is an implementation class, to implement browser builder API. Just
 * include this JAR on classpath and the browser builder API will find
 * this implementation automatically.
 */
public class FXBrwsr extends Application {
    private static final Logger LOG = Logger.getLogger(FXBrwsr.class.getName());
    private static FXBrwsr INSTANCE;
    private static final CountDownLatch FINISHED = new CountDownLatch(1);
    private BorderPane root;

    public static synchronized WebView findWebView(final URL url, final AbstractFXPresenter onLoad) {
        if (INSTANCE == null) {
            final String callee = findCalleeClassName();
            Executors.newFixedThreadPool(1).submit(new Runnable() {
                @Override
                public void run() {
                    if (!Platform.isFxApplicationThread()) {
                        try {
                            Platform.runLater(this);
                        } catch (IllegalStateException ex) {
                            try {
                                FXBrwsr.launch(FXBrwsr.class, callee);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            } finally {
                                FINISHED.countDown();
                            }
                        }
                    } else {
                        FXBrwsr brwsr = new FXBrwsr();
                        brwsr.start(new Stage(), callee);
                        INSTANCE = brwsr;
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

    static synchronized Stage findStage() throws InterruptedException {
        while (INSTANCE == null) {
            FXBrwsr.class.wait();
        }
        return INSTANCE.stage;
    }

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        start(primaryStage, this.getParameters().getRaw().get(0));
    }

    final void start(Stage primaryStage, String callee) {
        BorderPane r = new BorderPane();
        Object[] arr = findInitialSize(callee);
        Scene scene = new Scene(r, (Double)arr[2], (Double)arr[3]);
        primaryStage.setScene(scene);
        this.root = r;
        this.stage = primaryStage;
        synchronized (FXBrwsr.class) {
            INSTANCE = this;
            FXBrwsr.class.notifyAll();
        }
        primaryStage.setX((Double)arr[0]);
        primaryStage.setY((Double)arr[1]);
        if (arr[4] != null) {
            scene.getWindow().setOnCloseRequest((EventHandler<WindowEvent>) arr[4]);
        }
        if (Boolean.getBoolean("fxpresenter.headless")) {
            return;
        }
        primaryStage.show();
    }

    static String findCalleeClassName() {
        StackTraceElement[] frames = new Exception().getStackTrace();
        for (StackTraceElement e : frames) {
            String cn = e.getClassName();
            if (cn.startsWith("org.netbeans.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("net.java.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("java.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("javafx.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("com.sun.")) { // NOI18N
                continue;
            }
            return cn;
        }
        return "org.netbeans.html"; // NOI18N
    }

    private static Object[] findInitialSize(String callee) {
        final Preferences prefs = Preferences.userRoot().node(callee.replace('.', '/'));
        Rectangle2D screen = Screen.getPrimary().getBounds();
        double x = prefs.getDouble("x", screen.getWidth() * 0.05); // NOI18N
        double y = prefs.getDouble("y", screen.getHeight() * 0.05); // NOI18N
        double width = prefs.getDouble("width", screen.getWidth() * 0.9); // NOI18N
        double height = prefs.getDouble("height", screen.getHeight() * 0.9); // NOI18N

        Object[] arr = {
            x, y, width, height, null
        };

        if (!callee.equals("org.netbeans.html")) { // NOI18N
            arr[4] = new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Window window = (Window) event.getSource();
                    prefs.putDouble("x", window.getX()); // NOI18N
                    prefs.putDouble("y", window.getY()); // NOI18N
                    prefs.putDouble("width", window.getWidth()); // NOI18N
                    prefs.putDouble("height", window.getHeight()); // NOI18N
                }
            };
        }

        return arr;
    }

    private WebView newView(final URL url, final AbstractFXPresenter onLoad) {
        final WebView view = new WebView();
        view.setContextMenuEnabled(false);
        Stage newStage;
        BorderPane bp;
        if (root == null) {
            newStage = new Stage();
            newStage.initOwner(stage);
            bp = new BorderPane();
            newStage.setScene(new Scene(bp));
            newStage.show();
        } else {
            bp = root;
            newStage = stage;
            root = null;
        }

        attachHandlers(view, newStage);
        final FXConsole fxConsole = new FXConsole(view, newStage);
        bp.setCenter(view);
        final Worker<Void> w = view.getEngine().getLoadWorker();
        w.stateProperty().addListener(new ChangeListener<Worker.State>() {
            private String previous;

            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State newState) {
                if (newState.equals(Worker.State.SUCCEEDED)) {
                    if (checkValid()) {
                        fxConsole.register(view.getEngine());
                        onLoad.onPageLoad();
                    }
                }
                if (newState.equals(Worker.State.FAILED)) {
                    throw new IllegalStateException("Failed to load " + url);
                }
            }
            private boolean checkValid() {
                final String crnt = view.getEngine().getLocation();
                if (previous != null && !previous.equals(crnt)) {
                    w.stateProperty().removeListener(this);
                    return false;
                }
                previous = crnt;
                return true;
            }

        });
        fxConsole.observeWebViewTitle();
        return view;
    }

    private static void attachHandlers(final WebView view, final Stage owner) {
        view.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> t) {
                final Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(owner);
                ResourceBundle r = ResourceBundle.getBundle("org/netbeans/html/boot/fx/Bundle"); // NOI18N
                dialogStage.setTitle(r.getString("AlertTitle")); // NOI18N
                final Button button = new Button(r.getString("AlertCloseButton")); // NOI18N
                final Text text = new Text(t.getData());
                VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                box.setSpacing(10);
                box.setPadding(new Insets(10));
                box.getChildren().addAll(text, button);
                dialogStage.setScene(new Scene(box));
                button.setCancelButton(true);
                button.setOnAction(new CloseDialogHandler(dialogStage, null));
                dialogStage.centerOnScreen();
                dialogStage.showAndWait();
            }
        });
        view.getEngine().setConfirmHandler(new Callback<String, Boolean>() {
            @Override
            public Boolean call(String question) {
                final Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(owner);
                ResourceBundle r = ResourceBundle.getBundle("org/netbeans/html/boot/fx/Bundle"); // NOI18N
                dialogStage.setTitle(r.getString("ConfirmTitle")); // NOI18N
                final Button ok = new Button(r.getString("ConfirmOKButton")); // NOI18N
                final Button cancel = new Button(r.getString("ConfirmCancelButton")); // NOI18N
                final Text text = new Text(question);
                final Insets ins = new Insets(10);
                final VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                box.setSpacing(10);
                box.setPadding(ins);
                final HBox buttons = new HBox(10);
                buttons.getChildren().addAll(ok, cancel);
                buttons.setAlignment(Pos.CENTER);
                buttons.setPadding(ins);
                box.getChildren().addAll(text, buttons);
                dialogStage.setScene(new Scene(box));
                ok.setCancelButton(false);

                final boolean[] res = new boolean[1];
                ok.setOnAction(new CloseDialogHandler(dialogStage, res));
                cancel.setCancelButton(true);
                cancel.setOnAction(new CloseDialogHandler(dialogStage, null));
                dialogStage.centerOnScreen();
                dialogStage.showAndWait();
                return res[0];
            }
        });
        view.getEngine().setPromptHandler(new Callback<PromptData, String>() {
            @Override
            public String call(PromptData prompt) {
                final Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(owner);
                ResourceBundle r = ResourceBundle.getBundle("org/netbeans/html/boot/fx/Bundle"); // NOI18N
                dialogStage.setTitle(r.getString("PromptTitle")); // NOI18N
                final Button ok = new Button(r.getString("PromptOKButton")); // NOI18N
                final Button cancel = new Button(r.getString("PromptCancelButton")); // NOI18N
                final Text text = new Text(prompt.getMessage());
                final TextField line = new TextField();
                if (prompt.getDefaultValue() != null) {
                    line.setText(prompt.getDefaultValue());
                }
                final Insets ins = new Insets(10);
                final VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                box.setSpacing(10);
                box.setPadding(ins);
                final HBox buttons = new HBox(10);
                buttons.getChildren().addAll(ok, cancel);
                buttons.setAlignment(Pos.CENTER);
                buttons.setPadding(ins);
                box.getChildren().addAll(text, line, buttons);
                dialogStage.setScene(new Scene(box));
                ok.setCancelButton(false);

                final boolean[] res = new boolean[1];
                ok.setOnAction(new CloseDialogHandler(dialogStage, res));
                cancel.setCancelButton(true);
                cancel.setOnAction(new CloseDialogHandler(dialogStage, null));
                dialogStage.centerOnScreen();
                dialogStage.showAndWait();
                return res[0] ? line.getText() : null;
            }
        });
        view.getEngine().setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
            @Override
            public WebEngine call(PopupFeatures param) {
                final Stage stage = new Stage(StageStyle.UTILITY);
                stage.initOwner(owner);
                final WebView popUpView = new WebView();
                stage.setScene(new Scene(popUpView));
                FXConsole fxConsole = new FXConsole(popUpView, stage);
                fxConsole.observeWebViewTitle();
                stage.show();
                return popUpView.getEngine();
            }
        });
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

    private static final class CloseDialogHandler implements EventHandler<ActionEvent> {
        private final Stage dialogStage;
        private final boolean[] res;

        public CloseDialogHandler(Stage dialogStage, boolean[] res) {
            this.dialogStage = dialogStage;
            this.res = res;
        }

        @Override
        public void handle(ActionEvent t) {
            dialogStage.close();
            if (res != null) {
                res[0] = true;
            }
        }
    }
}
