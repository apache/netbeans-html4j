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
package org.netbeans.html.ko4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import org.testng.Assert;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.Test;

public class LicenseTest {
    @Test
    public void dependencies() throws IOException {
        String text = readGeneratedResource("META-INF/DEPENDENCIES");
        assertNotEquals(text.indexOf("Knockout JavaScript library"), -1, text);
        assertNotEquals(text.indexOf("License: MIT"), -1, text);
    }

    @Test
    public void license() throws IOException {
        String text = readGeneratedResource("META-INF/LICENSE");
        assertNotEquals(text.indexOf("The MIT License (MIT)"), -1, text);
        assertNotEquals(text.indexOf("Copyright (c) Steven Sanderson, the Knockout.js team"), -1, text);
    }

    private static String readGeneratedResource(String resource) throws IOException {
        URL found = null;
        Enumeration<URL> en = ClassLoader.getSystemResources(resource);
        StringBuilder sb = new StringBuilder();
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            sb.append("url: ").append(url).append("\n");
            if ("file".equals(url.getProtocol())) {
                if (url.getFile().endsWith("/classes/" + resource)) {
                    assertNull(found, sb.toString());
                    found = url;
                }
            }
        }
        Assert.assertNotNull(found, sb.toString());
        sb.setLength(0);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(found.openStream()))) {
            for (;;) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
