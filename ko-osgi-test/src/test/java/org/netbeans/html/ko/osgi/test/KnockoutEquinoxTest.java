/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package org.netbeans.html.ko.osgi.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.jar.JarFile;
import net.java.html.BrwsrCtx;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class KnockoutEquinoxTest {
    private static Framework framework;
    @BeforeClass public static void initOSGi() throws Exception {
        for (FrameworkFactory ff : ServiceLoader.load(FrameworkFactory.class)) {
            Map<String,String> config = new HashMap<String, String>();
            framework = ff.newFramework(config);
            framework.init();
            loadClassPathBundles(framework);
            framework.start();
            return;
        }
        fail("No OSGi framework in the path");
    }

    private static void loadClassPathBundles(Framework f) throws IOException, BundleException {
        for (String jar : System.getProperty("java.class.path").split(File.pathSeparator)) {
            File file = new File(jar);
            if (!file.isFile()) {
                continue;
            }
            JarFile jf = new JarFile(file);
            final String name = jf.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
            jf.close();
            if (name != null) {
                if (name.contains("org.eclipse.osgi")) {
                    continue;
                }
                Bundle b = f.getBundleContext().installBundle("reference:" + file.toURI().toString());
                b.start();
            }
        }
    }
    
    @Test public void loadAClass() throws Exception {
        Bundle bundle = null;
        for (Bundle b : framework.getBundleContext().getBundles()) {
            if (b.getSymbolicName().equals("net.java.html")) {
                bundle = b;
                break;
            }
        }
        assertNotNull(bundle, "My bundle was found");
        Class<?> clazz = bundle.loadClass(BrwsrCtx.class.getName());
        assertNotNull(clazz, "Class loaded OK");
    }
}
