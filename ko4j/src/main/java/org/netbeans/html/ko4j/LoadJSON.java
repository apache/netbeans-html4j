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

/**
 *
 * @author Jaroslav Tulach
 */
final class LoadJSON {
    private LoadJSON() {}

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
        + "  done.@org.netbeans.html.json.spi.JSONCall::notifySuccess(Ljava/lang/Object;)(data);\n"
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

    @JavaScriptBody(args = {"url", "done", "method", "data", "hp"}, javacall = true, body = ""
        + "var request = new XMLHttpRequest();\n"
        + "if (!method) method = 'GET';\n"
        + "request.open(method, url, true);\n"
        + "request.setRequestHeader('Content-Type', 'application/json; charset=utf-8');\n"
        + "for (var i = 0; i < hp.length; i += 2) {\n"
        + "  var h = hp[i];\n"
        + "  var v = hp[i + 1];\n"
        + "  request.setRequestHeader(h, v);\n"
        + "}\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (request.readyState !== 4) return;\n"
        + "  var r = request.response || request.responseText;\n"
        + "  try {\n"
        + "    var str = r;\n"
        + "    if (request.status !== 0)\n"
        + "      if (request.status < 100 || request.status >= 400) throw request.status + ': ' + request.statusText;"
        + "    try { r = eval('(' + r + ')'); } catch (ignore) { }"
        + "    @org.netbeans.html.ko4j.KOTransfer::notifySuccess(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)(done, str, r);\n"
        + "  } catch (error) {;\n"
        + "    @org.netbeans.html.ko4j.KOTransfer::notifyError(Ljava/lang/Object;Ljava/lang/Object;)(done, error);\n"
        + "  }\n"
        + "};\n"
        + "request.onerror = function (e) {\n"
        + "  @org.netbeans.html.ko4j.KOTransfer::notifyError(Ljava/lang/Object;Ljava/lang/Object;)(done, e.type + ' status ' + request.status);\n"
        + "};\n"
        + "if (data) request.send(data);\n"
        + "else request.send();\n"
    )
    static void loadJSON(
        String url, JSONCall done, String method, String data, Object[] headerPairs
    ) {
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
            values[i] = Knockout.getProperty(jsonObject, props[i]);
        }
    }

}
