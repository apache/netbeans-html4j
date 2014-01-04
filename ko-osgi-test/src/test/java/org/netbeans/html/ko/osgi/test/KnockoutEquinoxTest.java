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
package org.netbeans.html.ko.osgi.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.tck.KOTest;
import org.apidesign.html.json.tck.KnockoutTCK;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.html.boot.impl.FnContext;
import org.netbeans.html.kofx.FXContext;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = KnockoutTCK.class)
public class KnockoutEquinoxTest extends KnockoutTCK {
    private static final Logger LOG = Logger.getLogger(KnockoutEquinoxTest.class.getName());
    private static Framework framework;
    private static File dir;
    static Framework framework() throws Exception {
        if (framework != null) {
            return framework;
        }
        for (FrameworkFactory ff : ServiceLoader.load(FrameworkFactory.class)) {
            Map<String,String> config = new HashMap<>();
            dir = File.createTempFile("osgi", "tmp");
            dir.delete();
            dir.mkdirs();
            config.put(Constants.FRAMEWORK_STORAGE, dir.getPath());
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "sun.misc");
            framework = ff.newFramework(config);
            framework.init();
            loadClassPathBundles(framework);
            framework.start();
            for (Bundle b : framework.getBundleContext().getBundles()) {
                try {
                    b.start();
                    LOG.log(Level.INFO, "Started {0}", b.getSymbolicName());
                } catch (BundleException ex) {
                    LOG.log(Level.WARNING, "Cannot start bundle " + b.getSymbolicName(), ex);
                }
            }
            return framework;
        }
        fail("No OSGi framework in the path");
        return null;
    }
    
    @AfterClass public static void cleanUp() throws Exception {
        if (framework != null) framework.stop();
        clearUpDir(dir);
    }
    private static void clearUpDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                clearUpDir(f);
            }
        }
        dir.delete();
    }
    
    

    private static void loadClassPathBundles(Framework f) throws IOException, BundleException {
        for (String jar : System.getProperty("java.class.path").split(File.pathSeparator)) {
            File file = new File(jar);
            if (!file.isFile()) {
                continue;
            }
            JarFile jf = new JarFile(file);
            final String name = jf.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
            jf.close();
            if (name != null) {
                if (name.contains("org.eclipse.osgi")) {
                    continue;
                }
                if (name.contains("testng")) {
                    continue;
                }
                final String path = "reference:" + file.toURI().toString();
                Bundle b = f.getBundleContext().installBundle(path);
            }
        }
    }
    
    private static Class<?> loadAClass(Class<?> c) throws Exception {
        for (Bundle b : framework().getBundleContext().getBundles()) {
            try {
                Class<?> osgiClass = b.loadClass(c.getName());
                if (
                    osgiClass != null && 
                    osgiClass.getClassLoader() != ClassLoader.getSystemClassLoader()
                ) {
                    return osgiClass;
                }
            } catch (ClassNotFoundException cnfe) {
                // go on
            }
        }
        fail("Cannot load " + c + " from the OSGi container!");
        return null;
    }
    
    private static Class<?> browserClass;
    private static Fn.Presenter browserContext;
    
    @Factory public static Object[] compatibilityTests() throws Exception {
        Class<?> tck = loadAClass(KnockoutTCK.class);
        Class<?> peer = loadAClass(KnockoutEquinoxTest.class);
        // initialize the TCK
        Callable<Class[]> inst = (Callable<Class[]>) peer.newInstance();
        
        Class[] arr = inst.call();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getClassLoader() == ClassLoader.getSystemClassLoader()) {
                fail("Should be an OSGi class: " + arr[i]);
            }
        }
        
        URI uri = DynamicHTTP.initServer();

        
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(peer).
            loadPage(uri.toString()).
            invoke("initialized");
        
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                bb.showAndWait();
            }
        });
        
        ClassLoader l = getClassLoader();
        List<Object> res = new ArrayList<Object>();
        for (int i = 0; i < arr.length; i++) {
            Class<?> c = Class.forName(arr[i].getName(), true, l);
            seekKOTests(c, res);
        }
        return res.toArray();
    }

    private static void seekKOTests(Class<?> c, List<Object> res) throws SecurityException, ClassNotFoundException {
        Class<? extends Annotation> koTest =
            c.getClassLoader().loadClass(KOTest.class.getName()).
            asSubclass(Annotation.class);
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(koTest) != null) {
                res.add(new KOFx(browserContext, m));
            }
        }
    }

    static synchronized ClassLoader getClassLoader() throws InterruptedException {
        while (browserClass == null) {
            KnockoutEquinoxTest.class.wait();
        }
        return browserClass.getClassLoader();
    }
    
    public static synchronized void initialized(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserContext = FnContext.currentPresenter();
        KnockoutEquinoxTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Class<?> classpathClass = ClassLoader.getSystemClassLoader().loadClass(KnockoutEquinoxTest.class.getName());
        Method m = classpathClass.getMethod("initialized", Class.class);
        m.invoke(null, KnockoutEquinoxTest.class);
        browserContext = FnContext.currentPresenter();
    }

    @Override
    public BrwsrCtx createContext() {
        FXContext fx = new FXContext(browserContext);
        Contexts.Builder cb = Contexts.newBuilder().
            register(Technology.class, fx, 10).
            register(Transfer.class, fx, 10);
//        if (fx.areWebSocketsSupported()) {
//            cb.register(WSTransfer.class, fx, 10);
//        }
        return cb.build();
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
        try {
            Class.forName("java.util.function.Function");
            return false;
        } catch (ClassNotFoundException ex) {
            // running on JDK7, FX WebView WebSocket impl does not work
            return true;
        }
    }
    
}
