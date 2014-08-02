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
package org.netbeans.html.ko4j;

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

/**
 *
 * @author Jaroslav Tulach
 */
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
            final InputStream is = DynamicHTTP.class.getResourceAsStream("test.html");
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
