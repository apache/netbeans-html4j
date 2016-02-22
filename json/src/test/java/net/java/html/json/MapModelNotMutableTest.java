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
