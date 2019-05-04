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
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class FnTest extends JsClassLoaderBase {
    private static Fn.Presenter presenter;
    
    public FnTest() {
    }

    @BeforeClass
    public static void createClassLoader() throws Exception {
        final ScriptEngine eng = JsUtils.initializeEngine();
        
        final URL my = FnTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader parent = JsClassLoaderTest.class.getClassLoader().getParent();
        final URLClassLoader ul = new URLClassLoader(new URL[] { my }, parent);
        
        class Impl implements FindResources, Fn.Presenter, Fn.FromJavaScript {
            @Override
            public void findResources(String path, Collection<? super URL> results, boolean oneIsEnough) {
                URL u = ul.findResource(path);
                if (u != null) {
                    results.add(u);
                }
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
                            } catch (ScriptException ex) {
                                throw ex;
                            }
                        }
                    };
                } catch (ScriptException ex) {
                    throw new LinkageError("Can't parse: " + sb, ex);
                }
            }

            @Override
            public void displayPage(URL resource, Runnable r) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void loadScript(Reader code) throws Exception {
                eng.eval(code);
            }

            @Override
            public java.lang.Object toJava(java.lang.Object js) {
                return JsUtils.toJava(eng, js);
            }
        }
        Impl impl = new Impl();
        ClassLoader loader = FnUtils.newLoader(impl, impl, parent);
        presenter = impl;
        
        Closeable close = FnContext.activate(impl);
        methodClass = loader.loadClass(JsMethods.class.getName());
        close.close();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullProcessorNPE() {
        Fn.activate(null);
    }
    
    @Test public void flushingPresenter() throws IOException {
        class FP implements Fn.Presenter, Flushable {
            int flush;

            @Override
            public Fn defineFn(String code, String... names) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void displayPage(URL page, Runnable onPageLoad) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void loadScript(Reader code) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void flush() throws IOException {
                flush++;
            }
        }
        
        FP p = new FP();
        Closeable c1 = Fn.activate(p);
        Closeable c2 = Fn.activate(p);
        c2.close();
        assertEquals(p.flush, 0, "No flush yet");
        c1.close();
        assertEquals(p.flush, 1, "Now flushed");
    }

    @BeforeMethod public void initPresenter() {
        FnContext.currentPresenter(presenter);
    }
}
