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
package net.java.html.boot.script;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import net.java.html.boot.BrowserBuilder;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.KOTest;
import org.testng.Assert;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class Jsr223JavaScriptTest {
    private static Class<?> browserClass;
    private static Fn.Presenter browserPresenter;
    
    public Jsr223JavaScriptTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        final BrowserBuilder bb = BrowserBuilder.newBrowser(new ScriptPresenter(SingleCase.JS)).
            loadClass(Jsr223JavaScriptTest.class).
            loadPage("empty.html").
            invoke("initialized");

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                bb.showAndWait();
            }
        });

        List<Object> res = new ArrayList<Object>();
        Class<? extends Annotation> test = 
            loadClass().getClassLoader().loadClass(KOTest.class.getName()).
            asSubclass(Annotation.class);

        Class[] arr = (Class[]) loadClass().getDeclaredMethod("tests").invoke(null);
        for (Class c : arr) {
            for (Method m : c.getMethods()) {
                if (m.getAnnotation(test) != null) {
                    res.add(new SingleCase(browserPresenter, m));
                }
            }
        }
        return res.toArray();
    }

    static synchronized Class<?> loadClass() throws InterruptedException {
        while (browserClass == null) {
            Jsr223JavaScriptTest.class.wait();
        }
        return browserClass;
    }
    
    public static synchronized void ready(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserPresenter = Fn.activePresenter();
        Jsr223JavaScriptTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Assert.assertSame(
            Jsr223JavaScriptTest.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
            "No special classloaders"
        );
        Jsr223JavaScriptTest.ready(Jsr223JavaScriptTst.class);
    }
}
