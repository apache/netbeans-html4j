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
package net.java.html.boot.fx;

import org.netbeans.html.boot.fx.InitializeWebView;
import java.net.URL;
import javafx.application.Platform;
import javafx.scene.web.WebView;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.context.spi.Contexts.Id;

/** Utility methods to use {@link WebView} and {@link JavaScriptBody} code
 * in existing <em>JavaFX</em> applications.
 * This class is for those who want to instantiate their own {@link WebView},
 * configure it manually, embed it into own <em>JavaFX</em>
 * application and based on other events in the application
 * {@link #runInBrowser(javafx.scene.web.WebView, java.lang.Runnable) re-execute code} 
 * inside of such {@link WebView}s.
 * <p>
 * In case such detailed control is not necessary,
 * consider using {@link BrowserBuilder}. Btw. when using the {@link BrowserBuilder}
 * one can execute presenter in headless mode. Just specify: <code>
 * {@link System}.{@link System#setProperty(java.lang.String, java.lang.String) setProperty}("fxpresenter.headless", "true");
 * </code>
 *
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
     * <p>
     * Since introduction of {@link Id technology identifiers} the 
     * provided <code>args</code> strings are also passed to the 
     * {@link BrwsrCtx context} when it is being 
     * {@link Contexts#newBuilder(java.lang.Object...) created}
     * and can influence the selection
     * of available technologies 
     * (like {@link org.netbeans.html.json.spi.Technology},
     * {@link org.netbeans.html.json.spi.Transfer} or
     * {@link org.netbeans.html.json.spi.WSTransfer}).
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
        Object[] context = new Object[args.length + 1];
        System.arraycopy(args, 0, context, 1, args.length);
        final InitializeWebView load = new InitializeWebView(webView, null);
        context[0] = load;
        BrowserBuilder.newBrowser(context).
            loadPage(url.toExternalForm()).
            loadFinished(load).
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
     * immediately. Once the support is active, it calls back {@link Runnable#run() run}
     * method in <code>onPageLoad</code>. 
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
        load(webView, url, onPageLoad, loader, new Object[0]);
    }
    
    /** Enables the Java/JavaScript bridge (that supports {@link JavaScriptBody} and co.)
     * in the provided <code>webView</code>. This method returns 
     * immediately. Once the support is active, it calls back {@link Runnable#run() run}
     * method in <code>onPageLoad</code>. 
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
     * @param context additonal configuration to pass to {@link BrowserBuilder#newBrowser(java.lang.Object...)}
     *   and {@link Contexts#newBuilder(java.lang.Object...)} factory methods 
     * @since 1.1
     */
    public static void load(
        WebView webView, final URL url, Runnable onPageLoad, ClassLoader loader,
        Object... context
    ) {
        Object[] newCtx = new Object[context.length + 1];
        System.arraycopy(context, 0, newCtx, 1, context.length);
        final InitializeWebView load = new InitializeWebView(webView, onPageLoad);
        newCtx[0] = load;
        BrowserBuilder.newBrowser(newCtx).
                loadPage(url.toExternalForm()).
                loadFinished(load).
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
        if (!(ud instanceof InitializeWebView)) {
            throw new IllegalArgumentException();
        }
        ((InitializeWebView)ud).runInContext(code);
    }
}
