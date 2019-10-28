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
package org.netbeans.html.presenters.webkit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.JavaScriptTCK;
import org.netbeans.html.json.tck.KOTest;
import org.testng.Assert;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import org.testng.annotations.Factory;

public class GtkJavaScriptTest extends JavaScriptTCK {
    private static Class<?> browserClass;
    private static Fn.Presenter browserPresenter;
    
    public GtkJavaScriptTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        Runnable onPageLoaded = GtkJavaScriptTest::initialized;

        // BEGIN: org.netbeans.html.presenters.webkit.GtkJavaScriptTest
        final WebKitPresenter headlessPresenter = new WebKitPresenter(true);
        final BrowserBuilder bb = BrowserBuilder.newBrowser(headlessPresenter).
            loadFinished(onPageLoaded).
            loadPage("empty.html");
        // END: org.netbeans.html.presenters.webkit.GtkJavaScriptTest

        Future<Void> future = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bb.showAndWait();
                return null;
            }
        });

        List<Object> res = new ArrayList<>();
        try {
            future.get();
            Class<? extends Annotation> test =
                loadClass().getClassLoader().loadClass(KOTest.class.getName()).
                asSubclass(Annotation.class);

            Class[] arr = (Class[]) loadClass().getDeclaredMethod("tests").invoke(null);
            for (Class c : arr) {
                for (Method m : c.getMethods()) {
                    if (m.getAnnotation(test) != null) {
                        res.add(new Case(browserPresenter, m));
                    }
                }
            }
        } catch (InterruptedException | ExecutionException err) {
            err.printStackTrace();
            if (err.getCause() instanceof LinkageError) {
                res.add(new Skip(err.getCause().getMessage()));
            } else {
                res.add(new Skip(err.getMessage()));
            }
        }

        return res.toArray();
    }

    static synchronized Class<?> loadClass() throws InterruptedException {
        while (browserClass == null) {
            GtkJavaScriptTest.class.wait();
        }
        return browserClass;
    }
    
    public static synchronized void ready(Class<?> browserCls) {
        browserClass = browserCls;
        browserPresenter = Fn.activePresenter();
        GtkJavaScriptTest.class.notifyAll();
    }
    
    public static void initialized() {
        BrwsrCtx b1 = BrwsrCtx.findDefault(GtkJavaScriptTest.class);
        assertNotSame(b1, BrwsrCtx.EMPTY, "Browser context is not empty");
        BrwsrCtx b2 = BrwsrCtx.findDefault(GtkJavaScriptTest.class);
        assertSame(b1, b2, "Browser context remains stable");
        Assert.assertSame(GtkJavaScriptTest.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
            "No special classloaders"
        );
        GtkJavaScriptTest.ready(GtkJavaScriptTest.class);
    }
    
    public static Class[] tests() {
        return testClasses();
    }
}
