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

import net.java.html.BrwsrCtx;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.java.html.json.MapModelTest.One;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Proto;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "Types", properties = {
    @Property(name = "intX", type = int.class),
    @Property(name = "byteX", type = byte.class),
    @Property(name = "shortX", type = short.class),
    @Property(name = "longX", type = long.class),
    @Property(name = "floatX", type = float.class),
    @Property(name = "doubleX", type = double.class),
    @Property(name = "charX", type = char.class),
    @Property(name = "StringX", type = String.class),
    @Property(name = "boolX", type = boolean.class),
})
public class TypesTest {
    private MapModelTest.MapTechnology t;
    private BrwsrCtx c;

    @BeforeMethod public void initTechnology() {
        t = new MapModelTest.MapTechnology();
        c = Contexts.newBuilder().register(Technology.class, t, 1).
            register(Transfer.class, t, 1).build();
    }
    @Function static void readFromEvent(int intX,
        byte byteX,
        short shortX, long longX, float floatX,
        boolean boolX,
        char charX,
        double doubleX,
        String StringX, Types myModel) {

        myModel.setIntX(intX);
        myModel.setDoubleX(doubleX);
        myModel.setStringX(StringX);

        myModel.setByteX(byteX);
        myModel.setShortX(shortX);
        myModel.setLongX(longX);
        myModel.setFloatX(floatX);
        myModel.setBoolX(boolX);
        myModel.setCharX(charX);
    }

    @Test public void canParseEventAttributes() {
        Types t = Models.bind(new Types(), c);
        t.setIntX(33);
        t.setDoubleX(180.5);
        t.setStringX("Ahoj");
        t.setCharX('A');
        t.setByteX((byte)3);
        t.setShortX((short)10);
        t.setLongX(66);
        t.setFloatX(99f);
        t.setBoolX(true);

        assertValidJSON(t.toString());

        Object json = Models.toRaw(t);

        Types copy = Models.bind(new Types(), c);
        Map copyMap = (Map) Models.toRaw(copy);
        One o = (One) copyMap.get("readFromEvent");
        o.fb.call(null, json);

        assertEquals(copy.getIntX(), 33);
        assertEquals(copy.getDoubleX(), 180.5);
        assertEquals(copy.getStringX(), "Ahoj");
        assertEquals(copy.getByteX(), (byte)3);
        assertEquals(copy.getShortX(), (short)10);
        assertEquals(copy.getLongX(), 66L);
        assertEquals(copy.getFloatX(), 99f);
        assertTrue(copy.isBoolX());
        assertEquals(copy.getCharX(), 'A');
    }

    private static void assertValidJSON(String text) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine eng = sem.getEngineByMimeType("text/javascript");
        try {
            eng.eval("var obj = " + text + ";");
        } catch (ScriptException ex) {
            fail("Cannot parse " + text, ex);
        }
    }

    @Test
    public void subclassOfProtoType() {
        class MyType extends Proto.Type<Number> {
            public MyType() {
                super(Integer.class, Integer.class, 0, 0);
            }

            @Override
            protected void setValue(Number model, int index, Object value) {
            }

            @Override
            protected Object getValue(Number model, int index) {
                return null;
            }

            @Override
            protected void call(Number model, int index, Object data, Object event) throws Exception {
            }

            @Override
            protected Number cloneTo(Number model, BrwsrCtx ctx) {
                return model;
            }

            @Override
            protected Number read(BrwsrCtx c, Object json) {
                return null;
            }

            @Override
            protected void onChange(Number model, int index) {
            }

            @Override
            protected Proto protoFor(Object object) {
                return null;
            }
        }

        MyType type = new MyType();
        assertNotNull(type, "Can be instantiated");
    }
}
