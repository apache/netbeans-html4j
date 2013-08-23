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
package org.apidesign.html.kofx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.json.spi.JSONCall;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/** Communication with WebSockets for WebView 1.8.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LoadWS {
    private static final Map<String,LoadWS> CONNECTIONS = new HashMap<String, LoadWS>();
    private final Object ws;
    private Deque<JSONCall> pending = new ArrayDeque<JSONCall>();
    private final JSONCall call;

    private LoadWS(JSONCall first, String url) {
        call = first;
        ws = initWebSocket(this, url);
    }
    
    static void send(JSONCall call) {
        String url = call.composeURL(null);
        LoadWS load = CONNECTIONS.get(url);
        if (load == null) {
            load = new LoadWS(call, url);
            if (load.ws != null) {
                CONNECTIONS.put(url, load);
            } else {
                call.notifyError(new UnsupportedOperationException("WebSocket API is not supported"));
                return;
            }
        } else {
            if (!call.isDoOutput()) {
                close(load.ws);
            }
        }
        if (call.isDoOutput()) {
            load.push(call);
        }
    }
    
    private synchronized void push(JSONCall call) {
        if (pending != null) {
            pending.add(call);
        } else {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                call.writeData(os);
                String msg = new String(os.toByteArray(), "UTF-8");
                send(ws, msg);
            } catch (IOException ex) {
                call.notifyError(ex);
            }
        }
    }

    void onOpen(Object ev) {
        Deque<JSONCall> p;
        synchronized (this) {
            p = pending;
            pending = null;
        }
        if (!call.isDoOutput()) {
            call.notifySuccess(null);
        }
        for (JSONCall c : p) {
            push(c);
        }
    }
    
    void onMessage(Object ev, String data) {
        Object json;
        try {
            data = data.trim();
            
            JSONTokener tok = new JSONTokener(data);
            Object obj;
            obj = data.startsWith("[") ? new JSONArray(tok) : new JSONObject(tok);
            json = LoadJSON.convertToArray(obj);
        } catch (JSONException ex) {
            json = data;
        }
        call.notifySuccess(json);
    }
    
    void onError(Object ev) {
        call.notifyError(new Exception(ev.toString()));
    }

    void onClose(boolean wasClean, int code, String reason) {
        call.notifyError(null);
    }

    @JavaScriptBody(args = { "back", "url" }, javacall = true, body = ""
        + "if (window.WebSocket) {"
        + "  try {"
        + "    var ws = new window.WebSocket(url);"
        + "    ws.onopen = function(ev) { back.@org.apidesign.html.kofx.LoadWS::onOpen(Ljava/lang/Object;)(ev); };"
        + "    ws.onmessage = function(ev) { back.@org.apidesign.html.kofx.LoadWS::onMessage(Ljava/lang/Object;Ljava/lang/String;)(ev, ev.data); };"
        + "    ws.onerror = function(ev) { back.@org.apidesign.html.kofx.LoadWS::onError(Ljava/lang/Object;)(ev); };"
        + "    ws.onclose = function(ev) { back.@org.apidesign.html.kofx.LoadWS::onClose(ZILjava/lang/String;)(ev.wasClean, ev.code, ev.reason); };"
        + "    return ws;"
        + "  } catch (ex) {"
        + "    return null;"
        + "  }"
        + "} else {"
        + "  return null;"
        + "}"
    )
    private static Object initWebSocket(Object back, String url) {
        return null;
    }
    

    @JavaScriptBody(args = { "ws", "msg" }, body = ""
        + "ws.send(msg);"
    )
    private void send(Object ws, String msg) {
    }

    @JavaScriptBody(args = { "ws" }, body = "ws.close();")
    private static void close(Object ws) {
    }
}
