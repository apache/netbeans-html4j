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
package org.netbeans.html.ko4j;

import net.java.html.js.JavaScriptBody;
import org.netbeans.html.json.spi.JSONCall;

/** Communication with WebSockets via browser's WebSocket object.
 *
 * @author Jaroslav Tulach
 */
final class LoadWS {
    private final Object ws;
    private final JSONCall call;
    LoadWS(JSONCall first, String url) {
        call = first;
        ws = initWebSocket(this, url);
        if (ws == null) {
            first.notifyError(new IllegalArgumentException("Wrong URL: " + url));
        }
    }
    
    void send(JSONCall call) {
        push(call);
    }
    
    private synchronized void push(JSONCall call) {
        send(ws, call.getMessage());
    }

    void onOpen(Object ev) {
        if (!call.isDoOutput()) {
            call.notifySuccess(null);
        }
    }
    
    
    @JavaScriptBody(args = { "data" }, body = """
        try {
            return eval('(' + data + ')');
        } catch (error) {;
            return data;
        }
        """
    )
    private static native Object toJSON(String data);
    
    void onMessage(Object ev, String data) {
        Object json = toJSON(data);
        call.notifySuccess(json);
    }
    
    void onError(Object ev) {
        call.notifyError(new Exception(ev.toString()));
    }

    void onClose(boolean wasClean, int code, String reason) {
        call.notifyError(null);
    }
    
    @JavaScriptBody(args = { "back", "url" }, javacall = true, body = """
        if (window.WebSocket) {
          try {
            var ws = new window.WebSocket(url);
            ws.onopen = function(ev) {
              back.@org.netbeans.html.ko4j.LoadWS::onOpen(Ljava/lang/Object;)(ev);
            };
            ws.onmessage = function(ev) {
              back.@org.netbeans.html.ko4j.LoadWS::onMessage(Ljava/lang/Object;Ljava/lang/String;)(ev, ev.data);
            };
            ws.onerror = function(ev) {
              back.@org.netbeans.html.ko4j.LoadWS::onError(Ljava/lang/Object;)(ev);
            };
            ws.onclose = function(ev) {
              back.@org.netbeans.html.ko4j.LoadWS::onClose(ZILjava/lang/String;)(ev.wasClean, ev.code, ev.reason);
            };
            return ws;
          } catch (ex) {
            return null;
          }
        } else {
          return null;
        }
        """
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

    void close() {
        close(ws);
    }
}
