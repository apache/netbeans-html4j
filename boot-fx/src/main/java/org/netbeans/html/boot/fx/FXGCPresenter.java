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
package org.netbeans.html.boot.fx;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import javafx.scene.web.WebView;

/** Presenter for stress testing. It tries to force few GC cycles
 * before returning a Java object from {@link WebView} to simulate the
 * fact that in JDK8 newer than build 112 the Java objects exposed to
 * {@link WebView} are held by weak references.
 *
 * @author Jaroslav Tulach
 */
public final class FXGCPresenter extends AbstractFXPresenter {
    static {
        try {
            try {
                Class<?> c = Class.forName("javafx.application.Platform");
                // OK, on classpath
            } catch (ClassNotFoundException classNotFoundException) {
                Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                m.setAccessible(true);
                File f = new File(System.getProperty("java.home"), "lib/jfxrt.jar");
                if (f.exists()) {
                    URL l = f.toURI().toURL();
                    m.invoke(ClassLoader.getSystemClassLoader(), l);
                }
            }
        } catch (Exception ex) {
            throw new LinkageError("Can't add jfxrt.jar on the classpath", ex);
        }
    }

    @Override
    void waitFinished() {
        FXBrwsr.waitFinished();
    }

    @Override
    WebView findView(final URL resource) {
        return FXBrwsr.findWebView(resource, this);
    }

    @Override
    Object emitJavaObject(Object[] pojo, int hash, int id) {
        Reference<Object> ref = new WeakReference<Object>(pojo[0]);
        boolean nonNull = ref.get() != null;
        assertGC(ref);
        Object r;
        if ((r = ref.get()) == null && nonNull) {
            throw new NullPointerException("Value has been GCed to null for " + hash + " and " + id);
        }
        return r;
    }

    private static boolean isGone(Reference<?> ref) {
        return ref.get() == null;
    }

    private static void assertGC(Reference<Object> ref) {
        long l = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            if (isGone(ref)) {
                return;
            }

            try {
                System.gc();
                System.runFinalization();
            } catch (Error err) {
                LOG.log(Level.INFO, "Problems during GCing attempt of " + ref.get(), err);
            }
        }
        final long took = System.currentTimeMillis() - l;
        LOG.log(Level.FINE, "Good: No GC of {1} for {0} ms.", new Object[]{took, ref.get()});
    }

}
