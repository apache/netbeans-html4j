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

import java.lang.reflect.Method;
import java.util.Map;
import org.objectweb.asm.Type;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class JsCallback {
    private final ClassLoader loader;
    private final String ownName;
    private final Map<String,String> ownMethods;

    JsCallback(ClassLoader l, String ownName, Map<String, String> ownMethods) {
        this.loader = l;
        this.ownName = ownName;
        this.ownMethods = ownMethods;
    }

    String callbackImpl(String body) throws ClassNotFoundException, NoSuchMethodException {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (;;) {
            int next = body.indexOf(".@", pos);
            if (next == -1) {
                sb.append(body.substring(pos));
                return sb.toString();
            }
            int ident = next;
            while (ident > 0 && Character.isJavaIdentifierPart(body.charAt(--ident))) {
            }
            ident++;
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
            
            sb.append(workWithParams(refId, fqn, method, params));
            pos = sigEnd + 1;
        }
    }

    static Class<?> toClass(final Type t, ClassLoader loader) throws ClassNotFoundException {
        if (t == Type.INT_TYPE) {
            return Integer.TYPE;
        } else if (t == Type.VOID_TYPE) {
            return Void.TYPE;
        } else if (t == Type.BOOLEAN_TYPE) {
            return Boolean.TYPE;
        } else if (t == Type.BYTE_TYPE) {
            return Byte.TYPE;
        } else if (t == Type.CHAR_TYPE) {
            return Character.TYPE;
        } else if (t == Type.SHORT_TYPE) {
            return Short.TYPE;
        } else if (t == Type.DOUBLE_TYPE) {
            return Double.TYPE;
        } else if (t == Type.FLOAT_TYPE) {
            return Float.TYPE;
        } else if (t == Type.LONG_TYPE) {
            return Long.TYPE;
        }
        return Class.forName(t.getClassName(), false, loader);
    }

    private CharSequence workWithParams(
        String ident, String fqn, String method, String params
    ) throws NoSuchMethodException, ClassNotFoundException {
        StringBuilder sb = new StringBuilder();
        sb.append(ident);
        if (fqn.equals(ownName.replace('/', '.'))) {
            if (!ownMethods.containsKey(method + params)) {
                throw new IllegalStateException("Wrong reference to " + method + params);
            }
            sb.append("['").append(method).append("(");
            final Type[] argTps = Type.getArgumentTypes(params);
            Class<?>[] argCls = new Class<?>[argTps.length];
            String sep = "";
            for (int i = 0; i < argCls.length; i++) {
                sb.append(sep).append(toClass(argTps[i], loader).getName());
                sep = ",";
            }
            sb.append(")']");
        } else {
            Class<?> clazz = Class.forName(fqn, false, loader);
            final Type[] argTps = Type.getArgumentTypes(params);
            Class<?>[] argCls = new Class<?>[argTps.length];
            for (int i = 0; i < argCls.length; i++) {
                argCls[i] = toClass(argTps[i], loader);
            }
            Method m = clazz.getMethod(method, argCls);
            sb.append("['").append(m.getName()).append("(");
            String sep = "";
            for (Class<?> pt : m.getParameterTypes()) {
                sb.append(sep).append(pt.getName());
                sep = ",";
            }
            sb.append(")']");
        }
        return sb;
    }
    
}
