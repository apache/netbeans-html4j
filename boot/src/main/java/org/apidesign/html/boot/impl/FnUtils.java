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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apidesign.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class FnUtils {

    private FnUtils() {
    }

    public static Fn define(Class<?> caller, String code, String... names) {
        JsClassLoader cl = (JsClassLoader)caller.getClassLoader();
        return cl.defineFn(code, names);
    }

    public static ClassLoader newLoader(final FindResources f, final Fn.Presenter d, ClassLoader parent) {
        return new JsClassLoader(parent) {
            @Override
            protected URL findResource(String name) {
                List<URL> l = res(name, true);
                return l.isEmpty() ? null : l.get(0);
            }
            
            @Override
            protected Enumeration<URL> findResources(String name) {
                return Collections.enumeration(res(name, false));
            }
            
            private List<URL> res(String name, boolean oneIsEnough) {
                List<URL> l = new ArrayList<URL>();
                f.findResources(name, l, oneIsEnough);
                return l;
            }
            
            @Override
            protected Fn defineFn(String code, String... names) {
                return d.defineFn(code, names);
            }
        };
    }

    static String callback(String body, ClassLoader loader) {
        try {
            return callbackImpl(body, loader);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Can't parse " + body, ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Can't parse " + body, ex);
        }
    }
    
    private static String callbackImpl(String body, ClassLoader loader)
    throws ClassNotFoundException, NoSuchMethodException {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (;;) {
            int next = body.indexOf(".@", pos);
            if (next == -1) {
                sb.append(body.substring(pos));
                return sb.toString();
            }
            sb.append(body.substring(pos, next));
            
            int sigBeg = body.indexOf('(', next);
            int sigEnd = body.indexOf(')', sigBeg);
            
            int colon4 = body.indexOf("::", next);
            
            if (sigBeg == -1 || sigEnd == -1 || colon4 == -1) {
                throw new IllegalStateException("Malformed body " + body);
            }
            
            String fqn = body.substring(next + 2, colon4);
            String method = body.substring(colon4 + 2, sigBeg);
            String params = body.substring(sigBeg + 1, sigEnd);
            
            Class<?> clazz = Class.forName(fqn, false, loader);
            Method m = clazz.getMethod(method);
            
            sb.append("['").append(m.getName()).append("(");
            String sep = "";
            for (Class<?> pt : m.getParameterTypes()) {
                sb.append(sep).append(pt.getName());
                sep = ",";
            }
            sb.append(")']");
            
            pos = sigEnd + 1;
        }
    }

    
}
