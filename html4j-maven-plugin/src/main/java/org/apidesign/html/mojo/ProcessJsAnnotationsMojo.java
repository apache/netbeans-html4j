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
package org.apidesign.html.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
            for (Artifact a : prj.getArtifacts()) {
                if (!"provided".equals(a.getScope())) {
                    continue;
                }
                final File f = a.getFile();
                if (f != null) {
                    try {
                        processClasses(f, classes);
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
    
    private void processClasses(File jar, File target) throws IOException {
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
                byte[] newArr = FnUtils.transform(arr, null);
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
