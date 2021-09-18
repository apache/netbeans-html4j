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
package org.netbeans.html.ko.felix.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.tck.KnockoutTCK;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = KnockoutTCK.class)
public class KnockoutFelixTCKImpl extends KnockoutTCK implements Callable<Class[]> {
    
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
    
    public static void start(String callBackClass, URI server, final boolean useAllClassloader) throws Exception {
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(KnockoutFelixTCKImpl.class).
            loadPage(server.toString()).
            invoke("initialized", callBackClass);

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle[] arr = FrameworkUtil.getBundle(BrowserBuilder.class).getBundleContext().getBundles();
                    if (useAllClassloader) {
                        final ClassLoader osgiClassLoader = new AllBundlesLoader(arr);
                        bb.classloader(osgiClassLoader);
                    }
                    bb.showAndWait();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    public static void initialized(String... args) throws Exception {
        Bundle bundle = FrameworkUtil.getBundle(KnockoutFelixTCKImpl.class);
        if (bundle == null) {
            throw new IllegalStateException(
                "Should be loaded from a bundle. But was: " + KnockoutFelixTCKImpl.class.getClassLoader()
            );
        }
        Class<?> classpathClass = ClassLoader.getSystemClassLoader().loadClass(args[0]);
        Method m = classpathClass.getMethod("initialized", Class.class, Object.class);
        browserContext = Fn.activePresenter();
        m.invoke(null, KnockoutFelixTCKImpl.class, browserContext);
    }
    
    @Override
    public BrwsrCtx createContext() {
        try {
            Contexts.Builder cb = Contexts.newBuilder().
                register(Technology.class, (Technology)osgiInstance("KOTech"), 10).
                register(Transfer.class, (Transfer)osgiInstance("KOTransfer"), 10).
                register(Executor.class, (Executor)browserContext, 10);
//        if (fx.areWebSocketsSupported()) {
//            cb.register(WSTransfer.class, fx, 10);
//        }
            return cb.build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Object osgiInstance(String simpleName) throws IllegalAccessException, SecurityException, IllegalArgumentException, Exception, NoSuchMethodException, InstantiationException, InvocationTargetException {
        Class<?> fxCls = loadOSGiClass(
                "org.netbeans.html.ko4j." + simpleName,
                FrameworkUtil.getBundle(KnockoutFelixTCKImpl.class).getBundleContext()
        );
        final Constructor<?> cnstr = fxCls.getDeclaredConstructor();
        cnstr.setAccessible(true);
        Object fx = cnstr.newInstance();
        return fx;
    }

    @Override
    public Object createJSON(Map<String, Object> values) {
        Object json = createObj();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            putObj(json, entry.getKey(), entry.getValue());
        }
        return json;
    }
    
    @JavaScriptBody(args = {  }, body = "return {};")
    private static native Object createObj();
    @JavaScriptBody(args = { "obj", "prop", "val" }, body = "obj[prop] = val;")
    private static native void putObj(Object obj, String prop, Object val);

    @Override
    @JavaScriptBody(args = { "s", "args" }, body = ""
        + "var f = new Function(s); "
        + "return f.apply(null, args);"
    )
    public native Object executeScript(String script, Object[] arguments);

    @JavaScriptBody(args = {  }, body = 
          """
          var h;if (!!window && !!window.location && !!window.location.href)
            h = window.location.href;
          else   h = null;return h;
          """
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

    private static final class AllBundlesLoader extends ClassLoader {
        private final Bundle[] arr;

        public AllBundlesLoader(Bundle[] arr) {
            super(ClassLoader.getSystemClassLoader().getParent());
            this.arr = arr;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return loadClass(name, false);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            ClassNotFoundException err = null;
            for (Bundle b : arr) {
                try {
                    Class<?> cls = b.loadClass(name);
                    if (FrameworkUtil.getBundle(cls) == b) {
                        return cls;
                    }
                } catch (ClassNotFoundException ex) {
                    err = ex;
                }
            }
            throw err;
        }

        @Override
        protected URL findResource(String name) {
            for (Bundle b : arr) {
                URL r = b.getResource(name);
                if (r != null) {
                    return r;
                }
            }
            return null;
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            List<URL> ret = new ArrayList<URL>();
            for (Bundle b : arr) {
                Enumeration<URL> en = b.getResources(name);
                if (en != null) while (en.hasMoreElements()) {
                    URL u = en.nextElement();
                    ret.add(u);
                }
            }
            return Collections.enumeration(ret);
        }
        
        
        
    }
}
