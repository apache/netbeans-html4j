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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Supplier;
import net.java.html.boot.BrowserBuilder;
import static org.netbeans.html.presenters.browser.JavaScriptUtilities.closeSoon;
import static org.netbeans.html.presenters.browser.JavaScriptUtilities.setLoaded;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class ServerMimeTypeTest {
    @Test(dataProviderClass = ServerFactories.class, dataProvider = "serverFactories")
    public void checkMimeTypes(String name, Supplier<HttpServer<?,?,?,?>> serverProvider) throws Exception {
        final Thread main = Thread.currentThread();
        final int[] loaded = { 0 };

        Browser server = new Browser(
            "test", new Browser.Config().command("NONE"), serverProvider
        );
        BrowserBuilder builder = BrowserBuilder.newBrowser(server)
            .loadPage("server.html")
            .loadFinished(() -> {
                setLoaded("" + ++loaded[0]);
                closeSoon(5000);
            });
        builder.showAndWait();

        int serverPort = server.server().getPort();
        URL connect = new URL("http://localhost:" + serverPort);
        InputStream is = connect.openStream();
        Assert.assertNotNull(is, "Connection opened");
        byte[] arr = new byte[4096];
        int len = is.read(arr);
        is.close();

        final String page = new String(arr, 0, len, "UTF-8");
        assertTrue(page.contains("<h1>Server</h1>"), "Server page loaded OK:\n" + page);

        String cssType = new URL(connect, "test.css").openConnection().getContentType();
        assertMimeType(cssType, "text/css");

        String jsType = new URL(connect, "test.js").openConnection().getContentType();
        assertMimeType(jsType, "*/javascript");

        String jsMinType = new URL(connect, "test.min.js").openConnection().getContentType();
        assertMimeType(jsMinType, "*/javascript");

        URLConnection conn = new URL(connect, "non-existing.file").openConnection();
        assertTrue(conn instanceof HttpURLConnection, "it is HTTP connection: " + conn);

        HttpURLConnection httpConn = (HttpURLConnection) conn;
        assertEquals(httpConn.getResponseCode(), 404, "Expecting not exist status");

        server.close();
        try {
            HttpURLConnection url =  (HttpURLConnection) connect.openConnection();
            url.setConnectTimeout(3000);
            url.setReadTimeout(3000);
            InputStream unavailable = url.getInputStream();
            fail("Stream can no longer be opened: " + unavailable);
        } catch (IOException ex) {
            // OK
        }
    }

    private void assertMimeType(String type, String exp) {
        int semicolon = type.indexOf(';');
        if (semicolon >= 0) {
            type = type.substring(0, semicolon);
        }
        if (exp.startsWith("*")) {
            assertTrue(type.endsWith(exp.substring(1)), "Expecting " + exp + " but was: " + type);
        } else {
            assertEquals(type, exp);
        }
    }
}
