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
import org.objectweb.asm.ClassReader;

abstract class ProcessJsAnnotations {
    private final LinkedList<URL> cp = new LinkedList<URL>();

    protected abstract void log(String msg);

    public void addClasspathEntry(File f) {
        try {
            cp.add(f.toURI().toURL());
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void addAsm() {
        URL loc = ClassReader.class.getProtectionDomain().getCodeSource().getLocation();
        cp.addFirst(loc);
    }

    public void process(File classes) throws IOException {
        cp.add(classes.toURI().toURL());
        URLClassLoader l = new URLClassLoader(cp.toArray(new URL[cp.size()]));
        File master = new File(new File(classes, "META-INF"), "net.java.html.js.classes");
        processClasses(l, master, classes);
    }

    private void processClasses(ClassLoader l, File master, File f) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            boolean classes = new File(f, "net.java.html.js.classes").exists();
            File[] arr = f.listFiles();
            if (arr != null) {
                for (File file : arr) {
                    if (classes || file.isDirectory()) {
                        processClasses(l, master, file);
                    }
                }
            }
            return;
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

        byte[] newArr = null;
        try {
            Class<?> fnUtils = l.loadClass("org.netbeans.html.boot.impl.FnUtils");
            Method transform = fnUtils.getMethod("transform", byte[].class, ClassLoader.class);

            newArr = (byte[]) transform.invoke(null, arr, l);
            if (newArr == null || newArr == arr) {
                return;
            }
            filterClass(new File(f.getParentFile(), "net.java.html.js.classes"), f.getName());
            filterClass(master, f.getName());
        } catch (Exception ex) {
            throw new IOException("Can't process " + f, ex);
        }
        log("Processing " + f);
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

    private static void filterClass(File f, String className) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }

        BufferedReader r = new BufferedReader(new FileReader(f));
        List<String> arr = new ArrayList<String>();
        boolean modified = false;
        for (;;) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.endsWith(className)) {
                modified = true;
                continue;
            }
            arr.add(line);
        }
        r.close();

        if (modified) {
            if (arr.isEmpty()) {
                f.delete();
            } else {
                FileWriter w = new FileWriter(f);
                for (String l : arr) {
                    w.write(l);
                    w.write("\n");
                }
                w.close();
            }
        }
    }
}
