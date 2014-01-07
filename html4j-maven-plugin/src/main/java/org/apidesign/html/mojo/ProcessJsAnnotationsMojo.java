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
package org.apidesign.html.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apidesign.html.boot.impl.FnUtils;

@Mojo(
    name="process-js-annotations",
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    defaultPhase= LifecyclePhase.PROCESS_CLASSES
)
public final class ProcessJsAnnotationsMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    private MavenProject prj;
    
    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File classes;
    
    /** Checks all "provided" dependency JAR files and if they contain
     * usage of JavaScriptXXX annotation, their classes are expanded into the
     * <code>classes</code> directory.
     */
    @Parameter(defaultValue = "false")
    private boolean processProvided;

    public ProcessJsAnnotationsMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            processClasses(classes);
        } catch (IOException ex) {
            throw new MojoExecutionException("Problem converting JavaScriptXXX annotations", ex);
        }
        
        if (processProvided) {
            List<URL> arr = new ArrayList<URL>();
            for (Artifact a : prj.getArtifacts()) {
                final File f = a.getFile();
                if (f != null) {
                    try {
                        arr.add(f.toURI().toURL());
                    } catch (MalformedURLException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
            URLClassLoader l = new URLClassLoader(arr.toArray(new URL[arr.size()]));
            for (Artifact a : prj.getArtifacts()) {
                if (!"provided".equals(a.getScope())) {
                    continue;
                }
                final File f = a.getFile();
                if (f != null) {
                    try {
                        processClasses(f, classes, l);
                    } catch (IOException ex) {
                        throw new MojoExecutionException("Problem converting JavaScriptXXX annotations in " + f, ex);
                    }
                }
            }
        }
    }
    
    private void processClasses(File f) throws IOException, MojoExecutionException {
        if (f.isDirectory()) {
            File[] arr = f.listFiles();
            if (arr != null) {
                for (File file : arr) {
                    processClasses(file);
                }
            }
            return;
        }
        if (!f.exists()) {
            throw new MojoExecutionException("Does not exist: " + f);
        }
        
        if (!f.getName().endsWith(".class")) {
            return;
        }
        
        byte[] arr = new byte[(int)f.length()];
        FileInputStream is = new FileInputStream(f);
        try {
            readArr(arr, is);
        } finally {
            is.close();
        }
        
        byte[] newArr = FnUtils.transform(arr, null);
        if (newArr == null || newArr == arr) {
            return;
        }
        getLog().info("Processing " + f);
        writeArr(f, newArr);        
    }

    private void writeArr(File f, byte[] newArr) throws IOException, FileNotFoundException {
        FileOutputStream os = new FileOutputStream(f);
        try {
            os.write(newArr);
        } finally {
            os.close();
        }
    }

    private static void readArr(byte[] arr, InputStream is) throws IOException {
        int off = 0;
        while (off< arr.length) {
            int read = is.read(arr, off, arr.length - off);
            if (read == -1) {
                break;
            }
            off += read;
        }
    }
    
    private void processClasses(File jar, File target, ClassLoader l) throws IOException {
        ZipFile zf = new ZipFile(jar);
        Enumeration<? extends ZipEntry> en = zf.entries();
        Map<String,byte[]> waiting = new HashMap<String, byte[]>();
        boolean found = false;
        while (en.hasMoreElements()) {
            ZipEntry ze = en.nextElement();
            if (ze.getName().endsWith("/")) {
                continue;
            }
            byte[] arr = new byte[(int)ze.getSize()];
            InputStream is = zf.getInputStream(ze);
            try {
                readArr(arr, is);
            } finally {
                is.close();
            }
            if (ze.getName().endsWith(".class")) {
                byte[] newArr = FnUtils.transform(arr, l);
                if (newArr == null || newArr == arr) {
                    waiting.put(ze.getName(), arr);
                    continue;
                }
                File t = new File(target, ze.getName().replace('/', File.separatorChar));
                t.getParentFile().mkdirs();
                writeArr(t, newArr);
                found = true;
                getLog().info("Found " + ze.getName() + " in " + jar + " - will copy all classes");
            } else {
                waiting.put(ze.getName(), arr);
            }
        }
        
        if (found) {
            for (Map.Entry<String, byte[]> entry : waiting.entrySet()) {
                String name = entry.getKey();
                byte[] arr = entry.getValue();
                File t = new File(target, name.replace('/', File.separatorChar));
                t.getParentFile().mkdirs();
                writeArr(t, arr);
            }
        }
    }
}
