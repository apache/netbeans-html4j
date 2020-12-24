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
package org.netbeans.html.boot.impl;


/**
 *
 * @author Jaroslav Tulach
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
                throw new IllegalStateException(
                    """
                    Wrong format of instance callback. Should be: 'inst.@pkg.Class::method(Ljava/lang/Object;)(param)':
                    """ 
                    + body
                );
            }
            String fqn = body.substring(next + 2, colon4);
            String method = body.substring(colon4 + 2, sigBeg);
            String params = body.substring(sigBeg, sigEnd + 1);

            int paramBeg = body.indexOf('(', sigEnd + 1);
            if (paramBeg == -1) {
                throw new IllegalStateException(
                    """
                    Wrong format of instance callback. Should be: 'inst.@pkg.Class::method(Ljava/lang/Object;)(param)':
                    """ 
                    + body
                );
            }
            
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
            int paramBeg = body.indexOf('(', sigEnd + 1);
            if (sigBeg == -1 || sigEnd == -1 || colon4 == -1 || paramBeg == -1) {
                throw new IllegalStateException(
                    """
                    Wrong format of static callback. Should be: '@pkg.Class::staticMethod(Ljava/lang/Object;)(param)':
                    """ 
                    + body
                );
            }
            String fqn = body.substring(next + 1, colon4);
            String method = body.substring(colon4 + 2, sigBeg);
            String params = body.substring(sigBeg, sigEnd + 1);

            
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
            replace(fqn) + "$" + replace(method) + "$" + replace(params);
    }
    
    private static String replace(String orig) {
        return orig.replace("_", "_1").
            replace(";", "_2").
            replace("[", "_3").
            replace('.', '_').replace('/', '_');
    }
}
