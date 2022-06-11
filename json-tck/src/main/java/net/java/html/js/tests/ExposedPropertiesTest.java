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

import java.util.List;
import static net.java.html.js.tests.JavaScriptBodyTest.assertEquals;
import static net.java.html.js.tests.JavaScriptBodyTest.assertNull;
import static net.java.html.js.tests.JavaScriptBodyTest.fail;
import net.java.html.json.Models;
import org.netbeans.html.json.tck.KOTest;

public class ExposedPropertiesTest {
    @KOTest
    public void exposedPropertiesOfAJavaObject() {
        Sum s = new Sum();
        Object[] props = Bodies.forIn(s);

        List<Object> all = Models.asList(props);
        assertEquals(0, all.size(), "No own properties: " + all);
    }

    @KOTest
    public void exposedEqualsOfAJavaObject() {
        Sum s = new Sum();
        assertNoProp(s, "equals", s);
    }

    @KOTest
    public void exposedHashCodeOfAJavaObject() {
        Sum s = new Sum();

        assertNoProp(s, "hashCode", null);
    }

    @KOTest
    public void exposedWaitOfAJavaObject() {
        Sum s = new Sum();

        assertNoProp(s, "wait", null);
    }

    @KOTest
    public void exposedGetClassOfAJavaObject() {
        Sum s = new Sum();

        assertNoProp(s, "getClass", null);
    }

    @KOTest
    public void exposedNotifyOfAJavaObject() {
        Sum s = new Sum();

        assertNoProp(s, "notify", null);
    }

    @KOTest
    public void exposedNotifyAllOfAJavaObject() {
        Sum s = new Sum();

        assertNoProp(s, "notifyAll", null);
    }

    @KOTest
    public void exposedEqualsOfAJavaArray() {
        Object s = new Object[5];
        assertNoProp(s, "equals", s);
    }

    @KOTest
    public void exposedHashCodeOfAJavaArray() {
        Object s = new Object[5];

        assertNoProp(s, "hashCode", null);
    }

    @KOTest
    public void exposedWaitOfAJavaArray() {
        Object s = new Object[5];

        assertNoProp(s, "wait", null);
    }

    @KOTest
    public void exposedGetClassOfAJavaArray() {
        Object s = new Object[5];

        assertNoProp(s, "getClass", null);
    }

    @KOTest
    public void exposedNotifyOfAJavaArray() {
        Object s = new Object[5];

        assertNoProp(s, "notify", null);
    }

    @KOTest
    public void exposedNotifyAllOfAJavaArray() {
        Object s = new Object[5];

        assertNoProp(s, "notifyAll", null);
    }

    @KOTest
    public void exposedEqualsOfAJavaPrimitiveArray() {
        Object s = new int[5];
        assertNoProp(s, "equals", s);
    }

    @KOTest
    public void exposedHashCodeOfAJavaPrimitiveArray() {
        Object s = new int[5];

        assertNoProp(s, "hashCode", null);
    }

    @KOTest
    public void exposedWaitOfAJavaPrimitiveArray() {
        Object s = new int[5];

        assertNoProp(s, "wait", null);
    }

    @KOTest
    public void exposedGetClassOfAJavaPrimitiveArray() {
        Object s = new int[5];

        assertNoProp(s, "getClass", null);
    }

    @KOTest
    public void exposedNotifyOfAJavaPrimitiveArray() {
        Object s = new int[5];

        assertNoProp(s, "notify", null);
    }

    private static void assertNoProp(Object obj, String name, Object arg) {
        Object prop = Bodies.get(obj, name);
        assertNull(prop, "Expecting no value for property " + name + ", but was " + Bodies.typeof(prop, false));

        try {
            Object res = Bodies.invoke(obj, name, arg);
            if (name.equals(res)) {
                return;
            }
            fail("Invoking " + name + " on " + obj + " returned " + res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
