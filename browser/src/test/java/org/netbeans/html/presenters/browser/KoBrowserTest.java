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
package org.netbeans.html.presenters.browser;

import org.netbeans.html.presenters.browser.Browser;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.tck.KOTest;
import org.netbeans.html.json.tck.KnockoutTCK;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.netbeans.html.ko4j.KO4J;
import org.openide.util.lookup.ServiceProvider;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Factory;

@ServiceProvider(service = KnockoutTCK.class)
public class KoBrowserTest extends KnockoutTCK {
    private static final Logger LOG = Logger.getLogger(KoBrowserTest.class.getName());
    private static Class<?> browserClass;
    private static Fn.Presenter browserPresenter;
    
    public KoBrowserTest() {
    }

    static Object[] showBrwsr(URI uri, String cmd) throws IOException {
        LOG.log(Level.INFO, "Showing {0}", uri);
        if (cmd == null) {
            try {
                LOG.log(Level.INFO, "Trying Desktop.browse on {0} {2} by {1}", new Object[]{
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.vm.vendor"),
                    System.getProperty("java.vm.version"),});
                java.awt.Desktop.getDesktop().browse(uri);
                LOG.log(Level.INFO, "Desktop.browse successfully finished");
                return null;
            } catch (UnsupportedOperationException ex) {
                LOG.log(Level.INFO, "Desktop.browse not supported: {0}", ex.getMessage());
                LOG.log(Level.FINE, null, ex);
            }
        }
        {
            String cmdName = cmd == null ? "xdg-open" : cmd;
            String[] cmdArr = {
                cmdName, uri.toString()
            };
            LOG.log(Level.INFO, "Launching {0}", Arrays.toString(cmdArr));
            final Process process = Runtime.getRuntime().exec(cmdArr);
            return new Object[]{process, null};
        }
    }
   
    @Factory public static Object[] compatibilityTests() throws Exception {
        Browser.LOG.setLevel(Level.FINE);
        Browser.LOG.addHandler(new ConsoleHandler());
        
        final BrowserBuilder bb = BrowserBuilder.newBrowser(new Browser("KoBrowserTest", new Browser.Config())).
            loadClass(KoBrowserTest.class).
            loadPage("empty.html").
            invoke("initialized");

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                bb.showAndWait();
            }
        });

        List<Object> res = new ArrayList<Object>();
        Class<? extends Annotation> test = 
            loadClass().getClassLoader().loadClass(KOTest.class.getName()).
            asSubclass(Annotation.class);

        Class[] arr = (Class[]) loadClass().getDeclaredMethod("tests").invoke(null);

        final HttpServer s = Browser.findServer(browserPresenter);
        ServerConfiguration conf = s.getServerConfiguration();
        conf.addHttpHandler(new DynamicHTTP(s), "/dynamic");
        for (Class c : arr) {
            for (Method m : c.getMethods()) {
                if (m.getAnnotation(test) != null) {
                    res.add(new KOScript(browserPresenter, m));
                }
            }
        }
        return res.toArray();
    }
    
    public static Class[] tests() {
        return testClasses();
    }

    static synchronized Class<?> loadClass() throws InterruptedException {
        while (browserClass == null) {
            KoBrowserTest.class.wait();
        }
        return browserClass;
    }
    
    public static synchronized void ready(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserPresenter = Fn.activePresenter();
        KoBrowserTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        browserPresenter = Fn.activePresenter();
        Class<?> classpathClass = ClassLoader.getSystemClassLoader().loadClass(KoBrowserTest.class.getName());
        Method m = classpathClass.getMethod("ready", Class.class);
        m.invoke(null, KoBrowserTest.class);
    }

    @Override
    public BrwsrCtx createContext() {
        KO4J ko = new KO4J();
        Contexts.Builder b = Contexts.newBuilder();
        b.register(Technology.class, ko.knockout(), 7);
        b.register(Transfer.class, ko.transfer(), 7);
        assertNotNull(browserPresenter, "Presenter needs to be registered");
        b.register(Executor.class, (Executor)browserPresenter, 10);
        return b.build();
    }

    @Override
    public boolean canFailWebSocketTest() {
        return true;
    }

    @Override
    public Object createJSON(Map<String, Object> values) {
        Object json = putValue(null, null, null);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            json = putValue(json, entry.getKey(), entry.getValue());
        }
        return json;
    }

    private Fn jsonFn;
    private Object putValue(Object json, String key, Object value) {
        if (jsonFn == null) {
            jsonFn = Fn.activePresenter().defineFn(
                "if (json === null) json = new Object();"
                + "if (key !== null) json[key] = value;"
                + "return json;",
                "json", "key", "value"
            );
        }
        try {
            return jsonFn.invoke(null, json, key, value);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private Fn executeScript;
    @Override
    public Object executeScript(String script, Object[] arguments) {
        if (executeScript == null) {
            executeScript = Fn.activePresenter().defineFn(
                "var f = new Function(s); "
              + "return f.apply(null, args);",
              "s", "args"
            );
        }
        try {
            return executeScript.invoke(null, script, arguments);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

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
}
