/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
package net.java.html.boot.fx;

import java.net.URL;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.fx.AbstractFXPresenter;

/** Utility methods for working with <em>JavaFX</em> <code>WebView</code>s.
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 * @since 0.6
 */
public final class FXBrowsers {
    private FXBrowsers() {
    }
    
    /** Enables the Java/JavaScript brige (that supports {@link JavaScriptBody} and co.)
     * in the provided <code>webView</code>. This method returns 
     * immediately. Once the support is active, it calls back specified
     * method in <code>onPageLoad</code> class - the class can possibly be
     * loaded by a different classloader (to enable replacement of
     * methods with {@link JavaScriptBody} annotations with executable
     * versions). The method <code>methodName</code> needs to be <code>public</code>
     * (in a public class), <code>static</code> and take either no parameters
     * or an array of {@link String}s.
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
