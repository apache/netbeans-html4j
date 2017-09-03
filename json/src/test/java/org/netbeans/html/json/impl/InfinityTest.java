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
package org.netbeans.html.json.impl;

import net.java.html.json.Model;
import net.java.html.json.Property;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import org.testng.annotations.Test;

@Model(className = "Infinity", properties = {
    @Property(name = "next", type = Infinity.class),
    @Property(name = "address", type = Address.class)
})
public class InfinityTest {
    @Test
    public void atLeastThousandStepsDeep() {
        Infinity infinity = new Infinity();
        int cnt = 0;
        while (++cnt < 1000) {
            infinity = infinity.getNext();
        }
        assertNotNull(infinity);
        assertEquals(cnt, 1000);
    }

    @Test
    public void afterInitializationRemainsTheSame() {
        Infinity infinity = new Infinity();
        Infinity first = infinity.getNext();
        Infinity second = infinity.getNext();
        assertSame(first, second);
    }

    @Test
    public void nullRemains() {
        Infinity infinity = new Infinity();
        infinity.setNext(null);
        assertNull(infinity.getNext(), "Remains null");
        assertNull(infinity.getNext(), "Again");
    }

    @Test
    public void ownValueRemains() {
        Infinity infinity = new Infinity();
        Infinity n = new Infinity();
        infinity.setNext(n);
        assertEquals(infinity.getNext(), n, "Remains n");
        assertEquals(infinity.getNext(), n, "Again n");
    }

    @Test
    public void nullRemainsAfterClone() {
        Infinity infinity = new Infinity();
        infinity.setNext(null);
        Infinity clone = infinity.clone();
        assertNull(clone.getNext(), "Remains null");
        assertNull(clone.getNext(), "Again");
        assertEquals(clone.hashCode(), infinity.hashCode(), "Same hashcode");
    }

    @Test
    public void ownValueRemainsAfterClone() {
        Infinity infinity = new Infinity();
        Infinity n = new Infinity();
        infinity.setNext(n);
        Infinity clone = infinity.clone();
        assertEquals(clone.getNext(), n, "Remains n");
        assertEquals(clone.getNext(), n, "Again n");
    }

    @Test
    public void hashCodeRemainsAfterClone() {
        Infinity infinity = new Infinity();
        Infinity n = new Infinity();
        infinity.setNext(n);
        Infinity clone = infinity.clone();
        assertEquals(clone.getNext(), n, "Remains n");
        assertEquals(clone.getNext(), n, "Again n");
        assertEquals(clone.hashCode(), infinity.hashCode(), "Same hashcode");
    }

    @Test
    public void simpleToStringWithNull() {
        Infinity infinity = new Infinity();
        assertNotNull(infinity.getAddress(), "Initialized will be stored as object");
        assertEquals("{\"next\":null,\"address\":{\"place\":null}}", infinity.toString());
        infinity.hashCode();

        Infinity second = new Infinity();
        assertEquals("{\"next\":null,\"address\":null}", second.toString(), "Uninitialized is turned into null");

        second.hashCode();
    }

    @Test
    public void toStringWithNullAndClone() {
        Infinity infinity = new Infinity();
        infinity.setNext(null);
        Infinity clone = infinity.clone();
        assertNull(infinity.getNext(), "Remains null");
        assertNotNull(infinity.getAddress(), "Address is initialized");
        assertNull(clone.getNext(), "Clone Remains null");
        assertNotNull(clone.getAddress(), "Clone Address is initialized");
        assertEquals(infinity.toString(), clone.toString());
        assertEquals(clone.hashCode(), infinity.hashCode(), "Same hashcode");
    }

}
