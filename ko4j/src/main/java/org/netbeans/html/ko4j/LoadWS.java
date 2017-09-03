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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
    
    
    @JavaScriptBody(args = { "data" }, body = "try {\n"
        + "    return eval('(' + data + ')');\n"
        + "  } catch (error) {;\n"
        + "    return data;\n"
        + "  }\n"
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
    
    @JavaScriptBody(args = { "back", "url" }, javacall = true, body = ""
        + "if (window.WebSocket) {\n"
        + "  try {\n"
        + "    var ws = new window.WebSocket(url);\n"
        + "    ws.onopen = function(ev) {\n"
        + "      back.@org.netbeans.html.ko4j.LoadWS::onOpen(Ljava/lang/Object;)(ev);\n"
        + "    };\n"
        + "    ws.onmessage = function(ev) {\n"
        + "      back.@org.netbeans.html.ko4j.LoadWS::onMessage(Ljava/lang/Object;Ljava/lang/String;)(ev, ev.data);\n"
        + "    };\n"
        + "    ws.onerror = function(ev) {\n"
        + "      back.@org.netbeans.html.ko4j.LoadWS::onError(Ljava/lang/Object;)(ev);\n"
        + "    };\n"
        + "    ws.onclose = function(ev) {\n"
        + "      back.@org.netbeans.html.ko4j.LoadWS::onClose(ZILjava/lang/String;)(ev.wasClean, ev.code, ev.reason);\n"
        + "    };\n"
        + "    return ws;\n"
        + "  } catch (ex) {\n"
        + "    return null;\n"
        + "  }\n"
        + "} else {\n"
        + "  return null;\n"
        + "}\n"
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
