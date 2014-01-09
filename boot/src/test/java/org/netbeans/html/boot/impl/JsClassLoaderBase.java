/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public class JsClassLoaderBase {
    protected static Class<?> methodClass;
    
    public JsClassLoaderBase() {
    }
    
    @BeforeMethod
    public void assertClassDefined() {
        assertNotNull(methodClass, "BeforeClass set up code should provide methodClass");
    }

    @Test public void noParamMethod() throws Throwable {
        Method plus = methodClass.getMethod("fortyTwo");
        try {
            final Object val = plus.invoke(null);
            assertTrue(val instanceof Number, "A number returned " + val);
            assertEquals(((Number)val).intValue(), 42);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void testExecuteScript() throws Throwable {
        Method plus = methodClass.getMethod("plus", int.class, int.class);
        try {
            assertEquals(plus.invoke(null, 10, 20), 30);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    @Test public void overloadedMethod() throws Throwable {
        Method plus = methodClass.getMethod("plus", int.class);
        try {
            assertEquals(plus.invoke(null, 10), 10);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void instanceMethod() throws Throwable {
        Method plus = methodClass.getMethod("plusInst", int.class);
        Object inst = methodClass.newInstance();
        try {
            assertEquals(plus.invoke(inst, 10), 10);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void staticThis() throws Throwable {
        Method st = methodClass.getMethod("staticThis");
        try {
            assertNull(st.invoke(null));
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    @Test public void getThis() throws Throwable {
        Object th = methodClass.newInstance();
        Method st = methodClass.getMethod("getThis");
        try {
            assertEquals(st.invoke(th), th);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void truth() throws Throwable {
        Method st = methodClass.getMethod("truth");
        assertTrue((st.getModifiers() & Modifier.STATIC) != 0, "Is static");
        assertEquals(st.invoke(null), Boolean.TRUE, "Can return boolean");
    }
    
    @Test public void callback() throws Throwable {
        class R implements Runnable {
            int cnt;
            
            @Override
            public void run() {
                cnt++;
            }
        }
        R r = new R();
        
        Method inc = methodClass.getMethod("callback", Runnable.class);
        inc.invoke(null, r);
        
        assertEquals(r.cnt, 1, "Callback happened");
    }
    
    @Test public void sumArray() throws Throwable {
        Method st = methodClass.getMethod("sumArr", int[].class);
        assertEquals(st.invoke(null, new int[] { 1, 2, 3 }), 6, "1+2+3 is six");
    }
    
    @Test public void javaScriptResource() throws Throwable {
        try {
            Method st = methodClass.getMethod("useExternalMul", int.class, int.class);
            assertEquals(st.invoke(null, 6, 7), 42, "Meaning of JavaScript?");
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void callJavaScriptMethodOnOwnClass() throws Throwable {
        try {
            Object thiz = methodClass.newInstance();
            Method st = methodClass.getMethod("returnYourSelf", methodClass);
            assertEquals(st.invoke(null, thiz), thiz, "Returns this");
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void callStaticJavaMethod() throws Throwable {
        Method st = methodClass.getMethod("staticCallback", int.class, int.class);
        assertEquals(st.invoke(null, 6, 7), 42, "Meaning of JavaScript?");
    }

    @Test public void callStaticStringParamMethod() throws Throwable {
        Method st = methodClass.getMethod("parseInt", String.class);
        assertEquals(st.invoke(null, "42"), 42, "Meaning of JavaScript?");
    }
    
    @Test public void firstLong() throws Throwable {
        Method st = methodClass.getMethod("chooseLong", boolean.class, boolean.class, long.class, long.class);
        assertEquals(st.invoke(null, true, false, 10, 20), 10L, "Take first value");
    }

    @Test public void secondLong() throws Throwable {
        Method st = methodClass.getMethod("chooseLong", boolean.class, boolean.class, long.class, long.class);
        assertEquals(st.invoke(null, false, true, 10, 20), 20L, "Take 2nd value");
    }

    @Test public void bothLong() throws Throwable {
        Method st = methodClass.getMethod("chooseLong", boolean.class, boolean.class, long.class, long.class);
        assertEquals(st.invoke(null, true, true, 10, 20), 30L, "Take both values");
    }
    
    @Test public void recordError() throws Throwable {
        Method st = methodClass.getMethod("recordError", Object.class);
        assertEquals(st.invoke(methodClass.newInstance(), "Hello"), "Hello", "The same parameter returned");
    }
    
    @Test public void arrayInOut() throws Throwable {
        String[] arr = { "Ahoj" };
        Method st = methodClass.getMethod("arr", Object[].class);
        Object ret = st.invoke(null, (Object) arr);
        assertTrue(ret instanceof Object[], "Expecting array: " + ret);
        Object[] res = (Object[]) ret;
        assertEquals(res.length, 1, "One element");
        assertEquals(res[0], "Ahoj", "The right string");
    }
}