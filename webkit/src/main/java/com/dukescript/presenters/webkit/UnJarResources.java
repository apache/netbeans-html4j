package com.dukescript.presenters.webkit;

/*
 * #%L
 * WebKit Presenter - a library from the "DukeScript Presenters" project.
 * 
 * Dukehoff GmbH designates this particular file as subject to the "Classpath"
 * exception as provided in the README.md file that accompanies this code.
 * %%
 * Copyright (C) 2015 - 2019 Dukehoff GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
        File dir = File.createTempFile(jar.getName(), ".dir");
        dir.delete();
        dir.mkdirs();

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
