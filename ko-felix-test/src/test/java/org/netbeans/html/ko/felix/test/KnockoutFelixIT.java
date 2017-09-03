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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
package org.netbeans.html.ko.felix.test;

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
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class KnockoutFelixIT {
    private static final Logger LOG = Logger.getLogger(KnockoutFelixIT.class.getName());
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
            framework = ff.newFramework(config);
            framework.init();
            loadClassPathBundles(framework);
            framework.start();
            for (Bundle b : framework.getBundleContext().getBundles()) {
                try {
                    if (b.getSymbolicName().contains("felix.framework")) {
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
                    throw new IllegalStateException("Found " + name + " !");
                }
                if (name.contains("felix.framework")) {
                    continue;
                }
                if (name.contains("testng")) {
                    continue;
                }
                if (name.equals("org.apache.aries.spifly.dynamic.bundle")) {
                    continue;
                }
                final String path = "reference:" + file.toURI().toString();
                try {
                    Bundle b = f.getBundleContext().installBundle(path);
                } catch (BundleException ex) {
                    LOG.log(Level.WARNING, "Cannot install " + file, ex);
                }
            }
        }
    }
    
    private static Class<?> loadOSGiClass(Class<?> c) throws Exception {
        return KnockoutFelixTCKImpl.loadOSGiClass(c.getName(), KnockoutFelixIT.framework().getBundleContext());
    }
    
    private static Class<?> browserClass;
    private static Object browserContext;
    
    @Factory public static Object[] compatibilityTests() throws Exception {
        Class<?> tck = loadOSGiClass(KnockoutTCK.class);
        Class<?> peer = loadOSGiClass(KnockoutFelixTCKImpl.class);
        // initialize the TCK
        Callable<Class[]> inst = (Callable<Class[]>) peer.newInstance();
        
        Class[] arr = inst.call();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getClassLoader() == ClassLoader.getSystemClassLoader()) {
                fail("Should be an OSGi class: " + arr[i]);
            }
        }
        
        URI uri = DynamicHTTP.initServer();

        Method start = peer.getMethod("start", String.class, URI.class, boolean.class);
        start.invoke(null, KnockoutFelixIT.class.getName(), uri, true);
        
        ClassLoader l = getClassLoader(null);
        List<Object> res = new ArrayList<Object>();
        for (int i = 0; i < arr.length; i++) {
            seekKOTests(arr[i], res);
        }
        return res.toArray();
    }

    private static void seekKOTests(Class<?> c, List<Object> res) throws SecurityException, ClassNotFoundException {
        Class<? extends Annotation> koTest =
            c.getClassLoader().loadClass(KOTest.class.getName()).
            asSubclass(Annotation.class);
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(koTest) != null) {
                res.add(new KOFx(KnockoutFelixIT.class, browserContext, m));
            }
        }
    }

    static synchronized ClassLoader getClassLoader(Object[] presenter) throws InterruptedException {
        while (browserClass == null) {
            KnockoutFelixIT.class.wait();
        }
        if (presenter != null) {
            presenter[0] = browserContext;
        }
        return browserClass.getClassLoader();
    }
    
    public static synchronized void initialized(Class<?> browserCls, Object presenter) throws Exception {
        browserClass = browserCls;
        browserContext = presenter;
        KnockoutFelixIT.class.notifyAll();
    }

    public static Closeable activateInOSGi(Object presenter) throws Exception {
        Class<?> presenterClass = loadOSGiClass(Fn.Presenter.class);
        Class<?> fnClass = loadOSGiClass(Fn.class);
        Method m = fnClass.getMethod("activate", presenterClass);
        return (Closeable) m.invoke(null, presenter);
    }
}
