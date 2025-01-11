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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jaroslav Tulach
 */
final class JsPkgCache {
    private static final Logger LOG = Logger.getLogger(JsPkgCache.class.getName());
    private final Map<String,Set<String>> props = new WeakHashMap<>();
    private static final Map<ClassLoader, JsPkgCache> CACHE = new WeakHashMap<>();
    private static final Set<String> NONE = Collections.emptySet();

    public static boolean process(ClassLoader loader, String dotOrSlashClassName) {
        if (loader == null) {
            return false;
        }
        var slashClassName = dotOrSlashClassName.replace('.', '/');
        var className = dotOrSlashClassName.replace('/', '.');
        return switch (className) {
            case "net.java.html.js.JavaScriptBody" -> true; // NOI18N
            case "net.java.html.js.JavaScriptResource" -> true; // NOI18N
            case "net.java.html.js.JavaScriptResource$Group" -> true; // NOI18N;
            case "org.netbeans.html.boot.impl.Test" -> true; // NOI18N
            default -> packageCheck(loader, slashClassName, className);
        };
    }

    private static boolean packageCheck(ClassLoader l, String slashClassName, String dotClassName) {
        Set<String> p;
        JsPkgCache c;
        String pkgName;
        synchronized (CACHE) {
            c = CACHE.get(l);
            if (c == null) {
                c = new JsPkgCache();
                CACHE.put(l, c);
            }
            int lastDot = slashClassName.lastIndexOf('/');
            pkgName = slashClassName.substring(0, lastDot + 1);
            p = c.props.get(pkgName);
            if (p == NONE) {
                return false;
            } else if (p != null) {
                return p.contains(dotClassName);
            }
        }
        final String res = pkgName + "net.java.html.js.classes";

        Enumeration<URL> en;
        try {
            en = l.getResources(res);
        } catch (IOException ex) {
            en = null;
        }
        if (en == null || !en.hasMoreElements()) synchronized (CACHE) {
            c.props.put(pkgName, NONE);
            return false;
        }

        try {
            Set<String> arr = new TreeSet<>();
            while (en.hasMoreElements()) {
                URL u = en.nextElement();
                BufferedReader r = new BufferedReader(
                    new InputStreamReader(u.openStream(), "UTF-8")
                );
                for (;;) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    arr.add(line);
                }
                r.close();
            }
            p = arr;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Can't read " + res, ex);
            p = NONE;
        }

        synchronized (CACHE) {
            c.props.put(pkgName, p);
            return p.contains(dotClassName);
        }
    }
}
