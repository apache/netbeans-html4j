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
package net.java.html.boot.fx;

import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import net.java.html.boot.BrowserBuilder;
import org.apidesign.html.boot.fx.AbstractFXPresenter;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public final class FXBrowsers {
    private FXBrowsers() {
    }
    
    public static void load(
        final WebView webView, final URL url, 
        Class<?> onPageLoad, String methodName,
        String... args
    ) {
        class Load extends AbstractFXPresenter {
            @Override
            protected void waitFinished() {
                // don't wait
            }

            @Override
            protected WebView findView(URL resource) {
                final Worker<Void> w = webView.getEngine().getLoadWorker();
                w.stateProperty().addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State newState) {
                        if (newState.equals(Worker.State.SUCCEEDED)) {
                            onPageLoad();
                        }
                        if (newState.equals(Worker.State.FAILED)) {
                            throw new IllegalStateException("Failed to load " + url);
                        }
                    }
                });
                
                return webView;
            }
        }
        BrowserBuilder.newBrowser(new Load()).
            loadPage(url.toExternalForm()).
            loadClass(onPageLoad).
            invoke(methodName, args).
            showAndWait();
    }
}
