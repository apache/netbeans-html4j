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

import java.lang.ref.WeakReference;
import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import net.java.html.BrwsrCtx;

public final class InitializeWebView extends AbstractFXPresenter implements Runnable {

    private final WebView webView;
    private final Runnable myLoad;
    BrwsrCtx ctx;

    public InitializeWebView(WebView webView, Runnable onLoad) {
        this.webView = webView;
        this.myLoad = onLoad;
        webView.setUserData(reference());
    }

    @Override
    public void run() {
        ctx = BrwsrCtx.findDefault(InitializeWebView.class);
        if (myLoad != null) {
            myLoad.run();
        }
    }

    @Override
    void waitFinished() {
        // don't wait
    }

    @Override
    WebView findView(final URL resource) {
        final Worker<Void> w = webView.getEngine().getLoadWorker();
        w.stateProperty().addListener(new FindViewListener(this, resource, w));
        return webView;
    }

    public final void runInContext(Runnable r) {
        ctx.execute(r);
    }

    private static class FindViewListener extends WeakReference<InitializeWebView>
    implements ChangeListener<Worker.State> {
        private InitializeWebView toNotify;
        private final URL resource;
        private final Worker<Void> w;

        public FindViewListener(InitializeWebView view, URL resource, Worker<Void> w) {
            super(view);
            this.toNotify = view;
            this.resource = resource;
            this.w = w;
        }
        private String previous;

        @Override
        public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State newState) {
            InitializeWebView view = get();
            if (view == null) {
                w.stateProperty().removeListener(this);
                return;
            }
            if (newState.equals(Worker.State.SUCCEEDED)) {
                if (checkValid(view)) {
                    view.onPageLoad();
                    toNotify = null;
                }
            }
            if (newState.equals(Worker.State.FAILED)) {
                checkValid(view);
                throw new IllegalStateException("Failed to load " + resource);
            }
        }

        private boolean checkValid(InitializeWebView view) {
            final String crnt = view.webView.getEngine().getLocation();
            if (previous != null && !previous.equals(crnt)) {
                w.stateProperty().removeListener(this);
                return false;
            }
            previous = crnt;
            return true;
        }
    }

}
