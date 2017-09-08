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
package net.java.html.json;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

@Model(className = "Keywords", properties = {
    @Property(name = "private", type = String.class),
    @Property(name = "public", type = double.class),
    @Property(name = "final", type = String.class),
    @Property(name = "int", type = int.class),
    @Property(name = "class", type = String.class),
//    @Property(name = "{", type = String.class),
    @Property(name = "array", type = KeywordsInArray.class)
})
public class KeywordsTest {
    @Model(className = "KeywordsInArray", properties = {
        @Property(name = "private", type = String.class, array = true),
        @Property(name = "public", type = double.class, array = true),
        @Property(name = "final", type = String.class, array = true),
        @Property(name = "int", type = int.class, array = true),
        @Property(name = "class", type = String.class, array = true),
//    @Property(name = "{", type = String.class),
        @Property(name = "array", type = Keywords.class, array = true)
    })
    static class KeywordsInArrayCntrl {
    }
    
    @Test public void verifyKeywordsClassCompiles() {
        Keywords k = new Keywords();
        k.setClass("c");
        k.setFinal("f");
        k.setInt(10);
        k.setPrivate("p");
        k.setPublic(42.0);
        
        assertEquals(k.accessClass(), "c");
        assertEquals(k.getFinal(), "f");
        assertEquals(k.getInt(), 10);
        assertEquals(k.getPrivate(), "p");
        assertEquals(k.getPublic(), 42.0);
    }
    
    @Test public void verifyKeywordsInArrayClassCompiles() {
        KeywordsInArray k = new KeywordsInArray();
        k.accessClass().add("c");
        k.getFinal().add("f");
        k.getInt().add(10);
        k.getPrivate().add("p");
        k.getPublic().add(42.0);
        
        assertEquals(k.accessClass().get(0), "c");
        assertEquals(k.getFinal().get(0), "f");
        assertEquals(k.getInt().get(0), Integer.valueOf(10));
        assertEquals(k.getPrivate().get(0), "p");
        assertEquals(k.getPublic().get(0), 42.0);
    }
}
