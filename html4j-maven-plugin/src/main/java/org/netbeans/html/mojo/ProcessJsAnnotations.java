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
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.objectweb.asm.ClassReader;

abstract class ProcessJsAnnotations {
    private final LinkedList<URL> cp = new LinkedList<>();
    private final List<File> roots = new LinkedList<>();

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

    public void addRoot(File file) {
        roots.add(file);
    }

    public void process() throws IOException {
        MultiFile classes = new MultiFile(roots);
        for (File r : roots) {
            cp.add(r.toURI().toURL());
        }
        URLClassLoader l = new URLClassLoader(cp.toArray(new URL[cp.size()]));
        MultiFile master = classes.child("META-INF", "net.java.html.js.classes");
        processClasses(l, master, classes);
    }

    private void processClasses(ClassLoader l, MultiFile master, MultiFile f) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            boolean classes = f.child("net.java.html.js.classes").exists();
            MultiFile[] arr = f.listFiles();
            if (arr != null) {
                for (MultiFile file : arr) {
                    if (classes || file.isDirectory()) {
                        processClasses(l, master, file);
                    }
                }
            }
        }

        if (!f.isFile() || !f.getName().endsWith(".class")) {
            return;
        }

        byte[] arr = f.readFully();
        byte[] newArr = null;
        try {
            Class<?> fnUtils = l.loadClass("org.netbeans.html.boot.impl.FnUtils");
            Method transform = fnUtils.getMethod("transform", byte[].class, ClassLoader.class);

            newArr = (byte[]) transform.invoke(null, arr, l);
            if (newArr == null || newArr == arr) {
                return;
            }
            filterClass(f.getParentFile().child("net.java.html.js.classes"), f.getName());
            filterClass(master, f.getName());
        } catch (Exception ex) {
            throw new IOException("Can't process " + f, ex);
        }
        log("Processing " + f);
        f.writeArr(newArr);
    }

    private static void filterClass(MultiFile f, String className) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }

        List<String> arr;
        boolean modified;
        try (BufferedReader r = new BufferedReader(f.reader())) {
            arr = new ArrayList<>();
            modified = false;
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
        }

        if (modified) {
            if (arr.isEmpty()) {
                f.delete();
            } else {
                try (FileWriter w = f.writer()) {
                    for (String l : arr) {
                        w.write(l);
                        w.write("\n");
                    }
                }
            }
        }
    }

    private static final class MultiFile {

        private final List<File> roots;

        MultiFile(List<File> roots) {
            this.roots = roots;
        }

        MultiFile child(String... names) {
            List<File> arr = new ArrayList<>();
            for (File r : roots) {
                for (String n : names) {
                    r = new File(r, n);
                }
                arr.add(r);
            }
            return new MultiFile(arr);
        }

        boolean exists() {
            for (File r : roots) {
                if (r.exists()) {
                    return true;
                }
            }
            return false;
        }

        boolean isDirectory() {
            for (File r : roots) {
                if (r.isDirectory()) {
                    return true;
                }
            }
            return false;
        }

        String getName() {
            for (File r : roots) {
                return r.getName();
            }
            return null;
        }

        MultiFile[] listFiles() {
            Set<String> names = new TreeSet<>();
            for (File r : roots) {
                final String[] children = r.list();
                if (children == null) {
                    continue;
                }
                names.addAll(Arrays.asList(children));
            }
            MultiFile[] arr = new MultiFile[names.size()];
            int at = 0;
            for (String name : names) {
                arr[at++] = child(name);
            }
            return arr;
        }

        boolean isFile() {
            for (File r : roots) {
                if (r.isFile()) {
                    return true;
                }
            }
            return false;
        }

        private MultiFile getParentFile() {
            List<File> arr = new ArrayList<>();
            for (File r : roots) {
                arr.add(r.getParentFile());
            }
            return new MultiFile(arr);
        }

        byte[] readFully() throws IOException {
            for (File f : roots) {
                if (f.isFile()) {
                    byte[] arr;
                    try (FileInputStream is = new FileInputStream(f)) {
                        arr = new byte[(int)f.length()];
                        int off = 0;
                        while (off < arr.length) {
                            int read = is.read(arr, off, arr.length - off);
                            if (read == -1) {
                                break;
                            }
                            off += read;
                        }
                    }
                    return arr;
                }
            }
            throw new FileNotFoundException();
        }

        private void writeArr(byte[] newArr) throws IOException, FileNotFoundException {
            for (File f : roots) {
                if (f.isDirectory()) {
                    continue;
                }
                f.getParentFile().mkdirs();
                try (FileOutputStream os = new FileOutputStream(f)) {
                    os.write(newArr);
                }
                return;
            }
            throw new FileNotFoundException();
        }

        private Reader reader() throws FileNotFoundException {
            for (File r : roots) {
                if (r.isFile()) {
                    return new FileReader(r);
                }
            }
            throw new FileNotFoundException();
        }

        private void delete() {
            for (File r : roots) {
                r.delete();
            }
        }

        private FileWriter writer() throws IOException {
            for (File r : roots) {
                if (r.isFile()) {
                    return new FileWriter(r);
                }
            }
            throw new FileNotFoundException();
        }
    }
}
