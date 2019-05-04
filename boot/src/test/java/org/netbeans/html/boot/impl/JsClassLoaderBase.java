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

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.html.boot.spi.Fn;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
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
            final java.lang.Object val = plus.invoke(null);
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
        java.lang.Object inst = methodClass.newInstance();
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
        java.lang.Object th = methodClass.newInstance();
        Method st = methodClass.getMethod("getThis");
        try {
            assertEquals(st.invoke(th), th);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    @Test public void primitiveArrayReturn() throws Throwable {
        Method st = methodClass.getMethod("both", double.class, double.class);
        Throwable ex;
        try {
            java.lang.Object arr = st.invoke(null, 2, 5);
            ex = null;
        } catch (InvocationTargetException invoke) {
            ex = invoke.getTargetException();
        }
        assertTrue(ex instanceof ClassCastException, "Primitive arrays aren't returned from JavaScript: " + ex);
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
            java.lang.Object thiz = methodClass.newInstance();
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

    @Test public void passEnum() throws Throwable {
        Class<?> enmClazz = methodClass.getDeclaredClasses()[0];
        assertTrue(Enum.class.isAssignableFrom(enmClazz), "It is an enum: " + enmClazz);
        Class<? extends Enum> enmClazz2 = enmClazz.asSubclass(Enum.class);
        Method st = methodClass.getMethod("fromEnum", enmClazz);
        
        java.lang.Object valueB = Enum.valueOf(enmClazz2, "B");
        assertEquals(st.invoke(null, valueB), "B", "Converts to string");
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
        Method st = methodClass.getMethod("recordError", java.lang.Object.class);
        assertEquals(st.invoke(methodClass.newInstance(), "Hello"), "Hello", "The same parameter returned");
    }
    
    @Test public void plusOrMul() throws Throwable {
        Method st = methodClass.getMethod("plusOrMul", int.class, int.class);
        assertNotNull(Fn.activePresenter(), "Is there a presenter?");
        Closeable c = FnContext.activate(null);
        try {
            assertNull(Fn.activePresenter(), "No presenter now");
            assertEquals(st.invoke(null, 6, 7), 42, "Mul in Java");
        } finally {
            c.close();
        }
        assertNotNull(Fn.activePresenter(), "Is there a presenter again");
        assertEquals(st.invoke(null, 6, 7), 13, "Plus in JavaScript");
        c = FnContext.activate(null);
        try {
            assertNull(Fn.activePresenter(), "No presenter again");
            assertEquals(st.invoke(null, 6, 7), 42, "Mul in Java");
        } finally {
            c.close();
        }
        assertNotNull(Fn.activePresenter(), "Is there a presenter again");
        assertEquals(st.invoke(null, 6, 7), 13, "Plus in JavaScript again");
    }
    
    @Test public void arrayInOut() throws Throwable {
        String[] arr = { "Ahoj" };
        Method st = methodClass.getMethod("arr", java.lang.Object[].class);
        java.lang.Object ret = st.invoke(null, (java.lang.Object) arr);
        assertTrue(ret instanceof java.lang.Object[], "Expecting array: " + ret);
        java.lang.Object[] res = (java.lang.Object[]) ret;
        assertEquals(res.length, 1, "One element");
        assertEquals(res[0], "Ahoj", "The right string");
    }

    @Test public void parametricCallback() throws Throwable {
        Map<String,Number> map = new HashMap<String, Number>();
        Method st = methodClass.getMethod("callParamTypes", Map.class, int.class);
        st.invoke(null, map, 42);
        assertEquals(map.get("key").intValue(), 42, "The right value");
    }
    
   @Test public void checkTheTypeOfThrownException() throws Throwable {
        FnContext.currentPresenter(null);
        assertNull(Fn.activePresenter(), "No presenter is activer right now");
        java.lang.Object res = null;
        try {
            Method st = methodClass.getMethod("plus", int.class, int.class);
            try {
                res = st.invoke(null, 40, 2);
                Assert.fail("Native method should throw IllegalStateException. Was: " + res);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        } catch (IllegalStateException ex) {
            assertEquals(ex.getMessage(), "No presenter active. Use BrwsrCtx.execute!");
        }
    }    

}
