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
package com.dukescript.presenters.webkit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

final class DynamicHTTP extends HttpHandler {
    private static final Logger LOG = Logger.getLogger(DynamicHTTP.class.getName());
    private static int resourcesCount;
    private static List<Resource> resources;
    private static ServerConfiguration conf;
    private static HttpServer server;
    
    private DynamicHTTP() {
    }
    
    static URI initServer() throws Exception {
        server = HttpServer.createSimpleServer(null, new PortRange(8080, 65535));
        final WebSocketAddOn addon = new WebSocketAddOn();
        for (NetworkListener listener : server.getListeners()) {
            listener.registerAddOn(addon);
        }        
        resources = new ArrayList<Resource>();

        conf = server.getServerConfiguration();
        final DynamicHTTP dh = new DynamicHTTP();

        conf.addHttpHandler(dh, "/");
        
        server.start();

        return pageURL("http", server, "/test.html");
    }
    
    @Override
    public void service(Request request, Response response) throws Exception {
        if ("/test.html".equals(request.getRequestURI())) {
            response.setContentType("text/html");
            final InputStream is = new ByteArrayInputStream((
                "<html><body>" +
                "<h1>GTK harness</h1>" +
                "<script></script>" +
                "</body></html>"
            ).getBytes("UTF-8"));
            copyStream(is, response.getOutputStream(), null);
            return;
        }
        if ("/dynamic".equals(request.getRequestURI())) {
            String mimeType = request.getParameter("mimeType");
            List<String> params = new ArrayList<String>();
            boolean webSocket = false;
            for (int i = 0;; i++) {
                String p = request.getParameter("param" + i);
                if (p == null) {
                    break;
                }
                if ("protocol:ws".equals(p)) {
                    webSocket = true;
                    continue;
                }
                params.add(p);
            }
            final String cnt = request.getParameter("content");
            String mangle = cnt.replace("%20", " ").replace("%0A", "\n");
            ByteArrayInputStream is = new ByteArrayInputStream(mangle.getBytes("UTF-8"));
            URI url;
            final Resource res = new Resource(is, mimeType, "/dynamic/res" + ++resourcesCount, params.toArray(new String[params.size()]));
            if (webSocket) {
                url = registerWebSocket(res);
            } else {
                url = registerResource(res);
            }
            response.getWriter().write(url.toString());
            response.getWriter().write("\n");
            return;
        }

        for (Resource r : resources) {
            if (r.httpPath.equals(request.getRequestURI())) {
                response.setContentType(r.httpType);
                r.httpContent.reset();
                String[] params = null;
                if (r.parameters.length != 0) {
                    params = new String[r.parameters.length];
                    for (int i = 0; i < r.parameters.length; i++) {
                        params[i] = request.getParameter(r.parameters[i]);
                        if (params[i] == null) {
                            if ("http.method".equals(r.parameters[i])) {
                                params[i] = request.getMethod().toString();
                            } else if ("http.requestBody".equals(r.parameters[i])) {
                                Reader rdr = request.getReader();
                                StringBuilder sb = new StringBuilder();
                                for (;;) {
                                    int ch = rdr.read();
                                    if (ch == -1) {
                                        break;
                                    }
                                    sb.append((char) ch);
                                }
                                params[i] = sb.toString();
                            } else if (r.parameters[i].startsWith("http.header.")) {
                                params[i] = request.getHeader(r.parameters[i].substring(12));
                            }
                        }
                        if (params[i] == null) {
                            params[i] = "null";
                        }
                    }
                }

                copyStream(r.httpContent, response.getOutputStream(), null, params);
            }
        }
    }
    
    private URI registerWebSocket(Resource r) {
        WebSocketEngine.getEngine().register("", r.httpPath, new WS(r));
        return pageURL("ws", server, r.httpPath);
    }

    private URI registerResource(Resource r) {
        if (!resources.contains(r)) {
            resources.add(r);
            conf.addHttpHandler(this, r.httpPath);
        }
        return pageURL("http", server, r.httpPath);
    }
    
    private static URI pageURL(String proto, HttpServer server, final String page) {
        NetworkListener listener = server.getListeners().iterator().next();
        int port = listener.getPort();
        try {
            return new URI(proto + "://localhost:" + port + page);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    static final class Resource {

        final InputStream httpContent;
        final String httpType;
        final String httpPath;
        final String[] parameters;

        Resource(InputStream httpContent, String httpType, String httpPath,
            String[] parameters) {
            httpContent.mark(Integer.MAX_VALUE);
            this.httpContent = httpContent;
            this.httpType = httpType;
            this.httpPath = httpPath;
            this.parameters = parameters;
        }
    }

    static void copyStream(InputStream is, OutputStream os, String baseURL, String... params) throws IOException {
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            if (ch == '$' && params.length > 0) {
                int cnt = is.read() - '0';
                if (baseURL != null && cnt == 'U' - '0') {
                    os.write(baseURL.getBytes("UTF-8"));
                } else {
                    if (cnt >= 0 && cnt < params.length) {
                        os.write(params[cnt].getBytes("UTF-8"));
                    } else {
                        os.write('$');
                        os.write(cnt + '0');
                    }
                }
            } else {
                os.write(ch);
            }
        }
    }
    
    private static class WS extends WebSocketApplication {
        private final Resource r;

        private WS(Resource r) {
            this.r = r;
        }

        @Override
        public void onMessage(WebSocket socket, String text) {
            try {
                r.httpContent.reset();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copyStream(r.httpContent, out, null, text);
                String s = new String(out.toByteArray(), "UTF-8");
                socket.send(s);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error processing message " + text, ex);
            }
        }
    }
}
