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
package org.netbeans.html.boot.impl;

import java.io.Closeable;
import java.io.Reader;
import org.apidesign.html.boot.spi.Fn;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author Jaroslav Tulach
 */
public class JsClassLoaderTest extends JsClassLoaderBase{
    private static Fn.Presenter loader;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        final ScriptEngine eng = sem.getEngineByMimeType("text/javascript");
        
        final URL my = JsClassLoaderTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader parent = JsClassLoaderTest.class.getClassLoader().getParent();
        final URLClassLoader ul = new URLClassLoader(new URL[] { my }, parent);
        class MyCL extends JsClassLoader implements Fn.Presenter {

            public MyCL(ClassLoader parent) {
                super(parent);
            }
            
            @Override
            protected URL findResource(String name) {
                return ul.getResource(name);
            }
            @Override
            public Fn defineFn(String code, String... names) {
                StringBuilder sb = new StringBuilder();
                sb.append("(function() {");
                sb.append("return function(");
                String sep = "";
                for (String n : names) {
                    sb.append(sep);
                    sb.append(n);
                    sep = ", ";
                }
                sb.append(") {");
                sb.append(code);
                sb.append("};");
                sb.append("})()");
                try {
                    final Object val = eng.eval(sb.toString());
                    return new Fn(this) {
                        @Override
                        public Object invoke(Object thiz, Object... args) throws Exception {
                            List<Object> all = new ArrayList<Object>(args.length + 1);
                            all.add(thiz == null ? val : thiz);
                            all.addAll(Arrays.asList(args));
                            Invocable inv = (Invocable)eng;
                            try {
                                Object ret = inv.invokeMethod(val, "call", all.toArray());
                                return val.equals(ret) ? null : ret;
                            } catch (Exception ex) {
                                throw ex;
                            }
                        }
                    };
                } catch (ScriptException ex) {
                    throw new LinkageError("Can't parse: " + sb, ex);
                }
            }

            @Override
            protected Enumeration<URL> findResources(String name) {
                URL u = findResource(name);
                List<URL> arr = new ArrayList<URL>();
                if (u != null) {
                    arr.add(u);
                }
                return Collections.enumeration(arr);
            }

            @Override
            public void loadScript(Reader code) throws ScriptException {
                eng.eval(code);
            }

            @Override
            public void displayPage(URL page, Runnable onPageLoad) {
                throw new UnsupportedOperationException();
            }
        };
        
        MyCL l = new MyCL(parent);
        Closeable close = FnContext.activate(l);
        methodClass = l.loadClass(JsMethods.class.getName());
        close.close();
        loader = l;
    }
    
    @BeforeMethod public void initPresenter() {
        FnContext.currentPresenter(loader);
    }

    @AfterClass
    public static void cleanUp() {
        methodClass = null;
    }
}