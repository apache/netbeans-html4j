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
package net.java.html.boot.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.tck.KOTest;
import org.netbeans.html.json.tck.KnockoutTCK;
import org.netbeans.html.ko4j.KO4J;
import org.netbeans.html.wstyrus.TyrusContext;
import org.openide.util.lookup.ServiceProvider;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = KnockoutTCK.class)
public final class KnockoutEnvJSTest extends KnockoutTCK {
    private static Class<?> browserClass;
    private static Fn.Presenter browserContext;
    private static URI baseUri;

    public KnockoutEnvJSTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        ScriptEngine eng = new ScriptEngineManager().getEngineByName("nashorn");
        if (eng == null) {
            return new Object[] {
                new KOCase(null, null, "Nashorn engine not found. Skipping!")
            };
        }

        Class[] arr = testClasses();
        for (int i = 0; i < arr.length; i++) {
            assertEquals(
                arr[i].getClassLoader(),
                KnockoutEnvJSTest.class.getClassLoader(),
                "All classes loaded by the same classloader"
            );
        }

        baseUri = DynamicHTTP.initServer();

        final Fn.Presenter p = Scripts.newPresenter()
            .engine(eng)
            .sanitize(false)
            .executor(KOCase.JS)
            .build();
        try {
            Class.forName("java.lang.Module");
        } catch (ClassNotFoundException oldJDK) {
            try {
                URL envNashorn = new URL("https://bugs.openjdk.java.net/secure/attachment/11894/env.nashorn.1.2-debug.js");
                InputStream is = envNashorn.openStream();
                p.loadScript(new InputStreamReader(is));
                is.close();
            } catch (UnknownHostException | ConnectException ex) {
                ex.printStackTrace();
                return new Object[0];
            }
        }

        final BrowserBuilder bb = BrowserBuilder.newBrowser(p).
            loadClass(KnockoutEnvJSTest.class).
            loadPage(baseUri.toString()).
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
                res.add(new KOCase(browserContext, m, skipMsg(m.getName())));
            }
        }
    }

    private static String skipMsg(String methodName) {
        try {
            Class.forName("java.lang.Module");
            return "Don't try the env.js emulation on JDK9 and newer";
        } catch (ClassNotFoundException oldJDK) {
            // OK, go on
        }
        final String ver = System.getProperty("java.runtime.version"); // NOI18N
        if (
            ver.startsWith("1.8.0_25") ||
            ver.startsWith("1.8.0_31") ||
            ver.startsWith("1.8.0_40")
        ) {
            return "Broken due to JDK-8047764";
        }
        if (
            !ver.startsWith("1.8.0_")
        ) {
            // 1.8.0_ are and will remain broken
            return null;
        }
        switch (methodName) {
            case "paintTheGridOnClick":
            case "displayContentOfArrayOfPeople":
            case "connectUsingWebSocket":
            case "selectWorksOnModels":
            case "archetypeArrayModificationVisible":
            case "noLongerNeededArrayElementsCanDisappear":
                return "Does not work on JDK8, due to JDK-8046013";
            case "modifyRadioValueOnEnum":
                return "Does not work on JDK8";
        }
        return null;
    }

    static synchronized ClassLoader getClassLoader() throws InterruptedException {
        while (browserClass == null) {
            KnockoutEnvJSTest.class.wait();
        }
        return browserClass.getClassLoader();
    }

    public static synchronized void initialized(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserContext = Fn.activePresenter();
        KnockoutEnvJSTest.class.notifyAll();
    }

    public static void initialized() throws Exception {
        Assert.assertSame(
            KnockoutEnvJSTest.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
            "No special classloaders"
        );
        KnockoutEnvJSTest.initialized(KnockoutEnvJSTest.class);
        browserContext = Fn.activePresenter();
    }

    @Override
    public BrwsrCtx createContext() {
        KO4J fx = new KO4J(browserContext);
        TyrusContext tc = new TyrusContext();
        Contexts.Builder cb = Contexts.newBuilder().
            register(Technology.class, fx.knockout(), 10).
            register(Transfer.class, tc, 10);
        cb.register(Fn.Presenter.class, browserContext, 10);
        cb.register(Executor.class, (Executor)browserContext, 10);
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
    @JavaScriptBody(args = { "s", "args" }, body = "\n"
        + "var f = new Function(s);\n"
        + "return f.apply(null, args);\n"
    )
    public native Object executeScript(String script, Object[] arguments);

    private static String findBaseURL() {
        return baseUri.toString();
    }

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
