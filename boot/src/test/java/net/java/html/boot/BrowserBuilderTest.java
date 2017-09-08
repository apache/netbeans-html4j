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
package net.java.html.boot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class BrowserBuilderTest {
    private File dir;
    private File index;
    
    public BrowserBuilderTest() {
    }
    
    @BeforeMethod public void prepareFiles() throws IOException {
        dir = File.createTempFile("test", ".dir");
        dir.delete();
        assertTrue(dir.mkdirs(), "Dir successfully created: " + dir);
        
        index = new File(dir, "index.html");
        index.createNewFile();
        
        System.setProperty("browser.rootdir", dir.getPath());
    }
    
    @AfterMethod public void clearFiles() throws IOException {
        Files.walkFileTree(dir.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    

    @Test public void findsZhCN() throws IOException {
        File zh = new File(dir, "index_zh.html"); zh.createNewFile();
        File zhCN = new File(dir, "index_zh_CN.html"); zhCN.createNewFile();
        
        IOException[] mal = { null };
        URL url = BrowserBuilder.findLocalizedResourceURL("index.html", Locale.SIMPLIFIED_CHINESE, mal, BrowserBuilder.class);
        
        assertEquals(url, zhCN.toURI().toURL(), "Found both suffixes");
    }
    
    @Test public void findsZh() throws IOException {
        File zh = new File(dir, "index_zh.html"); zh.createNewFile();
        
        IOException[] mal = { null };
        URL url = BrowserBuilder.findLocalizedResourceURL("index.html", Locale.SIMPLIFIED_CHINESE, mal, BrowserBuilder.class);
        
        assertEquals(url, zh.toURI().toURL(), "Found one suffix");
    }

    @Test public void findsIndex() throws IOException {
        IOException[] mal = { null };
        URL url = BrowserBuilder.findLocalizedResourceURL("index.html", Locale.SIMPLIFIED_CHINESE, mal, BrowserBuilder.class);
        
        assertEquals(url, index.toURI().toURL(), "Found root file");
    }
    
}
