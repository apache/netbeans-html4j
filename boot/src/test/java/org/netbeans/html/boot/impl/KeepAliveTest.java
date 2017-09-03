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
import java.util.Collection;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KeepAliveTest implements Fn.Presenter, Fn.KeepAlive, FindResources {
    private Class<?> jsMethods;
    @Test public void keepAliveIsSetToFalse() throws Exception {
        Closeable c = Fn.activate(this);
        Number ret = (Number)jsMethods.getMethod("checkAllowGC", java.lang.Object.class).invoke(null, this);
        c.close();
        assertEquals(ret.intValue(), 0, "keepAlive is set to false");
    }    

    @Test public void keepAliveIsTheDefault() throws Exception {
        Closeable c = Fn.activate(this);
        Number ret = (Number)jsMethods.getMethod("plus", int.class, int.class).invoke(null, 40, 2);
        c.close();
        assertEquals(ret.intValue(), 1, "keepAlive returns true when the presenter is invoked");
    }    

    @BeforeMethod
    public void initClass() throws ClassNotFoundException {
        ClassLoader loader = FnUtils.newLoader(this, this, KeepAliveTest.class.getClassLoader().getParent());
        jsMethods = loader.loadClass(JsMethods.class.getName());
    }

    @Override
    public Fn defineFn(String code, String[] names, final boolean[] keepAlive) {
        return new Fn(this) {
            @Override
            public java.lang.Object invoke(java.lang.Object thiz, java.lang.Object... args) throws Exception {
                boolean res = true;
                if (keepAlive != null) {
                    for (int i = 0; i < keepAlive.length; i++) {
                        res &= keepAlive[i];
                    }
                }
                return res ? 1 : 0;
            }
        };
    }
    
    @Override
    public Fn defineFn(String code, String... names) {
        throw new UnsupportedOperationException("Never called!");
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadScript(Reader code) throws Exception {
    }

    @Override
    public void findResources(String path, Collection<? super URL> results, boolean oneIsEnough) {
        URL u = ClassLoader.getSystemClassLoader().getResource(path);
        if (u != null) {
            results.add(u);
        }
    }
}
