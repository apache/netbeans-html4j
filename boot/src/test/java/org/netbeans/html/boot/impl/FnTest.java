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
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apidesign.html.boot.spi.Fn;
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
        ScriptEngineManager sem = new ScriptEngineManager();
        final ScriptEngine eng = sem.getEngineByMimeType("text/javascript");
        
        final URL my = FnTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader parent = JsClassLoaderTest.class.getClassLoader().getParent();
        final URLClassLoader ul = new URLClassLoader(new URL[] { my }, parent);
        
        class Impl implements FindResources, Fn.Presenter {
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
        }
        Impl impl = new Impl();
        ClassLoader loader = FnUtils.newLoader(impl, impl, parent);
        presenter = impl;
        
        Closeable close = FnContext.activate(impl);
        methodClass = loader.loadClass(JsMethods.class.getName());
        close.close();
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
