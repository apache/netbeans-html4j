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
package org.netbeans.html.dynamicloader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import org.netbeans.html.boot.spi.Fn;
import org.testng.Assert;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class DynamicClassLoaderTest {
    private static Class<?> browserClass;
    private static Fn.Presenter browserPresenter;

    public DynamicClassLoaderTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(DynamicClassLoaderTest.class).
            loadPage("empty.html").
            invoke("initialized");

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                bb.showAndWait();
            }
        });

        List<Object> res = new ArrayList<Object>();

        Class[] arr = new Class[] { loadClass() };
        for (Class c : arr) {
            for (Method m : c.getDeclaredMethods()) {
                if ((m.getModifiers() & Modifier.PUBLIC) != 0) {
                    res.add(new KOFx(browserPresenter, m));
                }
            }
        }
        return res.toArray();
    }

    static synchronized Class<?> loadClass() throws InterruptedException {
        while (browserClass == null) {
            DynamicClassLoaderTest.class.wait();
        }
        return browserClass;
    }

    public static void ready(Class<?> browserCls) throws Exception {
        Class<?> origClazz = ClassLoader.getSystemClassLoader().loadClass(DynamicClassLoaderTest.class.getName());
        final Field f1 = origClazz.getDeclaredField("browserClass");
        f1.setAccessible(true);
        f1.set(null, browserCls);
        final Field f2 = origClazz.getDeclaredField("browserPresenter");
        f2.setAccessible(true);
        f2.set(null, Fn.activePresenter());
        synchronized (origClazz) {
            origClazz.notifyAll();
        }
    }

    public static void initialized() throws Exception {
        BrwsrCtx b1 = BrwsrCtx.findDefault(DynamicClassLoaderTest.class);
        assertNotSame(b1, BrwsrCtx.EMPTY, "Browser context is not empty");
        BrwsrCtx b2 = BrwsrCtx.findDefault(DynamicClassLoaderTest.class);
        assertSame(b1, b2, "Browser context remains stable");
        Assert.assertNotSame(DynamicClassLoaderTest.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
            "Should use special classloader, not system one"
        );
        DynamicClassLoaderTest.ready(JavaScriptBodyTst.class);
    }
}
