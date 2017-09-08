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
package net.java.html.js.tests;

import java.io.InputStream;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class BodiesTest {
    
    public BodiesTest() {
    }

    @Test public void annotationIsStillPresent() throws Exception {
        InputStream is = Bodies.class.getResourceAsStream("Bodies.class");
        assertNotNull(is, "Class Stream found");
        
        byte[] arr = new byte[is.available()];
        int len = is.read(arr);
        
        assertEquals(len, arr.length, "Fully read");
        
        String bytes = new String(arr, "UTF-8");
        
        {
            int idx = bytes.indexOf("Lnet/java/html/js/JavaScriptBody");
            if (idx == -1) {
                fail("Expecting JavaScriptBody reference in: " + bytes);
            }
        }
        {
            int idx = bytes.indexOf("return a + b");
            if (idx == -1) {
                fail("Expecting 'return a + b' in the class file: " + bytes);
            }
        }
    }
    
}
