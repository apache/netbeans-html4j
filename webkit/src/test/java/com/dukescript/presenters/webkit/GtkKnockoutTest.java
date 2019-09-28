package com.dukescript.presenters.webkit;

/*
 * #%L
 * WebKit Presenter - a library from the "DukeScript Presenters" project.
 * 
 * Dukehoff GmbH designates this particular file as subject to the "Classpath"
 * exception as provided in the README.md file that accompanies this code.
 * %%
 * Copyright (C) 2015 - 2019 Dukehoff GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


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
        
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                bb.showAndWait();
            }
        });
        
        ClassLoader l = getClassLoader();
        List<Object> res = new ArrayList<>();
        for (Class oldC : arr) {
            Class<?> c = Class.forName(oldC.getName(), true, l);
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
