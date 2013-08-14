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

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.java.html.boot.BrowserBuilder;
import netscape.javascript.JSObject;
import org.apidesign.html.boot.spi.Fn;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/** This is an implementation class, use {@link BrowserBuilder} API. Just
 * include this JAR on classpath and the {@link BrowserBuilder} API will find
 * this implementation automatically.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = Fn.Presenter.class)
public final class FXPresenter implements Fn.Presenter {
    static {
        try {
            try {
                Class<?> c = Class.forName("javafx.application.Platform");
                // OK, on classpath
            } catch (ClassNotFoundException classNotFoundException) {
                Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                m.setAccessible(true);
                File f = new File(System.getProperty("java.home"), "lib/jfxrt.jar");
                if (f.exists()) {
                    URL l = f.toURI().toURL();
                    m.invoke(ClassLoader.getSystemClassLoader(), l);
                }
            }
        } catch (Exception ex) {
            throw new LinkageError("Can't add jfxrt.jar on the classpath", ex);
        }
    }
    
    private WebEngine engine;
    
    @Override
    public Fn defineFn(String code, String... names) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function() {");
        sb.append("  return function(");
        String sep = "";
        for (String n : names) {
            sb.append(sep).append(n);
            sep = ",";
        }
        sb.append(") {\n");
        sb.append(code);
        sb.append("};");
        sb.append("})()");

        JSObject x = (JSObject) engine.executeScript(sb.toString());
        return new JSFn(x);
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        BufferedReader r = new BufferedReader(code);
        StringBuilder sb = new StringBuilder();
        for (;;) {
            String l = r.readLine();
            if (l == null) {
                break;
            }
            sb.append(l).append('\n');
        }
        engine.executeScript(sb.toString());
    }

    @Override
    public void displayPage(final URL resource, Runnable onLoad) {
        engine = FXBrwsr.findEngine(onLoad);
        try {
            FXInspect.initialize(engine);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                engine.load(resource.toExternalForm());
            }
        });
        FXBrwsr.waitFinished();
    }

    private static final class JSFn extends Fn {
        private final JSObject fn;

        public JSFn(JSObject fn) {
            this.fn = fn;
        }
        
        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            try {
                List<Object> all = new ArrayList<Object>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                all.addAll(Arrays.asList(args));
                Object ret = fn.call("call", all.toArray()); // NOI18N
                return ret == fn ? null : ret;
            } catch (Error t) {
                t.printStackTrace();
                throw t;
            } catch (Exception t) {
                t.printStackTrace();
                throw t;
            }
        }
    }

    /** This is an implementation class, use {@link BrowserBuilder} API. Just
     * include this JAR on classpath and the {@link BrowserBuilder} API will find
     * this implementation automatically.
     */
    public static class FXBrwsr extends Application {
        private static FXBrwsr INSTANCE;
        private static final CountDownLatch FINISHED = new CountDownLatch(1);

        private BorderPane root;

        private static final Logger LOG = Logger.getLogger(FXBrwsr.class.getName());
        
        public synchronized static WebEngine findEngine(final Runnable onLoad) {
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
                final WebEngine[] arr = { null };
                final CountDownLatch waitForResult = new CountDownLatch(1);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        arr[0] = INSTANCE.newEngine(onLoad);
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
                return INSTANCE.newEngine(onLoad);
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

        private WebEngine newEngine(final Runnable onLoad) {
            final WebView view = new WebView();
            final String nbUserDir = this.getParameters().getNamed().get("userdir"); // NOI18N
            WebController wc = new WebController(view, nbUserDir, getParameters().getUnnamed());
            root.setCenter(view);

            final Worker<Void> w = view.getEngine().getLoadWorker();
            w.stateProperty().addListener(new ChangeListener<Worker.State>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State newState) {
                    if (newState.equals(Worker.State.SUCCEEDED)) {
                        w.stateProperty().removeListener(this);
                        onLoad.run();
                    }
                }
            });
            return view.getEngine();
        }

        /**
         * Create a resizable WebView pane
         */
        private static class WebController {
            private final String ud;

            public WebController(WebView view, String ud, List<String> params) {
                final WebEngine eng = view.getEngine();
//                this.bridge = new JVMBridge(view.getEngine());
                this.ud = ud;
                LOG.log(Level.INFO, "Initializing WebView with {0}", params);

                if (params.size() > 0) {
                    LOG.log(Level.INFO, "loading page {0}", params.get(0));
                    eng.load(params.get(0));
                    LOG.fine("back from load");
                }
                eng.setOnAlert(new EventHandler<WebEvent<String>>() {
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
                /*
                WebDebug wd = null;
                try {
                    if (ud != null) {
                        wd = WebDebug.create(eng.impl_getDebugger(), ud);
                    }
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                this.dbg = wd;
                   */
            }

        }
        private static void waitFinished() {
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
}
