/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package org.netbeans.html.kofx;

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
    private static final boolean SUPPORTED = isWebSocket();
    private final Object ws;
    private final JSONCall call;

    LoadWS(JSONCall first, String url) {
        call = first;
        ws = initWebSocket(this, url);
        if (ws == null) {
            first.notifyError(new IllegalArgumentException("Wrong URL: " + url));
        }
    }
    
    static boolean isSupported() {
        return SUPPORTED;
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
    
    @JavaScriptBody(args = {}, body = "if (window.WebSocket) return true; else return false;")
    private static boolean isWebSocket() {
        return false;
    }

    @JavaScriptBody(args = { "back", "url" }, javacall = true, body = ""
        + "if (window.WebSocket) {"
        + "  try {"
        + "    var ws = new window.WebSocket(url);"
        + "    ws.onopen = function(ev) { back.@org.netbeans.html.kofx.LoadWS::onOpen(Ljava/lang/Object;)(ev); };"
        + "    ws.onmessage = function(ev) { back.@org.netbeans.html.kofx.LoadWS::onMessage(Ljava/lang/Object;Ljava/lang/String;)(ev, ev.data); };"
        + "    ws.onerror = function(ev) { back.@org.netbeans.html.kofx.LoadWS::onError(Ljava/lang/Object;)(ev); };"
        + "    ws.onclose = function(ev) { back.@org.netbeans.html.kofx.LoadWS::onClose(ZILjava/lang/String;)(ev.wasClean, ev.code, ev.reason); };"
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

    void close() {
        close(ws);
    }
}
