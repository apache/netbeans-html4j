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
import java.io.FileOutputStream;
import java.io.IOException;
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
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    defaultPhase= LifecyclePhase.PROCESS_CLASSES
)
public final class ProcessJsAnnotationsMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    private MavenProject prj;
    
    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File classes;

    public ProcessJsAnnotationsMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            processClasess(classes);
        } catch (IOException ex) {
            throw new MojoExecutionException("Problem converting JavaScriptXXX annotations", ex);
        }
    }
    
    private void processClasess(File f) throws IOException, MojoExecutionException {
        if (f.isDirectory()) {
            File[] arr = f.listFiles();
            if (arr != null) {
                for (File file : arr) {
                    processClasess(file);
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
            int off = 0;
            while (off< arr.length) {
                int read = is.read(arr, off, arr.length - off);
                if (read == -1) {
                    break;
                }
                off += read;
            }
        } finally {
            is.close();
        }
        
        byte[] newArr = FnUtils.transform(arr, null);
        if (newArr == null || newArr == arr) {
            return;
        }
        
        FileOutputStream os = new FileOutputStream(f);
        os.write(newArr);
        os.close();
    }
}
