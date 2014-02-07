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
package org.netbeans.html.boot.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class JsPkgCache {
    private final Map<String,Set<String>> props = new WeakHashMap<String, Set<String>>();
    private static final Map<ClassLoader, JsPkgCache> CACHE = new WeakHashMap<ClassLoader, JsPkgCache>();
    private static final Set<String> NONE = Collections.emptySet();

    public static boolean process(ClassLoader l, String className) {
        if (className.equals("org.netbeans.html.boot.impl.Test")) { // NOI18N
            return true;
        }
        Set<String> p;
        JsPkgCache c;
        String pkgName;
        synchronized (CACHE) {
            c = CACHE.get(l);
            if (c == null) {
                c = new JsPkgCache();
                CACHE.put(l, c);
            }
            int lastDot = className.lastIndexOf('.');
            pkgName = className.substring(0, lastDot + 1).replace('.', '/');
            p = c.props.get(pkgName);
            if (p == NONE) {
                return false;
            } else if (p != null) {
                return p.contains(className);
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
            Set<String> arr = new TreeSet<String>();
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
            return p.contains(className);
        }
        
    }
    private static final Logger LOG = Logger.getLogger(JsPkgCache.class.getName());
}
