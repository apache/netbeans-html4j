/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.wstyrus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import net.java.html.json.OnReceive;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.WSTransfer;
import org.apidesign.html.wstyrus.TyrusContext.Comm;
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
 * in the <code>org.apidesign.html:ko-fx:0.5</code> module.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = Contexts.Provider.class)
public final class TyrusContext implements Contexts.Provider, WSTransfer<Comm> {
    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        // default WebSocket transfer implementation is registered
        // in ko-fx module with 100, provide this one as a fallback only
        context.register(WSTransfer.class, this, 1000);
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
            } catch (DeploymentException | IOException ex) {
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
