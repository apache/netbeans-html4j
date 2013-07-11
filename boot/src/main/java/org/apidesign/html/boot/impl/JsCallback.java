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
package org.apidesign.html.boot.impl;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
abstract class JsCallback {
    final String parse(String body) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (;;) {
            int next = body.indexOf(".@", pos);
            if (next == -1) {
                sb.append(body.substring(pos));
                body = sb.toString();
                break;
            }
            int ident = next;
            while (ident > 0) {
                if (!Character.isJavaIdentifierPart(body.charAt(--ident))) {
                    ident++;
                    break;
                }
            }
            String refId = body.substring(ident, next);
            
            sb.append(body.substring(pos, ident));
            
            int sigBeg = body.indexOf('(', next);
            int sigEnd = body.indexOf(')', sigBeg);
            int colon4 = body.indexOf("::", next);
            if (sigBeg == -1 || sigEnd == -1 || colon4 == -1) {
                throw new IllegalStateException("Malformed body " + body);
            }
            String fqn = body.substring(next + 2, colon4);
            String method = body.substring(colon4 + 2, sigBeg);
            String params = body.substring(sigBeg, sigEnd + 1);

            int paramBeg = body.indexOf('(', sigEnd + 1);
            
            sb.append(callMethod(refId, fqn, method, params));
            if (body.charAt(paramBeg + 1) != (')')) {
                sb.append(",");
            }
            pos = paramBeg + 1;
        }
        pos = 0;
        sb = null;
        for (;;) {
            int next = body.indexOf("@", pos);
            if (next == -1) {
                if (sb == null) {
                    return body;
                }
                sb.append(body.substring(pos));
                return sb.toString();
            }
            if (sb == null) {
                sb = new StringBuilder();
            }
            
            sb.append(body.substring(pos, next));
            
            int sigBeg = body.indexOf('(', next);
            int sigEnd = body.indexOf(')', sigBeg);
            int colon4 = body.indexOf("::", next);
            if (sigBeg == -1 || sigEnd == -1 || colon4 == -1) {
                throw new IllegalStateException("Malformed body " + body);
            }
            String fqn = body.substring(next + 1, colon4);
            String method = body.substring(colon4 + 2, sigBeg);
            String params = body.substring(sigBeg, sigEnd + 1);

            int paramBeg = body.indexOf('(', sigEnd + 1);
            
            sb.append(callMethod(null, fqn, method, params));
            pos = paramBeg + 1;
        }
    }

    protected abstract CharSequence callMethod(
        String ident, String fqn, String method, String params
    );

    static String mangle(String fqn, String method, String params) {
        if (params.startsWith("(")) {
            params = params.substring(1);
        }
        if (params.endsWith(")")) {
            params = params.substring(0, params.length() - 1);
        }
        return 
            replace(fqn) + "__" + replace(method) + "__" + replace(params);
    }
    
    private static String replace(String orig) {
        return orig.replace("_", "_1").
            replace(";", "_2").
            replace("[", "_3").
            replace('.', '_').replace('/', '_');
    }
}
