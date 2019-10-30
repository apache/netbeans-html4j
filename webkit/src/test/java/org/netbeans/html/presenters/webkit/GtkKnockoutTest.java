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
package org.netbeans.html.presenters.webkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.spi.WSTransfer;
import org.netbeans.html.json.tck.KOTest;
import org.netbeans.html.json.tck.KnockoutTCK;
import org.netbeans.html.ko4j.KO4J;
import org.openide.util.lookup.ServiceProvider;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.Factory;

@ServiceProvider(service = KnockoutTCK.class)
public final class GtkKnockoutTest extends KnockoutTCK {
    private static Class<?> browserClass;
    private static Fn.Presenter browserContext;
    private static final Timeout WATCHER = new Timeout(60000);
    
    public GtkKnockoutTest() {
    }
    
    @Factory public static Object[] compatibilityTests() throws Exception {
        Class[] arr = testClasses();
        for (int i = 0; i < arr.length; i++) {
            assertEquals(arr[i].getClassLoader(),
                GtkKnockoutTest.class.getClassLoader(),
                "All classes loaded by the same classloader"
            );
        }
        
        URI uri = DynamicHTTP.initServer();
    
        final BrowserBuilder bb = BrowserBuilder.newBrowser(
            new WebKitPresenter(true)
        ).loadClass(GtkKnockoutTest.class).
            loadPage(uri.toString()).
            invoke("initialized");
        
        Future<Void> future = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
            @Override
            public Void call() {
                bb.showAndWait();
                return null;
            }
        });

        List<Object> res = new ArrayList<>();
        try {
            try {
                future.get(3, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                // no error in given time, let's go on
            }
            ClassLoader l = getClassLoader();
            for (Class oldC : arr) {
                Class<?> c = Class.forName(oldC.getName(), true, l);
                seekKOTests(c, res);
            }
        } catch (InterruptedException | ExecutionException err) {
            err.printStackTrace();
            if (err.getCause() instanceof LinkageError) {
                res.add(new Skip(err.getCause().getMessage()));
            } else {
                res.add(new Skip(err.getMessage()));
            }
        }
        return res.toArray();
    }

    private static void seekKOTests(Class<?> c, List<Object> res) throws SecurityException, ClassNotFoundException {
        Class<? extends Annotation> koTest =
            c.getClassLoader().loadClass(KOTest.class.getName()).
            asSubclass(Annotation.class);
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(koTest) != null) {
                res.add(new Case(browserContext, m));
            }
        }
    }

    static synchronized ClassLoader getClassLoader() throws InterruptedException {
        while (browserClass == null) {
            GtkKnockoutTest.class.wait();
        }
        return browserClass.getClassLoader();
    }
    
    public static synchronized void initialized(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserContext = Fn.activePresenter();
        GtkKnockoutTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Assert.assertSame(GtkKnockoutTest.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
            "No special classloaders"
        );
        GtkKnockoutTest.initialized(GtkKnockoutTest.class);
        browserContext = Fn.activePresenter();
    }
    
    @Override
    public BrwsrCtx createContext() {
        KO4J ko4j = new KO4J();
        Contexts.Builder cb = Contexts.newBuilder().
            register(Technology.class, ko4j.knockout(), 10).
            register(Transfer.class, ko4j.transfer(), 10);
        if (ko4j.websockets() != null) {
            cb.register(WSTransfer.class, ko4j.websockets(), 10);
        }
        cb.register(Executor.class, (Executor)browserContext, 10);
        cb.register(Fn.Presenter.class, browserContext, 10);
        BrwsrCtx ctx = cb.build();
        return ctx;
    }

    @Override
    public Object createJSON(Map<String, Object> values) {
        Object json = createJSON();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            setProperty(json, entry.getKey(), entry.getValue());
        }
        return json;
    }
    
    @JavaScriptBody(args = {}, body = "return new Object();")
    private static native Object createJSON();
    @JavaScriptBody(args = { "json", "key", "value" }, body = "json[key] = value;")
    private static native void setProperty(Object json, String key, Object value);

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
                sb.append("&param").append(i).append("=").append(parameters[i]);
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
