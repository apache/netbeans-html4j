/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
