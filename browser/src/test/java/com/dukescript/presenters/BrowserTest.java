package com.dukescript.presenters;

/*
 * #%L
 * DukeScript Presenter for any Browser - a library from the "DukeScript Presenters" project.
 * 
 * Dukehoff GmbH designates this particular file as subject to the "Classpath"
 * exception as provided in the README.md file that accompanies this code.
 * %%
 * Copyright (C) 2015 - 2019 Dukehoff GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import net.java.html.boot.BrowserBuilder;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.JavaScriptTCK;
import org.netbeans.html.json.tck.KOTest;
import org.testng.annotations.Factory;

public class BrowserTest extends JavaScriptTCK {
    private static Class<?> browserClass;
    private static Fn.Presenter browserPresenter;
    
    public BrowserTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        final BrowserBuilder bb = BrowserBuilder.newBrowser(new Browser("BrowserTest", new Browser.Config())).
            loadClass(BrowserTest.class).
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
                    res.add(new KOScript(browserPresenter, m));
                }
            }
        }
        return res.toArray();
    }
    
    public static Class[] tests() {
        return testClasses();
    }

    static synchronized Class<?> loadClass() throws InterruptedException {
        while (browserClass == null) {
            BrowserTest.class.wait();
        }
        return browserClass;
    }
    
    public static synchronized void ready(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserPresenter = Fn.activePresenter();
        BrowserTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Class<?> classpathClass = ClassLoader.getSystemClassLoader().loadClass(BrowserTest.class.getName());
        Method m = classpathClass.getMethod("ready", Class.class);
        m.invoke(null, BrowserTest.class);
    }
}
