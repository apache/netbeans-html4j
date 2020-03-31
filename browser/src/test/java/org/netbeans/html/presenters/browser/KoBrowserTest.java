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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import net.java.html.BrwsrCtx;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.tck.KOTest;
import org.netbeans.html.json.tck.KnockoutTCK;
import org.netbeans.html.ko4j.KO4J;
import org.openide.util.lookup.ServiceProvider;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Factory;

@ServiceProvider(service = KnockoutTCK.class)
public class KoBrowserTest extends KnockoutTCK {
    public KoBrowserTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        Browser.LOG.setLevel(Level.FINE);
        Browser.LOG.addHandler(new ConsoleHandler());

        List<Object> res = new ArrayList<>();
        Fn.Presenter[] all = ServerFactories.collect("KoBrowserTest", res, KOTest.class, KnockoutTCK::testClasses);
        for (Fn.Presenter browserPresenter : all) {
            final HttpServer s = Browser.findServer(browserPresenter);
            s.addHttpHandler(new DynamicHTTP(s), "/dynamic");
        }
        return res.toArray();
    }
    
    @Override
    public BrwsrCtx createContext() {
        KO4J ko = new KO4J();
        Contexts.Builder b = Contexts.newBuilder();
        b.register(Technology.class, ko.knockout(), 7);
        b.register(Transfer.class, ko.transfer(), 7);
        Fn.Presenter browserPresenter = Fn.activePresenter();
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

    private Object putValue(Object json, String key, Object value) {
        Fn jsonFn = Fn.activePresenter().defineFn(
            "if (json === null) json = new Object();"
            + "if (key !== null) json[key] = value;"
            + "return json;",
            "json", "key", "value"
        );
        try {
            return jsonFn.invoke(null, json, key, value);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public Object executeScript(String script, Object[] arguments) {
        Fn executeScript = Fn.activePresenter().defineFn(
            "var f = new Function(s); "
            + "return f.apply(null, args);",
            "s", "args"
        );
        try {
            return executeScript.invoke(null, script, arguments);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public URI prepareURL(String content, String mimeType, String[] parameters) {
        try {
            final URL baseURL = new URL(JavaScriptUtilities.findBaseURL());
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
        } catch (IOException | URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
