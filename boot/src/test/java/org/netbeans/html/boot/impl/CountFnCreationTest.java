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
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
@JavaScriptResource("empty.js")
public class CountFnCreationTest implements Fn.Presenter {
    private int cnt;
    
    @JavaScriptBody(args = {}, body = "return;")
    public static native void body();
    
    @Test public void countManyTimes() throws Exception {
        class Res implements FindResources {
            @Override
            public void findResources(String path, Collection<? super URL> results, boolean oneIsEnough) {
                try {
                    ClassLoader l = CountFnCreationTest.class.getClassLoader();
                    Enumeration<URL> en = l.getResources(path);
                    while (en.hasMoreElements()) {
                        results.add(en.nextElement());
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        ClassLoader l = FnUtils.newLoader(new Res(), this, CountFnCreationTest.class.getClassLoader().getParent());
        Method m = l.loadClass(CountFnCreationTest.class.getName()).getMethod("body");
        Closeable c = Fn.activate(this);
        try {
            assertEquals(cnt, 0, "No functions yet");
            m.invoke(null);
            assertEquals(cnt, 1, "One function defined");
            m.invoke(null);
            assertEquals(cnt, 1, "Still one function");
        } finally {
            c.close();
        }
    }

    @Override
    public Fn defineFn(String code, String... names) {
        cnt++;
        return new MyFn(this);
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
    }

    @Override
    public void loadScript(Reader code) throws Exception {
    }
    
    private static final class MyFn extends Fn {

        public MyFn(Presenter presenter) {
            super(presenter);
        }

        @Override
        public java.lang.Object invoke(java.lang.Object thiz, java.lang.Object... args) throws Exception {
            return null;
        }
    }
}
