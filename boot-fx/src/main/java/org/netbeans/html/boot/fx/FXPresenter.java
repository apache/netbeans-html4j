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
package org.netbeans.html.boot.fx;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javafx.scene.web.WebView;
import net.java.html.boot.BrowserBuilder;
import org.apidesign.html.boot.spi.Fn;
import org.openide.util.lookup.ServiceProvider;

/** This is an implementation class, use {@link BrowserBuilder} API. Just
 * include this JAR on classpath and the {@link BrowserBuilder} API will find
 * this implementation automatically.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = Fn.Presenter.class)
public final class FXPresenter extends AbstractFXPresenter {
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

    protected void waitFinished() {
        FXBrwsr.waitFinished();
    }

    protected WebView findView(final URL resource) {
        return FXBrwsr.findWebView(resource, this);
    }
}
