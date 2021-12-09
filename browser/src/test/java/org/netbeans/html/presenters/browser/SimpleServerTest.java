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
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class SimpleServerTest {
    public SimpleServerTest() {
    }

    @Test(dataProviderClass = ServerFactories.class, dataProvider = "serverFactories")
    public void testConnectionToTheServer(String name, Supplier<HttpServer<?,?,?,?>> serverProvider) throws IOException {
        if (serverProvider == null) {
            return;
        }
        int min = 42343;
        int max = 49343;
        HttpServer<?, ?, ?, ?> server = serverProvider.get();
        server.init(min, max);
        server.addHttpHandler(new HttpServer.Handler() {
            @Override
            <Request, Response> void service(HttpServer<Request, Response, ?, ?> server, Request rqst, Response rspns) throws IOException {
                assertEquals(server.getServerName(rqst), "localhost", "Connecting from localhost");
                assertEquals(server.getServerPort(rqst), server.getPort(), "Connecting via local port");
                assertEquals(server.getMethod(rqst), "GET", "Requesting GET");

                server.setCharacterEncoding(rspns, "UTF-8");
                server.setContentType(rspns, "text/x-test");
                Browser.cors(server, rspns);
                try (Writer w = server.getWriter(rspns)) {
                    final String n = server.getParameter(rqst, "name");
                    final String reply;
                    switch (server.getRequestURI(rqst)) {
                        case "/reply/hi": reply = "Ahoj " + n + "!"; break;
                        case "/reply/tchus": reply = "Ciao " + n + "!"; break;
                        default: reply = "What?";
                    }
                    w.write(reply);
                }
            }
        }, "/reply");
        server.start();

        int realPort = server.getPort();
        assertTrue(realPort <= max && realPort >= min, "Port from range (" + min + ", " + max + ") selected: " + realPort);

        final String baseUri = "http://localhost:" + realPort;
        assertURL("Ahoj John!", baseUri, "/reply/hi?name=John");
        assertURL("Ciao John!", baseUri, "/reply/tchus?name=John");
        assertURL("Ahoj Bogus!", baseUri, "/reply/hi?name=Bogus&fsdklre");

        server.shutdownNow();

    }

    private static void assertURL(String msg, String baseUri, final String path) throws IOException, MalformedURLException {
        URL url = new URL(baseUri + path);
        URLConnection conn = url.openConnection();

        final String contentAndAttribs = conn.getContentType();
        assertNotNull(contentAndAttribs, "Content-Type specified");
        int semicolon = contentAndAttribs.indexOf(';');
        final String content = semicolon == -1 ? contentAndAttribs : contentAndAttribs.substring(0, semicolon);
        assertEquals(content, "text/x-test");

        byte[] arr = new byte[8192];
        int len = conn.getInputStream().read(arr);
        assertNotEquals(len, -1, "Something shall be read");

        String txt = new String(arr, 0, len, StandardCharsets.UTF_8);
        assertEquals(txt, msg, "Message from the handler delivered");

        assertEquals(conn.getHeaderField("Access-Control-Allow-Origin"), "*");
    }

    @Test(dataProviderClass = ServerFactories.class, dataProvider = "serverFactories")
    public void testHeadersAndBody(String name, Supplier<HttpServer<?,?,?,?>> serverProvider) throws IOException {
        if (serverProvider == null) {
            return;
        }
        int min = 42343;
        int max = 49343;
        HttpServer<?, ?, ?, ?> server = serverProvider.get();
        server.init(min, max);
        server.addHttpHandler(new HttpServer.Handler() {
            @Override
            <Request, Response> void service(HttpServer<Request, Response, ?, ?> server, Request rqst, Response rspns) throws IOException {
                StringBuilder sb = new StringBuilder(server.getBody(rqst));

                server.setCharacterEncoding(rspns, "UTF-8");
                server.setContentType(rspns, "text/plain");
                try (Writer w = server.getWriter(rspns)) {
                    final String action = server.getHeader(rqst, "action");
                    assertNotNull(action, "action is specified");
                    String reply;
                    switch (action) {
                        case "reverse": reply = sb.reverse().toString(); break;
                        case "upper": reply = sb.toString().toUpperCase(); break;
                        default: reply = "What?";
                    }
                    w.write(reply);
                }
            }
        }, "/action");
        server.start();

        int realPort = server.getPort();
        assertTrue(realPort <= max && realPort >= min, "Port from range (" + min + ", " + max + ") selected: " + realPort);

        final String baseUri = "http://localhost:" + realPort;
        assertReadURL("reverse", "Ahoj", baseUri, "johA");
        assertReadURL("upper", "Ahoj", baseUri, "AHOJ");

        server.shutdownNow();

    }

    private static void assertReadURL(String action, String data, String baseUri, final String exp) throws IOException, MalformedURLException {
        URL url = new URL(baseUri + "/action");
        URLConnection conn = url.openConnection();
        conn.addRequestProperty("action", action);
        conn.setDoOutput(true);
        conn.connect();
        conn.getOutputStream().write(data.getBytes());


        final String contentAndAttribs = conn.getContentType();
        assertNotNull(contentAndAttribs, "Content-Type specified");
        int semicolon = contentAndAttribs.indexOf(';');
        final String content = semicolon == -1 ? contentAndAttribs : contentAndAttribs.substring(0, semicolon);
        assertEquals(content, "text/plain");

        byte[] arr = new byte[8192 * 8];
        int offset = 0;
        for (;;) {
            int len = conn.getInputStream().read(arr, offset, arr.length - offset);
            if (len == -1) {
                break;
            }
            offset += len;
        }
        assertNotEquals(offset, 0, "Something shall be read");

        String txt = new String(arr, 0, offset, StandardCharsets.UTF_8);
        assertEquals(txt, exp, "Message from the handler delivered");
    }

    @Test(dataProviderClass = ServerFactories.class, dataProvider = "serverFactories")
    public void testWaitForData(String name, Supplier<HttpServer<?,?,?,?>> serverProvider) throws IOException {
        if (serverProvider == null) {
            return;
        }
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        int min = 32343;
        int max = 33343;
        HttpServer<?, ?, ?, ?> server = serverProvider.get();
        server.init(min, max);

        class HandlerImpl extends HttpServer.Handler {
            @Override
            <Request, Response> void service(HttpServer<Request, Response, ?, ?> server, Request rqst, Response rspns) throws IOException {
                server.setCharacterEncoding(rspns, "UTF-8");
                server.setContentType(rspns, "text/x-test");
                Browser.cors(server, rspns);
                server.suspend(rspns);
                exec.schedule((Callable <Void>) () -> {
                    server.resume(rspns, () -> {
                        Writer w = server.getWriter(rspns);
                        try {
                            w.write("Finished!");
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    });
                    return null;
                }, 1, TimeUnit.SECONDS);
            }
        }
        server.addHttpHandler(new HandlerImpl(), "/async");
        server.start();

        int realPort = server.getPort();
        assertTrue(realPort <= max && realPort >= min, "Port from range (" + min + ", " + max + ") selected: " + realPort);

        final String baseUri = "http://localhost:" + realPort;
        assertURL("Finished!", baseUri, "/async");

        exec.shutdown();
        server.shutdownNow();
    }

    @Test(dataProviderClass = ServerFactories.class, dataProvider = "serverFactories")
    public void testEnormousBody(String name, Supplier<HttpServer<?,?,?, ?>> serverProvider) throws IOException {
        if (serverProvider == null) {
            return;
        }
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        int min = 32343;
        int max = 33343;
        HttpServer<?, ?, ?, ?> server = serverProvider.get();
        server.init(min, max);

        String id = veryLongId();

        class HandlerImpl extends HttpServer.Handler {
            @Override
            <Request, Response> void service(HttpServer<Request, Response, ?, ?> server, Request rqst, Response rspns) throws IOException {
                server.setCharacterEncoding(rspns, "UTF-8");
                server.setContentType(rspns, "text/plain");
                Browser.cors(server, rspns);

                assertEquals("lower", server.getHeader(rqst, "action"));

                String gotId = server.getBody(rqst);
                if (!gotId.equals(id)) {
                    fail("Id as expected by " + server + " isn't the same " + id.length() + " != " + gotId.length());
                }
                server.getWriter(rspns).write(gotId.toLowerCase());
            }
        }
        server.addHttpHandler(new HandlerImpl(), "/action");
        server.start();

        int realPort = server.getPort();
        assertTrue(realPort <= max && realPort >= min, "Port from range (" + min + ", " + max + ") selected: " + realPort);

        final String baseUri = "http://localhost:" + realPort;
        assertReadURL("lower", id, baseUri, id.toLowerCase());

        exec.shutdown();
        server.shutdownNow();
    }

    private static String veryLongId() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            final int max = 'Z' - 'A';
            int ch = 'A' + (i % max);
            sb.append((char) ch);
        }
        return sb.toString();
    }
}
