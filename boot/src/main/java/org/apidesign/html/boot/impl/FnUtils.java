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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.apidesign.html.boot.spi.Fn;
import org.objectweb.asm.Type;

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

            @Override
            protected void loadScript(Reader code) throws Exception {
                d.loadScript(code);
            }
        };
    }

    static String callback(final String body, final ClassLoader loader, final String ownName, final Map<String,String> ownMethods) {
        return new JsCallback() {
            @Override
            protected CharSequence callMethod(
                String ident, String fqn, String method, String params
            ) {
                try {
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
                } catch (ClassNotFoundException ex) {
                    throw new IllegalStateException("Can't parse " + body, ex);
                } catch (NoSuchMethodException ex) {
                    throw new IllegalStateException("Can't parse " + body, ex);
                }
            }

        }.parse(body);
    }

    static void loadScript(JsClassLoader jcl, String resource) {
        final InputStream script = jcl.getResourceAsStream(resource);
        if (script == null) {
            throw new NullPointerException("Can't find " + resource);
        }
        try {
            Reader isr = null;
            try {
                isr = new InputStreamReader(script, "UTF-8");
                jcl.loadScript(isr);
            } finally {
                if (isr != null) {
                    isr.close();
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Can't execute " + resource, ex);
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
}
