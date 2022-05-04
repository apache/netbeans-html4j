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
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.netbeans.html.boot.spi.Fn;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
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
        final ScriptEngine eng = JsUtils.initializeEngine();

        final URL my = JsClassLoaderTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader parent = JsClassLoaderTest.class.getClassLoader().getParent();
        final URLClassLoader ul = new URLClassLoader(new URL[] { my }, parent);
        class MyCL extends FnUtils.JsClassLoaderImpl implements Fn.Presenter, Fn.FromJavaScript {

            public MyCL(ClassLoader parent) {
                super(parent, null, null);
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
                    final java.lang.Object val = eng.eval(sb.toString());
                    return new Fn(this) {
                        @Override
                        public java.lang.Object invoke(java.lang.Object thiz, java.lang.Object... args) throws Exception {
                            List<java.lang.Object> all = new ArrayList<java.lang.Object>(args.length + 1);
                            all.add(thiz == null ? val : thiz);
                            all.addAll(Arrays.asList(args));
                            Invocable inv = (Invocable)eng;
                            try {
                                java.lang.Object ret = inv.invokeMethod(val, "call", all.toArray());
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

            @Override
            public java.lang.Object toJava(java.lang.Object js) {
                return JsUtils.toJava(eng, js);
            }
        }
;

        MyCL l = new MyCL(parent);
        Closeable close = FnContext.activate(l);
        methodClass = l.loadClass(JsMethods.class.getName());
        close.close();
        loader = l;
    }

    private Closeable ctx;
    @BeforeMethod public void initPresenter() {
        ctx = Fn.activate(loader);
    }

    @AfterMethod public void closePresener() throws Exception {
        ctx.close();
    }

    @AfterClass
    public static void cleanUp() {
        methodClass = null;
    }
}