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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javafx.scene.web.WebView;
import net.java.html.boot.BrowserBuilder;
import org.netbeans.html.boot.spi.Fn;
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
                File f = new File(System.getProperty("java.home"), "lib/jfxrt.jar");
                if (f.exists()) {
                    Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    m.setAccessible(true);
                    URL l = f.toURI().toURL();
                    m.invoke(ClassLoader.getSystemClassLoader(), l);
                }
            }
        } catch (Exception ex) {
            throw new LinkageError("Can't add jfxrt.jar on the classpath", ex);
        }
    }

    @Override
    void waitFinished() {
        FXBrwsr.waitFinished();
    }

    @Override
    WebView findView(final URL resource) {
        return FXBrwsr.findWebView(resource, this);
    }
}
