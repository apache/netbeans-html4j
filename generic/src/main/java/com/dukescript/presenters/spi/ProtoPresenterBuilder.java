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
package com.dukescript.presenters.spi;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;

/** The <em>prototypical</em> presenter builder. Builds a {@link Fn.Presenter} based on
 * top of textual protocol transferred between JVM and JavaScript engines.
 */
public final class ProtoPresenterBuilder {
    private Evaluator loadScript;
    private Executor executor;
    private Preparator onReady;
    private boolean sync;
    private boolean eval;
    private String type;
    private String app;
    private Displayer displayer;
    private Logger logger;
    private boolean implementExecutor;
    private final List<Object> data = new ArrayList<Object>();

    private ProtoPresenterBuilder() {
    }

    /** Starts building new, customized instance of {@link ProtoPresenter}.
     * @return new builder
     */
    public static ProtoPresenterBuilder newBuilder() {
        return new ProtoPresenterBuilder();
    }

    /** Interfaces for evaluation of JavaScript code.
     * Registered via {@link #loadJavaScript} method.
     */
    @FunctionalInterface
    public interface Evaluator {
        /** Evaluates (potentially asynchronously) given code in the JavaScript
         * engine.
         * @param code the code to evaluate
         */
        public void eval(String code);
    }

    /**
     * Registers an implementation of {@link Evaluator}.
     * @param loadScript the evaluator to use
     * @param synchronous is the evaluator synchronous or asynchronous
     * @return this builder
     */
    public ProtoPresenterBuilder loadJavaScript(Evaluator loadScript, boolean synchronous) {
        this.loadScript = loadScript;
        this.sync = synchronous;
        return this;
    }

    /**
     * Registers the executor to run all tasks in.
     *
     * @param executor the executor
     * @param implementExecutor {@code true} if the presenter created
     *   by {@link #build()} method should also implement the {@link Executor}
     *   interface
     * @return this builder
     */
    public ProtoPresenterBuilder dispatcher(Executor executor, boolean implementExecutor) {
        this.executor = executor;
        this.implementExecutor = implementExecutor;
        return this;
    }

    /** Prepares the JavaScript environment. Defines a globally visible
     * JavaScript function which takes five string arguments and
     * then calls {@link ProtoPresenter#js2java(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String) }
     * method of the {@link #build() created} {@link ProtoPresenter}.
     * Once the function is ready, it should notify the system
     * of the name of the function by calling {@link OnPrepared#callbackIsPrepared(java.lang.String)}.
     */
    @FunctionalInterface
    public interface Preparator {
        void prepare(OnPrepared onReady);
    }

    /** Callback to make when {@link Preparator#prepare(OnPrepared)} is
     * finished.
     */
    public static abstract class OnPrepared {
        OnPrepared() {
        }

        /** Called when callback function prepared by {@link Preparator#prepare}
         * is ready.
         *
         * @param callbackFunctionName the name of created JavaScript function
         */
        public abstract void callbackIsPrepared(String callbackFunctionName);
    }

    /** Registers instance of {@link Preparator}.
     *
     * @param onReady the instance to use
     * @param evalJavaScript is the result of function registered by {@link OnPrepared#callbackIsPrepared(java.lang.String)}
     *   just a string (return {@code true}) or real JavaScript object (return {@code false})?
     * @return this builder
     */
    public ProtoPresenterBuilder preparator(Preparator onReady, boolean evalJavaScript) {
        this.onReady = onReady;
        this.eval = evalJavaScript;
        return this;
    }

    /** The type of the presenter (iOS, Android, etc.).
     * @param type string to identify the presenter
    * @return this builder
    */
    public ProtoPresenterBuilder type(String type) {
        this.type = type;
        return this;
    }

    /** The identification of the application.
     *
     * @param app string to identify the application
     * @return this builder
     */
    public ProtoPresenterBuilder app(String app) {
        this.app = app;
        return this;
    }

    /** Interface to handle displaying of a URL.
     * Register via {@link ProtoPresenterBuilder#displayer(org.netbeans.html.presenter.spi.ProtoPresenterBuilder.Displayer)}.
     */
    @FunctionalInterface
    public interface Displayer {
        /** Display given URL and callback when the page is ready.
         *
         * @param url the page to display
         * @param onLoad callback to make when the page is ready
         */
        public void displayPage(URL url, Runnable onLoad);
    }

    /** Registers new displayer.
     *
     * @param displayer the instance of displayer.
     * @return this builder
     */
    public ProtoPresenterBuilder displayer(Displayer displayer) {
        this.displayer = displayer;
        return this;
    }

    /** Registers additional data with the {@link ProtoPresenter}.The data can be obtained by {@link ProtoPresenter#lookup}.
     * @param data instance of some data
     * @return this builder
     */
    public ProtoPresenterBuilder register(Object data) {
        this.data.add(data);
        return this;
    }

    /** Implementation of a logging interface from the {@link ProtoPresenter}.
     */
    @FunctionalInterface
    public interface Logger {
        /** Log a message. Uses levels and message formats suitable for
         * java.util.logging package.
         *
         * @param level value from 500-1000
         * @param msg message with optional curly braces parameters
         * @param args the parameter values
         */
        public void log(int level, String msg, Object... args);
    }

    /** Registers instance of logger.
     *
     * @param logger instance of logger - may be {@code null}
     * @return this builder
     */
    public ProtoPresenterBuilder logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /** Builds instance of presenter based on registered values.
     * @return instance of presenter
     */
    public ProtoPresenter build() {
        if (implementExecutor) {
            return new GenPresenterWithExecutor(this);
        }
        return new GenPresenter(this);
    }

    private static final class GenPresenterWithExecutor extends GenPresenter implements Executor {
        GenPresenterWithExecutor(ProtoPresenterBuilder b) {
            super(b);
        }

        @Override
        public void execute(Runnable command) {
            dispatch(command);
        }
    }

    private static class GenPresenter extends Generic implements ProtoPresenter {
        private final Evaluator loadScript;
        private final Executor executor;
        private final Preparator onReady;
        private final Displayer displayer;
        private final Logger logger;
        private final Object[] data;

        GenPresenter(ProtoPresenterBuilder b) {
            super(b.sync, b.eval, b.type, b.app);
            this.loadScript = b.loadScript;
            this.executor = b.executor;
            this.onReady = b.onReady;
            this.displayer = b.displayer;
            this.logger = b.logger;
            this.data = b.data.toArray();
        }

        @Override
        void log(Level level, String msg, Object... args) {
            if (logger != null) {
                logger.log(level.intValue(), msg, args);
            }
        }

        @Override
        void callbackFn(ProtoPresenterBuilder.OnPrepared onReady) {
            this.onReady.prepare(onReady);
        }

        @Override
        void loadJS(String js) {
            loadScript.eval(js);
        }

        @Override
        void dispatch(Runnable r) {
            executor.execute(r);
        }

        @Override
        public void displayPage(URL url, Runnable r) {
            if (url == null && r == null) {
                init();
            } else {
                displayer.displayPage(url, r);
            }
        }

        @Override
        public <T> T lookup(Class<T> type) {
            for (Object o : data) {
                if (type == o.getClass()) {
                    return type.cast(o);
                }
            }
            return null;
        }
    }
}
