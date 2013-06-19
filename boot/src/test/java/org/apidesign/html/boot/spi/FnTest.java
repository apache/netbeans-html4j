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

package org.apidesign.html.boot.spi;



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
import org.apidesign.html.boot.impl.FnUtils;
import org.apidesign.html.boot.impl.JsClassLoaderBase;
import org.apidesign.html.boot.impl.JsClassLoaderTest;
import org.apidesign.html.boot.impl.JsMethods;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FnTest extends JsClassLoaderBase {
    
    public FnTest() {
    }

    @BeforeClass
    public static void createClassLoader() throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        final ScriptEngine eng = sem.getEngineByMimeType("text/javascript");
        
        final URL my = FnTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader parent = JsClassLoaderTest.class.getClassLoader().getParent();
        final URLClassLoader ul = new URLClassLoader(new URL[] { my }, parent);
        
        class Impl implements Fn.Finder, Fn.Presenter {
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
                    return new Fn() {
                        @Override
                        public Object invoke(Object thiz, Object... args) throws Exception {
                            List<Object> all = new ArrayList<Object>(args.length + 1);
                            all.add(thiz == null ? val : thiz);
                            all.addAll(Arrays.asList(args));
                            Invocable inv = (Invocable)eng;
                            Object ret = inv.invokeMethod(val, "call", all.toArray());
                            return ret == val ? null : ret;
                        }
                    };
                } catch (ScriptException ex) {
                    throw new LinkageError("Can't parse: " + sb, ex);
                }
            }

            @Override
            public void loadPage(String resource) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void waitFinished() {
                throw new UnsupportedOperationException();
            }
            
        }
        Impl impl = new Impl();
        ClassLoader loader = FnUtils.newLoader(impl, impl, parent);
       
        
        methodClass = loader.loadClass(JsMethods.class.getName());
    }
    
}
