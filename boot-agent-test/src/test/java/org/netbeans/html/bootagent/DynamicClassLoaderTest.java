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
package org.netbeans.html.bootagent;

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
