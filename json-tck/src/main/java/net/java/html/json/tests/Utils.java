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

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
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

    static void scheduleLater(int delay, final Runnable r) {
        for (KnockoutTCK tck : tcks(r.getClass())) {
            if (tck.scheduleLater(delay, r)) {
                return;
            }
        }
        Timer t = new Timer("Running later");
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        }, delay);
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
            return Models.asList(instantiatedTCK);
        }
        return ServiceLoader.load(KnockoutTCK.class, cl(clazz));
    }

    static void exposeTypeOf(Class<?> clazz) throws Exception {
        String s = """
          var global = 0 || eval('this');
          if (!global['getTypeof']) {
            global['getTypeof'] = function (o) {
              return typeof o;
            };
          }
          """;
        executeScript(clazz, s);
    }

    static Object exposeHTML(Class<?> clazz, String html) throws Exception {
        String s = """
          var n = window.document.getElementById('ko.test.div');
           if (!n) {
            n = window.document.createElement('div');
             n.id = 'ko.test.div';
             var body = window.document.getElementsByTagName('body')[0];
            body.appendChild(n);
          }
          n.innerHTML = arguments[0];
           """;
        return executeScript(clazz, s, html);
    }

    static int countChildren(Class<?> caller, String id) throws Exception {
        return ((Number) executeScript(caller, """
            var e = window.document.getElementById(arguments[0]);
            if (typeof e === 'undefined') return -2;
            var list = e.childNodes;
            var cnt = 0;
            for (var i = 0; i < list.length; i++) {
              if (list[i].nodeType == 1) cnt++;
            }
            return cnt;
            """, id
        )).intValue();
    }

    static Object addChildren(Class<?> caller, String id, String field, Object value) throws Exception {
        return executeScript(caller, """
            var e = window.document.getElementById(arguments[0]);
            var f = arguments[1];
            var v = arguments[2];
            if (typeof e === 'undefined') return -2;
             var c = ko.contextFor(e);
            var fn = c.$rawData[f];
            var arr = c.$rawData[f]();
            arr.push(v);
            fn(arr);
            return arr;
            """, id, field, value
        );
    }

    static void scheduleClick(Class<?> clazz, String id, int delay) throws Exception {
        String s = """
            var id = arguments[0];
            var delay = arguments[1];
            var e = window.document.getElementById(id);
            var f = function() {;
                var ev = window.document.createEvent('MouseEvents');
                ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
                e.dispatchEvent(ev);
            };
            window.setTimeout(f, delay);
            """;
        Utils.executeScript(clazz, s, id, delay);
    }


    static String prepareURL(
        Class<?> clazz, String content, String mimeType, String... parameters) {
        for (KnockoutTCK tck : tcks(clazz)) {
            String o = tck.prepareWebResource(content, mimeType, parameters);
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
