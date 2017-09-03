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
@Contexts.Id("tyrus")
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
