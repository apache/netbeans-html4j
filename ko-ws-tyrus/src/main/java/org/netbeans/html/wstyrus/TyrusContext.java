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
package org.netbeans.html.wstyrus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import net.java.html.json.OnReceive;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.spi.WSTransfer;
import org.netbeans.html.wstyrus.TyrusContext.Comm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openide.util.lookup.ServiceProvider;

/** This is an implementation module that provides support for
 * WebSocket protocol for {@link OnReceive} communication end point for
 * JDK7.
 * <p>
 * Don't deal with this module directly, rather use the 
 * {@link OnReceive @OnReceive(url="ws://...", ...)} API to establish your
 * WebSocket connection.
 * <p>
 * There is no need to include this module in your application if you are
 * running on JDK8. JDK8 WebView provides its own implementation of the
 * WebSocket API based on WebSocket object inside a browser. This is included
 * in the <code>org.netbeans.html:ko4j:1.0</code> module.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = Contexts.Provider.class)
public final class TyrusContext 
implements Contexts.Provider, WSTransfer<Comm>, Transfer {
    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        // default WebSocket transfer implementation is registered
        // in ko-fx module with 100, provide this one as a fallback only
        context.register(WSTransfer.class, this, 1000);
        context.register(Transfer.class, this, 1000);
    }

    @Override
    public Comm open(String url, JSONCall callback) {
        try {
            return new Comm(new URI(url), callback);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void send(Comm socket, JSONCall data) {
        socket.session.getAsyncRemote().sendText(data.getMessage());
    }

    @Override
    public void close(Comm socket) {
        try {
            final Session s = socket.session;
            if (s != null) {
                s.close();
            }
        } catch (IOException ex) {
            socket.callback.notifyError(ex);
        }
    }

    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        LoadJSON.extractJSON(obj, props, values);
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        return LoadJSON.parse(is);
    }

    @Override
    public void loadJSON(JSONCall call) {
        LoadJSON.loadJSON(call);
    }
    
    /** Implementation class in an implementation. Represents a {@link ClientEndpoint} of the
     * WebSocket channel. You are unlikely to get on hold of it.
     */
    @ClientEndpoint
    public static final class Comm {
        private final JSONCall callback;
        private Session session;

        Comm(final URI url, JSONCall callback) {
            this.callback = callback;
            try {
                final WebSocketContainer c = ContainerProvider.getWebSocketContainer();
                c.connectToServer(Comm.this, url);
            } catch (Exception ex) {
                wasAnError(ex);
            }
        }

        @OnOpen
        public synchronized void open(Session s) {
            this.session = s;
            callback.notifySuccess(null);
        }

        @OnClose
        public void close() {
            this.session = null;
            callback.notifyError(null);
        }

        @OnMessage
        public void message(final String orig, Session s) {
            Object json;
            String data = orig.trim();
            try {
                JSONTokener tok = new JSONTokener(data);
                Object obj = data.startsWith("[") ? new JSONArray(tok) : new JSONObject(tok);
                json = convertToArray(obj);
            } catch (JSONException ex) {
                json = data;
            }
            callback.notifySuccess(json);
        }

        @OnError
        public void wasAnError(Throwable t) {
            callback.notifyError(t);
        }

        static Object convertToArray(Object o) throws JSONException {
            if (o instanceof JSONArray) {
                JSONArray ja = (JSONArray) o;
                Object[] arr = new Object[ja.length()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = convertToArray(ja.get(i));
                }
                return arr;
            } else if (o instanceof JSONObject) {
                JSONObject obj = (JSONObject) o;
                Iterator it = obj.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    obj.put(key, convertToArray(obj.get(key)));
                }
                return obj;
            } else {
                return o;
            }
        }
        
    } // end of Comm
}
