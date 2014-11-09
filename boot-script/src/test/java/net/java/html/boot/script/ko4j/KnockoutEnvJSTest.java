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
package net.java.html.boot.script.ko4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import net.java.html.boot.script.Scripts;
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
        try {
            Class.forName("java.lang.FunctionalInterface");
        } catch (ClassNotFoundException ex) {
            // only runs on JDK8
            return new Object[0];
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
        
        final Fn.Presenter p = Scripts.createPresenter(KOCase.JS);
        InputStream is = KnockoutEnvJSTest.class.getResourceAsStream("env.nashorn.1.2-debug.js");
        p.loadScript(new InputStreamReader(is));
        is.close();

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
        final String ver = System.getProperty("java.runtime.version"); // NOI18N
        if (
            ver.startsWith("1.8.0_25") ||
            ver.startsWith("1.8.0_40") 
        ) {
            return "Broken due to JDK-8047764";
        }
        if (
            !"1.8.0_05-b13".equals(ver) &&
            !"1.8.0_11-b12".equals(ver) 
        ) {
            // we know that 1.8.0_05 and 1.8.0_11 are broken, 
            // let's not speculate about anything else
            return null;
        }
        switch (methodName) {
            case "paintTheGridOnClick":
            case "displayContentOfArrayOfPeople":
            case "connectUsingWebSocket":
            case "selectWorksOnModels":
            case "archetypeArrayModificationVisible":
                return "Does not work on JDK8, due to JDK-8046013";
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
