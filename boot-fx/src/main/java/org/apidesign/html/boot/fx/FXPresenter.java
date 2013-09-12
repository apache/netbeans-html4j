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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.java.html.boot.BrowserBuilder;
import netscape.javascript.JSObject;
import org.apidesign.html.boot.spi.Fn;
import org.openide.util.lookup.ServiceProvider;

/** This is an implementation class, use {@link BrowserBuilder} API. Just
 * include this JAR on classpath and the {@link BrowserBuilder} API will find
 * this implementation automatically.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
