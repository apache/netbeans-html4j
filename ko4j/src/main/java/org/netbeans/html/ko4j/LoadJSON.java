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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.spi.WSTransfer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LoadJSON implements Transfer, WSTransfer<LoadWS> {
    private LoadJSON() {}
    
    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        extractJSON(obj, props, values);
    }

    @Override
    public void loadJSON(final JSONCall call) {
        if (call.isJSONP()) {
            String me = createJSONP(call);
            loadJSONP(call.composeURL(me), me);
        } else {
            String data = null;
            if (call.isDoOutput()) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    call.writeData(bos);
                    data = new String(bos.toByteArray(), "UTF-8");
                } catch (IOException ex) {
                    call.notifyError(ex);
                }
            }
            loadJSON(call.composeURL(null), call, call.getMethod(), data);
        }
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader r = new InputStreamReader(is);
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return parse(sb.toString());
    }

    @Override
    public LoadWS open(String url, JSONCall callback) {
        return new LoadWS(callback, url);
    }

    @Override
    public void send(LoadWS socket, JSONCall data) {
        socket.send(data);
    }

    @Override
    public void close(LoadWS socket) {
        socket.close();
    }
    
    //
    // implementations
    //
    
    @JavaScriptBody(args = {"object", "property"},
        body
        = "if (property === null) return object;\n"
        + "if (object === null) return null;\n"
        + "var p = object[property]; return p ? p : null;"
    )
    private static Object getProperty(Object object, String property) {
        return null;
    }

    static String createJSONP(JSONCall whenDone) {
        int h = whenDone.hashCode();
        String name;
        for (;;) {
            name = "jsonp" + Integer.toHexString(h);
            if (defineIfUnused(name, whenDone)) {
                return name;
            }
            h++;
        }
    }

    @JavaScriptBody(args = {"name", "done"}, javacall = true, body
        = "if (window[name]) return false;\n "
        + "window[name] = function(data) {\n "
        + "  delete window[name];\n"
        + "  var el = window.document.getElementById(name);\n"
        + "  el.parentNode.removeChild(el);\n"
        + "  done.@org.apidesign.html.json.spi.JSONCall::notifySuccess(Ljava/lang/Object;)(data);\n"
        + "};\n"
        + "return true;\n"
    )
    private static boolean defineIfUnused(String name, JSONCall done) {
        return true;
    }

    @JavaScriptBody(args = {"s"}, body = "return eval('(' + s + ')');")
    static Object parse(String s) {
        return s;
    }

    @JavaScriptBody(args = {"url", "done", "method", "data"}, javacall = true, body = ""
        + "var request = new XMLHttpRequest();\n"
        + "if (!method) method = 'GET';\n"
        + "request.open(method, url, true);\n"
        + "request.setRequestHeader('Content-Type', 'application/json; charset=utf-8');\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (request.readyState !== 4) return;\n"
        + "  var r = request.response || request.responseText;\n"
        + "  try {\n"
        + "    if (request.status >= 400) throw request.status + ': ' + request.statusText;"
        + "    try { r = eval('(' + r + ')'); } catch (ignore) { }"
        + "    done.@org.apidesign.html.json.spi.JSONCall::notifySuccess(Ljava/lang/Object;)(r);\n"
        + "  } catch (error) {;\n"
        + "    @org.netbeans.html.ko4j.LoadJSON::notifyError(Ljava/lang/Object;Ljava/lang/Object;)(done, error);\n"
        + "  }\n"
        + "};\n"
        + "request.onerror = function (e) {\n"
        + "  console.log('error loading :' + url + ' props: ' + Object.getOwnPropertyNames(e));\n"
        + "  @org.netbeans.html.ko4j.LoadJSON::notifyError(Ljava/lang/Object;Ljava/lang/Object;)(done, e);\n"
        + "}\n"
        + "if (data) request.send(data);"
        + "else request.send();"
    )
    static void loadJSON(
        String url, JSONCall done, String method, String data
    ) {
    }
    
    static void notifyError(Object done, Object msg) {
        ((JSONCall)done).notifyError(new Exception(msg.toString()));
    }

    @JavaScriptBody(args = {"url", "jsonp"}, body
        = "var scrpt = window.document.createElement('script');\n "
        + "scrpt.setAttribute('src', url);\n "
        + "scrpt.setAttribute('id', jsonp);\n "
        + "scrpt.setAttribute('type', 'text/javascript');\n "
        + "var body = document.getElementsByTagName('body')[0];\n "
        + "body.appendChild(scrpt);\n"
    )
    static void loadJSONP(String url, String jsonp) {

    }

    static void extractJSON(Object jsonObject, String[] props, Object[] values) {
        for (int i = 0; i < props.length; i++) {
            values[i] = getProperty(jsonObject, props[i]);
        }
    }
    
}
