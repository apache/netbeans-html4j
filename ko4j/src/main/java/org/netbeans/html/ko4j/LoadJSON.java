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

import net.java.html.js.JavaScriptBody;
import org.netbeans.html.json.spi.JSONCall;

/**
 *
 * @author Jaroslav Tulach
 */
final class LoadJSON {
    private LoadJSON() {}

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
            values[i] = getProperty(jsonObject, props[i]);
        }
    }

}
