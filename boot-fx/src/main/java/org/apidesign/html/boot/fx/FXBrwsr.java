/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.boot.fx;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
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
import org.openide.util.Exceptions;

/** This is an implementation class, use {@link BrowserBuilder} API. Just
 * include this JAR on classpath and the {@link BrowserBuilder} API will find
 * this implementation automatically.
 */
public class FXBrwsr extends Application {
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
                    Exceptions.printStackTrace(ex);
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
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
}
