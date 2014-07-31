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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.fx.AbstractFXPresenter;

/** Utility methods to use {@link WebView} and {@link JavaScriptBody} code
 * in existing <em>JavaFX</em> applications.
 * This class is for those who want to instantiate their own {@link WebView},
 * configure it manually, embed it into own <em>JavaFX</em>
 * application and based on other events in the application
 * {@link #runInBrowser(javafx.scene.web.WebView, java.lang.Runnable) re-execute code} 
 * inside of such {@link WebView}s.
 * In case such detailed control is not necessary,
 * consider using {@link BrowserBuilder}.
 * 
 * @author Jaroslav Tulach
 * @since 0.6
 */
public final class FXBrowsers {
    private FXBrowsers() {
    }
    
    /** Enables the Java/JavaScript bridge (that supports {@link JavaScriptBody} and co.)
     * in the provided <code>webView</code>. This method returns 
     * immediately. Once the support is active, it calls back specified
     * method in <code>onPageLoad</code> class - the class can possibly be
     * loaded by a different classloader (to enable replacement of
     * methods with {@link JavaScriptBody} annotations with executable
     * versions). The method <code>methodName</code> needs to be <code>public</code>
     * (in a public class), <code>static</code> and take either no parameters
     * or an array of {@link String}s.
     * <p>
     * This method sets {@link WebView#getUserData()} and {@link #runInBrowser(javafx.scene.web.WebView, java.lang.Runnable)}
     * relies on the value. Please don't alter it.
     * 
     * @param webView the instance of Web View to tweak
     * @param url the URL of the HTML page to load into the view
     * @param onPageLoad callback class with method <code>methodName</code>
     * @param methodName the method to call when the page is loaded
     * @param args arguments to pass to the <code>methodName</code> method
     */
    public static void load(
        final WebView webView, final URL url, 
        Class<?> onPageLoad, String methodName,
        String... args
    ) {
        BrowserBuilder.newBrowser(new Load(webView)).
            loadPage(url.toExternalForm()).
            loadClass(onPageLoad).
            invoke(methodName, args).
            showAndWait();
    }
    
    /** Enables the Java/JavaScript bridge (that supports {@link JavaScriptBody} and co.)
     * in the provided <code>webView</code>. This method returns 
     * immediately. Once the support is active, it calls back specified
     * method in <code>onPageLoad</code>'s run method. 
     * This is more convenient way to initialize the webview, 
     * but it requires one to make sure
     * all {@link JavaScriptBody} methods has been post-processed during
     * compilation and there will be no need to instantiate new classloader.
     * <p>
     * This method sets {@link WebView#getUserData()} and {@link #runInBrowser(javafx.scene.web.WebView, java.lang.Runnable)}
     * relies on the value. Please don't alter it.
     * 
     * @param webView the instance of Web View to tweak
     * @param url the URL of the HTML page to load into the view
     * @param onPageLoad callback to call when the page is ready
     * @since 0.8.1
     */
    public static void load(
        WebView webView, final URL url, Runnable onPageLoad
    ) {
        load(webView, url, onPageLoad, null);
    }
    
    /** Enables the Java/JavaScript bridge (that supports {@link JavaScriptBody} and co.)
     * in the provided <code>webView</code>. This method returns 
     * immediately. Once the support is active, it calls back specified
     * method in <code>onPageLoad</code>'s run method. 
     * This is more convenient way to initialize the webview, 
     * but it requires one to make sure
     * all {@link JavaScriptBody} methods has been post-processed during
     * compilation and there will be no need to instantiate new classloader.
     * <p>
     * This method sets {@link WebView#getUserData()} and {@link #runInBrowser(javafx.scene.web.WebView, java.lang.Runnable)}
     * relies on the value. Please don't alter it.
     * 
     * @param webView the instance of Web View to tweak
     * @param url the URL of the HTML page to load into the view
     * @param onPageLoad callback to call when the page is ready
     * @param loader the loader to use when constructing initial {@link BrwsrCtx} or <code>null</code>
     * @since 0.9
     */
    public static void load(
        WebView webView, final URL url, Runnable onPageLoad, ClassLoader loader
    ) {
        BrowserBuilder.newBrowser(new Load(webView)).
                loadPage(url.toExternalForm()).
                loadFinished(onPageLoad).
                classloader(loader).
                showAndWait();
    }
    
    /** Executes a code inside of provided {@link WebView}. This method
     * associates the {@link BrwsrCtx execution context} with provided browser,
     * so the {@link JavaScriptBody} annotations know where to execute
     * their JavaScript bodies.
     * The code is going to be executed synchronously
     * in case {@link Platform#isFxApplicationThread()} returns <code>true</code>.
     * Otherwise this method returns immediately and the code is executed
     * later via {@link Platform#runLater(java.lang.Runnable)}.
     * <p>
     * This method relies on {@link WebView#getUserData()} being properly
     * provided by the <code>load</code> methods in this class.
     * 
     * @param webView the web view previously prepared by one of the <code>load</code>
     *   methods in this class
     * @param code the code to execute
     * @throws IllegalArgumentException if the web view was not properly
     *   initialized
     * @see BrwsrCtx#execute(java.lang.Runnable) 
     * @since 0.8.1
     */
    public static void runInBrowser(WebView webView, Runnable code) {
        Object ud = webView.getUserData();
        if (!(ud instanceof Load)) {
            throw new IllegalArgumentException();
        }
        ((Load)ud).execute(code);
    }
    
    private static class Load extends AbstractFXPresenter {
        private final WebView webView;

        public Load(WebView webView) {
            webView.setUserData(this);
            this.webView = webView;
        }
        
        @Override
        protected void waitFinished() {
            // don't wait
        }

        @Override
        protected WebView findView(final URL resource) {
            final Worker<Void> w = webView.getEngine().getLoadWorker();
            w.stateProperty().addListener(new ChangeListener<Worker.State>() {
                private String previous;

                @Override
                public void changed(ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State newState) {
                    if (newState.equals(Worker.State.SUCCEEDED)) {
                        if (checkValid()) {
                            onPageLoad();
                        }
                    }
                    if (newState.equals(Worker.State.FAILED)) {
                        checkValid();
                        throw new IllegalStateException("Failed to load " + resource);
                    }
                }

                private boolean checkValid() {
                    final String crnt = webView.getEngine().getLocation();
                    if (previous != null && !previous.equals(crnt)) {
                        w.stateProperty().removeListener(this);
                        return false;
                    }
                    previous = crnt;
                    return true;
                }
            });

            return webView;
        }
    }
    
}
