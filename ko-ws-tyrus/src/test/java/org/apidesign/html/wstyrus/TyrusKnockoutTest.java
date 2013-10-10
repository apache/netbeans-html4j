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
package org.apidesign.html.wstyrus;

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
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.boot.impl.FnContext;
import org.apidesign.html.boot.impl.FnUtils;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.spi.WSTransfer;
import org.apidesign.html.json.tck.KOTest;
import org.apidesign.html.json.tck.KnockoutTCK;
import org.apidesign.html.kofx.FXContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.lookup.ServiceProvider;
import org.testng.annotations.Factory;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
    
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(TyrusKnockoutTest.class).
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
        browserContext = FnContext.currentPresenter();
        TyrusKnockoutTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Class<?> classpathClass = ClassLoader.getSystemClassLoader().loadClass(TyrusKnockoutTest.class.getName());
        Method m = classpathClass.getMethod("initialized", Class.class);
        m.invoke(null, TyrusKnockoutTest.class);
        browserContext = FnContext.currentPresenter();
    }
    
    @Override
    public BrwsrCtx createContext() {
        FXContext fx = new FXContext(browserContext);
        TyrusContext tc = new TyrusContext();
        Contexts.Builder cb = Contexts.newBuilder().
            register(Technology.class, fx, 10).
            register(Transfer.class, fx, 10).
            register(WSTransfer.class, tc, 10);
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
