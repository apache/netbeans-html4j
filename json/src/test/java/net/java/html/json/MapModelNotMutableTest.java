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

import java.util.Map;
import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Model(className = "ConstantValues", properties = {
    @Property(name = "byteNumber", type = byte.class, mutable = false),
    @Property(name = "shortNumber", type = short.class, mutable = false),
    @Property(name = "intNumber", type = int.class, mutable = false),
    @Property(name = "longNumber", type = long.class, mutable = false),
    @Property(name = "floatNumber", type = float.class, mutable = false),
    @Property(name = "doubleNumber", type = double.class, mutable = false),
    @Property(name = "stringValue", type = String.class, mutable = false),
    @Property(name = "byteArray", type = byte.class, mutable = false, array = true),
    @Property(name = "shortArray", type = short.class, mutable = false, array = true),
    @Property(name = "intArray", type = int.class, mutable = false, array = true),
    @Property(name = "longArray", type = long.class, mutable = false, array = true),
    @Property(name = "floatArray", type = float.class, mutable = false, array = true),
    @Property(name = "doubleArray", type = double.class, mutable = false, array = true),
    @Property(name = "stringArray", type = String.class, mutable = false, array = true),
})
public class MapModelNotMutableTest {
    private BrwsrCtx c;

    @BeforeMethod
    public void initTechnology() {
        MapModelTest.MapTechnology t = new MapModelTest.MapTechnology();
        c = Contexts.newBuilder().register(Technology.class, t, 1).
            register(Transfer.class, t, 1).build();
    }

    @Test
    public void byteConstant() throws Exception {
        ConstantValues value = Models.bind(new ConstantValues(), c);
        value.setByteNumber((byte)13);

        Map m = (Map) Models.toRaw(value);
        Object v = m.get("byteNumber");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), MapModelTest.One.class, "It is instance of One");
        MapModelTest.One o = (MapModelTest.One) v;
        assertEquals(o.changes, 0, "No change so far the only one change happened before we connected");
        assertEquals((byte)13, o.get());

        try {
            value.setByteNumber((byte)15);
            fail("Changing value shouldn't succeed!");
        } catch (IllegalStateException ex) {
            // OK
        }
        assertEquals(o.get(), (byte)13, "Old value should still be in the map");
        assertEquals(o.changes, 0, "No change");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
    }

    @Test
    public void shortConstant() throws Exception {
        ConstantValues value = Models.bind(new ConstantValues(), c);
        value.setShortNumber((short)13);

        Map m = (Map) Models.toRaw(value);
        Object v = m.get("shortNumber");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), MapModelTest.One.class, "It is instance of One");
        MapModelTest.One o = (MapModelTest.One) v;
        assertEquals(o.changes, 0, "No change so far the only one change happened before we connected");
        assertEquals((short)13, o.get());

        try {
            value.setShortNumber((short)15);
            fail("Changing value shouldn't succeed!");
        } catch (IllegalStateException ex) {
            // OK
        }
        assertEquals(o.get(), (short)13, "Old value should still be in the map");
        assertEquals(o.changes, 0, "No change");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
    }

    @Test
    public void intConstant() throws Exception {
        ConstantValues value = Models.bind(new ConstantValues(), c);
        value.setIntNumber(13);

        Map m = (Map) Models.toRaw(value);
        Object v = m.get("intNumber");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), MapModelTest.One.class, "It is instance of One");
        MapModelTest.One o = (MapModelTest.One) v;
        assertEquals(o.changes, 0, "No change so far the only one change happened before we connected");
        assertEquals(13, o.get());

        try {
            value.setIntNumber(15);
            fail("Changing value shouldn't succeed!");
        } catch (IllegalStateException ex) {
            // OK
        }
        assertEquals(o.get(), 13, "Old value should still be in the map");
        assertEquals(o.changes, 0, "No change");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
    }

    @Test
    public void doubleConstant() throws Exception {
        ConstantValues value = Models.bind(new ConstantValues(), c);
        value.setDoubleNumber(13);

        Map m = (Map) Models.toRaw(value);
        Object v = m.get("doubleNumber");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), MapModelTest.One.class, "It is instance of One");
        MapModelTest.One o = (MapModelTest.One) v;
        assertEquals(o.changes, 0, "No change so far the only one change happened before we connected");
        assertEquals(13.0, o.get());

        try {
            value.setDoubleNumber(15);
            fail("Changing value shouldn't succeed!");
        } catch (IllegalStateException ex) {
            // OK
        }
        assertEquals(o.get(), 13.0, "Old value should still be in the map");
        assertEquals(o.changes, 0, "No change");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
    }

    @Test
    public void stringConstant() throws Exception {
        ConstantValues value = Models.bind(new ConstantValues(), c);
        value.setStringValue("Hi");

        Map m = (Map) Models.toRaw(value);
        Object v = m.get("stringValue");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), MapModelTest.One.class, "It is instance of One");
        MapModelTest.One o = (MapModelTest.One) v;
        assertEquals(o.changes, 0, "No change so far the only one change happened before we connected");
        assertEquals("Hi", o.get());

        try {
            value.setStringValue("Hello");
            fail("Changing value shouldn't succeed!");
        } catch (IllegalStateException ex) {
            // OK
        }
        assertEquals(o.get(), "Hi", "Old value should still be in the map");
        assertEquals(o.changes, 0, "No change");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
    }

    @Test
    public void stringArray() throws Exception {
        ConstantValues value = Models.bind(new ConstantValues(), c);
        value.getStringArray().add("Hi");

        Map m = (Map) Models.toRaw(value);
        Object v = m.get("stringArray");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), MapModelTest.One.class, "It is instance of One");
        MapModelTest.One o = (MapModelTest.One) v;
        assertEquals(o.changes, 0, "No change so far the only one change happened before we connected");
        assertEquals(o.get(), new String[] { "Hi" }, "One element");

        try {
            value.getStringArray().add("Hello");
            fail("Changing value shouldn't succeed!");
        } catch (UnsupportedOperationException ex) {
            // OK
        }
        assertEquals(o.get(), new String[] { "Hi" }, "Old value should still be in the map");
        assertEquals(o.changes, 0, "No change");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
    }

}
