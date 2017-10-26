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
package org.netbeans.html.wstyrus;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.netbeans.html.boot.fx.FXGCPresenter;
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
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = KnockoutTCK.class)
public final class TyrusKnockoutTest extends KnockoutTCK {
    private static Class<?> browserClass;
    private static Fn.Presenter browserContext;
    
    public TyrusKnockoutTest() {
    }
    
    @Factory public static Object[] compatibilityTests() throws Exception {
        Class[] arr = testClasses();
        for (int i = 0; i < arr.length; i++) {
            assertEquals(
                arr[i].getClassLoader(),
                TyrusKnockoutTest.class.getClassLoader(),
                "All classes loaded by the same classloader"
            );
        }
        
        URI uri = TyrusDynamicHTTP.initServer();
    
        final BrowserBuilder bb = BrowserBuilder.newBrowser(new FXGCPresenter()).loadClass(TyrusKnockoutTest.class).
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
            Class<? extends Annotation> koTest = 
                c.getClassLoader().loadClass(KOTest.class.getName()).
                asSubclass(Annotation.class);
            for (Method m : c.getMethods()) {
                if (m.getAnnotation(koTest) != null) {
                    res.add(new TyrusFX(browserContext, m));
                }
            }
        }
        return res.toArray();
    }

    static synchronized ClassLoader getClassLoader() throws InterruptedException {
        while (browserClass == null) {
            TyrusKnockoutTest.class.wait();
        }
        return browserClass.getClassLoader();
    }
    
    public static synchronized void initialized(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserContext = Fn.activePresenter();
        TyrusKnockoutTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Assert.assertSame(
            TyrusKnockoutTest.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
            "No special classloaders"
        );
        TyrusKnockoutTest.initialized(TyrusKnockoutTest.class);
    }
    
    @Override
    public BrwsrCtx createContext() {
        KO4J ko = new KO4J(browserContext);
        TyrusContext tc = new TyrusContext();
        Contexts.Builder cb = Contexts.newBuilder().
            register(Technology.class, ko.knockout(), 10).
            register(Transfer.class, tc, 10).
            register(WSTransfer.class, tc, 10).
            register(Executor.class, (Executor)browserContext, 10).
            register(Fn.Presenter.class, (Fn.Presenter)browserContext, 10);
                
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
}
