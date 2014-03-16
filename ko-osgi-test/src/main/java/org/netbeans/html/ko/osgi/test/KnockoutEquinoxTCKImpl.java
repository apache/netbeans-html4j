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
package org.netbeans.html.ko.osgi.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.tck.KnockoutTCK;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = KnockoutTCK.class)
public class KnockoutEquinoxTCKImpl extends KnockoutTCK implements Callable<Class[]> {
    
    private static Fn.Presenter browserContext;

    public static Class loadOSGiClass(String name, BundleContext ctx) throws Exception {
        for (Bundle b : ctx.getBundles()) {
            try {
                Class<?> osgiClass = b.loadClass(name);
                if (osgiClass != null && osgiClass.getClassLoader() != ClassLoader.getSystemClassLoader()) {
                    return osgiClass;
                }
            } catch (ClassNotFoundException cnfe) {
                // go on
            }
        }
        throw new IllegalStateException("Cannot load " + name + " from the OSGi container!");
    }

    @Override
    public Class[] call() throws Exception {
        return testClasses();
    }
    
    public static void start(URI server) throws Exception {
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(KnockoutEquinoxTCKImpl.class).
            loadPage(server.toString()).
            invoke("initialized");

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final ClassLoader osgiClassLoader = BrowserBuilder.class.getClassLoader();
                    Thread.currentThread().setContextClassLoader(osgiClassLoader);
                    bb.showAndWait();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    public static void initialized() throws Exception {
        Bundle bundle = FrameworkUtil.getBundle(KnockoutEquinoxTCKImpl.class);
        if (bundle == null) {
            throw new IllegalStateException(
                "Should be loaded from a bundle. But was: " + KnockoutEquinoxTCKImpl.class.getClassLoader()
            );
        }
        Class<?> classpathClass = ClassLoader.getSystemClassLoader().loadClass(
            "org.netbeans.html.ko.osgi.test.KnockoutEquinoxIT"
        );
        Method m = classpathClass.getMethod("initialized", Class.class, Object.class);
        browserContext = Fn.activePresenter();
        m.invoke(null, KnockoutEquinoxTCKImpl.class, browserContext);
    }
    
    @Override
    public BrwsrCtx createContext() {
        try {
            Class<?> fxCls = loadOSGiClass(
                "org.netbeans.html.ko4j.FXContext",
                FrameworkUtil.getBundle(KnockoutEquinoxTCKImpl.class).getBundleContext()
            );
            final Constructor<?> cnstr = fxCls.getConstructor(Fn.Presenter.class);
            cnstr.setAccessible(true);
            Object fx = cnstr.newInstance(browserContext);
            Contexts.Builder cb = Contexts.newBuilder().
                register(Technology.class, (Technology)fx, 10).
                register(Transfer.class, (Transfer)fx, 10).
                register(Executor.class, (Executor)browserContext, 10);
//        if (fx.areWebSocketsSupported()) {
//            cb.register(WSTransfer.class, fx, 10);
//        }
            return cb.build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Object createJSON(Map<String, Object> values) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return json;
    }

    @Override
    @JavaScriptBody(args = { "s", "args" }, body = ""
        + "var f = new Function(s); "
        + "return f.apply(null, args);"
    )
    public native Object executeScript(String script, Object[] arguments);

    @JavaScriptBody(args = {  }, body = 
          "var h;"
        + "if (!!window && !!window.location && !!window.location.href)\n"
        + "  h = window.location.href;\n"
        + "else "
        + "  h = null;"
        + "return h;\n"
    )
    private static native String findBaseURL();
    
    @Override
    public URI prepareURL(String content, String mimeType, String[] parameters) {
        try {
            final URL baseURL = new URL(findBaseURL());
            StringBuilder sb = new StringBuilder();
            sb.append("/dynamic?mimeType=").append(mimeType);
            for (int i = 0; i < parameters.length; i++) {
                sb.append("&param" + i).append("=").append(parameters[i]);
            }
            String mangle = content.replace("\n", "%0a")
                .replace("\"", "\\\"").replace(" ", "%20");
            sb.append("&content=").append(mangle);

            URL query = new URL(baseURL, sb.toString());
            URLConnection c = query.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            URI connectTo = new URI(br.readLine());
            return connectTo;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public boolean canFailWebSocketTest() {
        return true;
    }

}
