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

import java.lang.reflect.Constructor;
import net.java.html.json.Model;
import net.java.html.json.Property;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

@Model(className="Builder", builder = "", properties = {
    @Property(name="bytes", type = byte.class, array = true),
    @Property(name="chars", type = char.class, array = true),
    @Property(name="shorts", type = short.class, array = true),
    @Property(name="ints", type = int.class, array = true),
    @Property(name="longs", type = long.class, array = true),
    @Property(name="floats", type = float.class, array = true),
    @Property(name="doubles", type = double.class, array = true),
    @Property(name="strings", type = String.class, array = true),
})
public class BuilderTest {
    @Test
    public void onlyDefaultClassLoader() {
        Constructor<?>[] arr = Builder.class.getConstructors();
        assertEquals(arr.length, 1, "One constructor");
        assertEquals(arr[0].getParameterTypes().length, 0, "No parameters");
    }

    @Test
    public void assignBytes() {
        Builder b = new Builder().bytes((byte)10, (byte)20, (byte)30);
        assertEquals(b.getBytes().size(), 3);
        assertEquals(b.getBytes().get(0).byteValue(), (byte)10);
    }
    @Test
    public void assignChars() {
        Builder b = new Builder().chars((char)10, (char)20, (char)30);
        assertEquals(b.getChars().size(), 3);
        assertEquals(b.getChars().get(0).charValue(), 10);
    }
    @Test
    public void assignShort() {
        Builder b = new Builder().shorts((short)10, (short)20, (short)30);
        assertEquals(b.getShorts().size(), 3);
        assertEquals(b.getShorts().get(0).intValue(), 10);
    }
    @Test
    public void assignInts() {
        Builder b = new Builder().ints(10, 20, 30);
        assertEquals(b.getInts().size(), 3);
        assertEquals(b.getInts().get(0).intValue(), 10);
    }
    @Test
    public void assignLongs() {
        Builder b = new Builder().longs(10, 20, 30);
        assertEquals(b.getLongs().size(), 3);
        assertEquals(b.getLongs().get(1).intValue(), 20);
    }
    @Test
    public void assignDouble() {
        Builder b = new Builder().doubles(10, 20, 30);
        assertEquals(b.getDoubles().size(), 3);
        assertEquals(b.getDoubles().get(0), 10.0);
    }
    @Test
    public void assignFloats() {
        Builder b = new Builder().floats(10, 20, 30);
        assertEquals(b.getFloats().size(), 3);
        assertEquals(b.getFloats().get(0), 10.0f);
    }
    @Test
    public void assignStrings() {
        Builder b = new Builder().strings("A", "AB", "ABC");
        assertEquals(b.getStrings().size(), 3);
        assertEquals(b.getStrings().get(1), "AB");
    }
}
