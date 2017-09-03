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
package net.java.html.json.tests;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import net.java.html.BrwsrCtx;
import org.netbeans.html.json.tck.KnockoutTCK;

/**
 *
 * @author Jaroslav Tulach
 */
public final class Utils {
    private static KnockoutTCK instantiatedTCK;

    static boolean skipIfNoFullJDK() {
        try {
            Class<?> thread = Class.forName("java.lang.Thread");
            Thread t = new Thread("Empty");
            t.setName("Different");
            t.setDaemon(false);
            t.interrupt();
            t.start();
        } catch (ClassNotFoundException ex) {
            return true;
        } catch (SecurityException ex) {
            return true;
        }
        return false;
    }

    private Utils() {
    }
    
    public static void registerTCK(KnockoutTCK tck) {
        instantiatedTCK = tck;
    }

    static  BrwsrCtx newContext(Class<?> clazz) {
        for (KnockoutTCK tck : tcks(clazz)) {
            BrwsrCtx c = tck.createContext();
            if (c != null) {
                return c;
            }
        }
        throw new AssertionError("Can't find appropriate Context in ServiceLoader!");
    }
    static Object createObject(Map<String,Object> values, Class<?> clazz) {
        for (KnockoutTCK tck : tcks(clazz)) {
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
        for (KnockoutTCK tck : tcks(clazz)) {
            return tck.executeScript(script, arguments);
        }
        throw new AssertionError("Can't find appropriate Context in ServiceLoader!");
    }

    private static Iterable<KnockoutTCK> tcks(Class<?> clazz) {
        if (instantiatedTCK != null) {
            return Collections.singleton(instantiatedTCK);
        }
        return ServiceLoader.load(KnockoutTCK.class, cl(clazz));
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

    static int countChildren(Class<?> caller, String id) throws Exception {
        return ((Number) executeScript(caller, 
            "var e = window.document.getElementById(arguments[0]);\n" + 
            "if (typeof e === 'undefined') return -2;\n " + 
            "var list = e.childNodes;\n" +
            "var cnt = 0;\n" + 
            "for (var i = 0; i < list.length; i++) {\n" + 
            "  if (list[i].nodeType == 1) cnt++;\n" + 
            "}\n" + 
            "return cnt;\n"
            , id
        )).intValue();
    }

    static Object addChildren(Class<?> caller, String id, String field, Object value) throws Exception {
        return executeScript(caller, 
            "var e = window.document.getElementById(arguments[0]);\n" + 
            "var f = arguments[1];\n" + 
            "var v = arguments[2];\n" + 
            "if (typeof e === 'undefined') return -2;\n " + 
            "var c = ko.contextFor(e);\n" +
            "var fn = c.$rawData[f];\n" +
            "var arr = c.$rawData[f]();\n" +
            "arr.push(v);\n" + 
            "fn(arr);\n" + 
            "return arr;\n"
            , id, field, value
        );
    }
    
    static String prepareURL(
        Class<?> clazz, String content, String mimeType, String... parameters) {
        for (KnockoutTCK tck : tcks(clazz)) {
            URI o = tck.prepareURL(content, mimeType, parameters);
            if (o != null) {
                return o.toString();
            }
        }
        throw new IllegalStateException();
    }

    static boolean canFailWebSockets(
        Class<?> clazz) {
        for (KnockoutTCK tck : tcks(clazz)) {
            if (tck.canFailWebSocketTest()) {
                return true;
            }
        }
        return false;
    }
    
    private static ClassLoader cl(Class<?> c) {
        try {
            return c.getClassLoader();
        } catch (SecurityException ex) {
            return null;
        }
    }
    
    static void fail(String msg) {
        throw new AssertionError(msg);
    }
    
    static void assertTrue(boolean c, String msg) {
        if (!c) {
            throw new AssertionError(msg);
        }
    }

    static void assertFalse(boolean c, String msg) {
        if (c) {
            throw new AssertionError(msg);
        }
    }
    
    static void assertNull(Object o, String msg) {
        if (o != null) {
            throw new AssertionError(msg + " but was: " + o);
        }
    }

    static void assertNotNull(Object o, String msg) {
        if (o == null) {
            throw new AssertionError(msg);
        }
    }
    
    static void assertEquals(Object a, Object b, String msg) {
        if (a == b) {
            return;
        }
        if (a != null && a.equals(b)) {
            return;
        }
        throw new AssertionError(msg + " expecting: " + b + " actual: " + a);
    }
}
