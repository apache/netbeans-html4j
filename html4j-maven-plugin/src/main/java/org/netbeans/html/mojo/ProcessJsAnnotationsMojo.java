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
package org.netbeans.html.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.objectweb.asm.ClassReader;

@Mojo(
    name="process-js-annotations",
    requiresDependencyResolution = ResolutionScope.COMPILE,
    defaultPhase= LifecyclePhase.PROCESS_CLASSES
)
public final class ProcessJsAnnotationsMojo extends AbstractMojo {
    @Component
    private MavenProject prj;

    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File classes;

    public ProcessJsAnnotationsMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ProcessJsAnnotations jsa = new ProcessJsAnnotations() {
            @Override
            protected void log(String msg) {
                getLog().info(msg);
            }
        };
        boolean foundAsm = false;
        for (Artifact a : prj.getArtifacts()) {
            final File f = a.getFile();
            if (f != null) {
                if (a.getArtifactId().equals("asm")) {
                    foundAsm = true;
                }
                jsa.addClasspathEntry(f);
            }
        }
        if (!foundAsm) {
            jsa.addAsm();
        }
        try {
            jsa.process(classes);
        } catch (IOException ex) {
            throw new MojoExecutionException("Problem converting JavaScriptXXX annotations", ex);
        }
    }
}
