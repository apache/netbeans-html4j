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
package org.netbeans.html.ko.osgi.test;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.KOTest;
import org.netbeans.html.json.tck.KnockoutTCK;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class KnockoutEquinoxIT {
    private static final Logger LOG = Logger.getLogger(KnockoutEquinoxIT.class.getName());
    private static Framework framework;
    private static File dir;
    static Framework framework() throws Exception {
        if (framework != null) {
            return framework;
        }
        for (FrameworkFactory ff : ServiceLoader.load(FrameworkFactory.class)) {

            String basedir = System.getProperty("basedir");
            assertNotNull("basedir preperty provided", basedir);
            File target = new File(basedir, "target");
            dir = new File(target, "osgi");
            dir.mkdirs();

            Map<String,String> config = new HashMap<String, String>();
            config.put(Constants.FRAMEWORK_STORAGE, dir.getPath());
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "sun.misc,"
                + "javafx.application,"
                + "javafx.beans,"
                + "javafx.beans.property,"
                + "javafx.beans.value,"
                + "javafx.collections,"
                + "javafx.concurrent,"
                + "javafx.event,"
                + "javafx.geometry,"
                + "javafx.scene,"
                + "javafx.scene.control,"
                + "javafx.scene.image,"
                + "javafx.scene.layout,"
                + "javafx.scene.text,"
                + "javafx.scene.web,"
                + "javafx.stage,"
                + "javafx.util,"
                + "netscape.javascript"
            );
            config.put("osgi.hook.configurators.include", "org.netbeans.html.equinox.agentclass.AgentHook");
            framework = ff.newFramework(config);
            framework.init();
            loadClassPathBundles(framework);
            framework.start();
            for (Bundle b : framework.getBundleContext().getBundles()) {
                try {
                    if (b.getSymbolicName().contains("equinox-agentclass-hook")) {
                        continue;
                    }
                    if (b.getSymbolicName().contains("glassfish.grizzly")) {
                        continue;
                    }
                    b.start();
                    LOG.log(Level.INFO, "Started {0}", b.getSymbolicName());
                } catch (BundleException ex) {
                    throw new IllegalStateException("Cannot start bundle " + b.getSymbolicName(), ex);
                }
            }
            return framework;
        }
        fail("No OSGi framework in the path");
        return null;
    }

    @AfterClass public static void cleanUp() throws Exception {
        if (framework != null) framework.stop();
        clearUpDir(dir);
    }
    private static void clearUpDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                clearUpDir(f);
            }
        }
        dir.delete();
    }



    private static void loadClassPathBundles(Framework f) throws IOException, BundleException {
        for (String jar : System.getProperty("java.class.path").split(File.pathSeparator)) {
            File file = new File(jar);
            if (!file.isFile()) {
                LOG.info("Not loading " + file);
                continue;
            }
            JarFile jf = new JarFile(file);
            final String name = jf.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
            jf.close();
            if (name != null) {
                if (name.contains("org.eclipse.osgi")) {
                    continue;
                }
                if (name.contains("testng")) {
                    continue;
                }
                if (name.contains("equinox-agentclass-hook")) {
                    continue;
                }
                final String path = "reference:" + file.toURI().toString();
                try {
                    LOG.log(Level.INFO, "Installing bundle {0}", path);
                    Bundle b = f.getBundleContext().installBundle(path);
                    assertNotNull(b);
                } catch (BundleException ex) {
                    LOG.log(Level.WARNING, "Cannot install " + file, ex);
                }
            }
        }
    }

    private static Class<?> loadOSGiClass(Class<?> c) throws Exception {
        return KnockoutEquinoxTCKImpl.loadOSGiClass(c.getName(), KnockoutEquinoxIT.framework().getBundleContext());
    }

    private static Class<?> browserClass;
    private static Object browserContext;

    @Factory public static Object[] compatibilityTests() throws Exception {
        Class<?> tck = loadOSGiClass(KnockoutTCK.class);
        Class<?> peer = loadOSGiClass(KnockoutEquinoxTCKImpl.class);
        // initialize the TCK
        Callable<Class[]> inst = (Callable<Class[]>) peer.newInstance();

        Class[] arr = inst.call();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getClassLoader() == ClassLoader.getSystemClassLoader()) {
                fail("Should be an OSGi class: " + arr[i]);
            }
        }

        final BundleContext ctx = framework.getBundleContext();
        assertNotNull(ctx, "Bundle context found");

        URI uri = DynamicHTTP.initServer();

        Method start = peer.getMethod("start", URI.class, BundleContext.class);
        start.invoke(null, uri, ctx);

        ClassLoader l = getClassLoader();
        List<Object> res = new ArrayList<Object>();
        for (int i = 0; i < arr.length; i++) {
            seekKOTests(arr[i], res);
        }

        for (Bundle b : ctx.getBundles()) {
            res.add(new BundleTest(b));
        }

        return res.toArray();
    }

    private static void seekKOTests(Class<?> c, List<Object> res) throws SecurityException, ClassNotFoundException {
        Class<? extends Annotation> koTest =
            c.getClassLoader().loadClass(KOTest.class.getName()).
            asSubclass(Annotation.class);
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(koTest) != null) {
                res.add(new KOFx(browserContext, m));
            }
        }
    }

    static synchronized ClassLoader getClassLoader() throws InterruptedException {
        while (browserClass == null) {
            KnockoutEquinoxIT.class.wait();
        }
        return browserClass.getClassLoader();
    }

    public static synchronized void initialized(Class<?> browserCls, Object presenter) throws Exception {
        browserClass = browserCls;
        browserContext = presenter;
        KnockoutEquinoxIT.class.notifyAll();
    }

    static Closeable activateInOSGi(Object presenter) throws Exception {
        Class<?> presenterClass = loadOSGiClass(Fn.Presenter.class);
        Class<?> fnClass = loadOSGiClass(Fn.class);
        Method m = fnClass.getMethod("activate", presenterClass);
        return (Closeable) m.invoke(null, presenter);
    }

    public static final class BundleTest implements ITest {
        private final Bundle b;

        BundleTest(Bundle b) {
            this.b = b;
        }
        @Override
        public String getTestName() {
            return "Checking " + b.getSymbolicName();
        }

        @Test
        public void checkBundleName() {
            switch (b.getSymbolicName()) {
                case "org.eclipse.osgi": return;
                case "org.json": return;
                case "com.beust.jcommander": return;
                case "com.sun.jna": return;
                case "javax.servlet-api": return;
                case "equinox-agentclass-hook": return;
                case "ko-osgi-test": return;
                default:
                    if (b.getSymbolicName().startsWith("org.glassfish.grizzly.")) {
                        return;
                    }
                    if (b.getSymbolicName().startsWith("net.java.html")) {
                        return;
                    }
                    if (b.getSymbolicName().startsWith("org.netbeans.html")) {
                        return;
                    }
            }
            fail("Unexpected OSGi bundle name: " + b.getSymbolicName());
        }
    }
}
