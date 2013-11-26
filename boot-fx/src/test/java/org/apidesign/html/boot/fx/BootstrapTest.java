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
package org.apidesign.html.boot.fx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import net.java.html.boot.BrowserBuilder;
import org.apidesign.html.boot.impl.FnContext;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.json.tck.JavaScriptTCK;
import org.apidesign.html.json.tck.KOTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class BootstrapTest {
    private static Class<?> browserClass;
    private static Fn.Presenter browserPresenter;
    
    public BootstrapTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(BootstrapTest.class).
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
                    res.add(new KOFx(browserPresenter, m));
                }
            }
        }
        return res.toArray();
    }

    static synchronized Class<?> loadClass() throws InterruptedException {
        while (browserClass == null) {
            BootstrapTest.class.wait();
        }
        return browserClass;
    }
    
    public static synchronized void ready(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserPresenter = Fn.activePresenter();
        BootstrapTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Class<?> classpathClass = ClassLoader.getSystemClassLoader().loadClass(BootstrapTest.class.getName());
        Method m = classpathClass.getMethod("ready", Class.class);
        m.invoke(null, FxJavaScriptTst.class);
    }
}
