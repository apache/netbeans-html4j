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

import org.netbeans.html.presenters.render.Show;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.java.html.boot.BrowserBuilder;
import static org.netbeans.html.presenters.browser.JavaScriptUtilities.closeSoon;
import static org.netbeans.html.presenters.browser.JavaScriptUtilities.setLoaded;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class ServerTest {
    @Test
    public void useAsAServer() throws Exception {
        final Thread main = Thread.currentThread();
        final int[] loaded = { 0 };

        int serverPort = selectFreePort();

        Browser server = new Browser(
            new Browser.Config()
            .command("NONE")
            .port(serverPort)
        );
        BrowserBuilder builder = BrowserBuilder.newBrowser(server)
            .loadPage("server.html")
            .loadFinished(() -> {
                setLoaded("" + ++loaded[0]);
                closeSoon(5000);
            });
        builder.showAndWait();

        URL connect = new URL("http://localhost:" + serverPort);
        InputStream is = connect.openStream();
        Assert.assertNotNull(is, "Connection opened");
        byte[] arr = new byte[4096];
        int len = is.read(arr);
        is.close();

        final String page = new String(arr, 0, len, "UTF-8");
        assertTrue(page.contains("<h1>Server</h1>"), "Server page loaded OK:\n" + page);

        show(connect.toURI());

        awaitLoaded(1, loaded);
        assertEquals(loaded[0], 1, "Connection has been opened");

        show(connect.toURI());

        awaitLoaded(2, loaded);
        assertEquals(loaded[0], 2, "Second connection has been opened");

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

    private static int selectFreePort() throws IOException {
        ServerSocket temp = new ServerSocket();
        temp.bind(null);
        int port = temp.getLocalPort();
        temp.close();
        return port;
    }

    private static void awaitLoaded(int expected, int[] counter) throws InterruptedException {
        int cnt = 100;
        while (cnt-- > 0 && counter[0] != expected) {
            Thread.sleep(100);
        }
    }

    private static void show(URI page) throws IOException {
        ExecutorService background = Executors.newSingleThreadExecutor();
        Future<Void> future = background.submit((Callable<Void>) () -> {
            IOException one, two;
            Show.show(System.getProperty("com.dukescript.presenters.browser"), page);
            return null;
        });

        try {
            Void ignore = future.get(2, TimeUnit.SECONDS);
            assertNull(ignore);
            background.shutdown();
        } catch (InterruptedException | ExecutionException  ex) {
            throw new AssertionError(ex);
        } catch (TimeoutException ex) {
            // OK
        }
    }
}
