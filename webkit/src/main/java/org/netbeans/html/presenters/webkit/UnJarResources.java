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
package org.netbeans.html.presenters.webkit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


final class UnJarResources {
    static URL extract(URL url) throws IOException {
        if (!"jar".equals(url.getProtocol())) {
            return url;
        }
        JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
        JarFile jar = jarConnection.getJarFile();
        if (jar == null) {
            return url;
        }
        File dir = Files.createTempDirectory(jar.getName() + ".dir").toFile();

        Enumeration<JarEntry> en = jar.entries();
        while (en.hasMoreElements()) {
            JarEntry entry = en.nextElement();
            final String entryName = entry.getName();
            if (entryName.endsWith(".class") || entryName.endsWith("/")) {
                continue;
            }
            File file = new File(dir, entryName.replace('/', File.separatorChar));
            file.getParentFile().mkdirs();
            try (InputStream is = jar.getInputStream(entry)) {
                Files.copy(is, file.toPath());
            }
        }

        File file = new File(dir, jarConnection.getEntryName().replace('/', File.separatorChar));
        if (file.exists()) {
            return file.toURI().toURL();
        } else {
            return url;
        }
    }
}
