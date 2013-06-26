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
package net.java.html.json.tests;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.ServiceLoader;
import net.java.html.BrwsrCtx;
import org.apidesign.html.json.tck.KnockoutTCK;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Utils {
    private Utils() {
    }

    static  BrwsrCtx newContext(Class<?> clazz) {
        for (KnockoutTCK tck : ServiceLoader.load(KnockoutTCK.class, cl(clazz))) {
            BrwsrCtx c = tck.createContext();
            if (c != null) {
                return c;
            }
        }
        throw new AssertionError("Can't find appropriate Context in ServiceLoader!");
    }
    static Object createObject(Map<String,Object> values, Class<?> clazz) {
        for (KnockoutTCK tck : ServiceLoader.load(KnockoutTCK.class, cl(clazz))) {
            Object o = tck.createJSON(values);
            if (o != null) {
                return o;
            }
        }
        throw new AssertionError("Can't find appropriate Context in ServiceLoader!");
    }
    static Object executeScript(Class<?> clazz, 
        String script, Object... arguments
    ) throws Exception {
        for (KnockoutTCK tck : ServiceLoader.load(KnockoutTCK.class, cl(clazz))) {
            return tck.executeScript(script, arguments);
        }
        throw new AssertionError("Can't find appropriate Context in ServiceLoader!");
    }
    
    static Object exposeHTML(Class<?> clazz, String html) throws Exception {
        String s = 
          "var n = window.document.getElementById('ko.test.div'); \n "
        + "if (!n) { \n"
        + "  n = window.document.createElement('div'); \n "
        + "  n.id = 'ko.test.div'; \n "
        + "  var body = window.document.getElementsByTagName('body')[0];\n"
        + "  body.appendChild(n);\n"
        + "}\n"
        + "n.innerHTML = arguments[0]; \n ";
        return executeScript(clazz, s, html);
    }

    static String prepareURL(
        Class<?> clazz, String content, String mimeType, String... parameters) {
        for (KnockoutTCK tck : ServiceLoader.load(KnockoutTCK.class, cl(clazz))) {
            URI o = tck.prepareURL(content, mimeType, parameters);
            if (o != null) {
                return o.toString();
            }
        }
        throw new IllegalStateException();
    }
    
    private static ClassLoader cl(Class<?> c) {
        try {
            return c.getClassLoader();
        } catch (SecurityException ex) {
            return null;
        }
    }
}
