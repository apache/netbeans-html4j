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
package net.java.html.json.tests;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import net.java.html.BrwsrCtx;
import org.apidesign.html.json.tck.KnockoutTCK;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Utils {
    private static KnockoutTCK instantiatedTCK;

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
}
