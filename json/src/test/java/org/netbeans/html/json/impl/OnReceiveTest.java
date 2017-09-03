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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import net.java.html.json.Person;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.Transfer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class OnReceiveTest {
    @Test public void performJSONCall() {
        MockTrans mt = new MockTrans();
        BrwsrCtx ctx = Contexts.newBuilder().register(Transfer.class, mt, 1).build();
        
        Employee e = Models.bind(new Employee(), ctx);
        e.setCall(null);
        Person p = new Person();
        
        mt.result = new HashMap<String, String>();
        mt.result.put("firstName", "Jarda");
        mt.result.put("lastName", "Tulach");
        e.changePersonalities(1, 2.0, "3", p);
        final Call c = e.getCall();
        assertNotNull(c, "A call has been made");
        assertEquals(c.getI(), 1);
        assertEquals(c.getD(), 2.0);
        assertEquals(c.getS(), "3");
        assertEquals(c.getP(), p);
        assertEquals(c.getData().size(), 1, "One result sent over wire");
        assertEquals(c.getData().get(0).getFirstName(), "Jarda");
        assertEquals(c.getData().get(0).getLastName(), "Tulach");
    }

    @Test public void performErrorJSONCallNoHandling() {
        MockTrans mt = new MockTrans();
        mt.err = new Exception("Error");
        BrwsrCtx ctx = Contexts.newBuilder().register(Transfer.class, mt, 1).build();

        Employee e = Models.bind(new Employee(), ctx);
        e.setCall(null);
        Person p = new Person();

        mt.result = new HashMap<String, String>();
        mt.result.put("firstName", "Jarda");
        mt.result.put("lastName", "Tulach");
        e.changePersonalities(1, 2.0, "3", p);
        final Call c = e.getCall();
        assertNull(c, "Error has been swallowed");
    }
    
    @Test public void performErrorJSONCall() {
        MockTrans mt = new MockTrans();
        mt.err = new Exception("Error");
        BrwsrCtx ctx = Contexts.newBuilder().register(Transfer.class, mt, 1).build();

        Employee e = Models.bind(new Employee(), ctx);
        e.setCall(null);
        Person p = new Person();

        mt.result = new HashMap<String, String>();
        mt.result.put("firstName", "Jarda");
        mt.result.put("lastName", "Tulach");
        e.changePersonalitiesWithEx(1, 2.0, "3", p);
        final Call c = e.getCall();
        assertNotNull(c, "A call has been made");
        assertTrue(c.getData().isEmpty(), "No data provided");

        assertEquals(c.getI(), -1);
        assertEquals(c.getD(), -1.0);
        assertEquals(c.getS(), null);
        assertEquals(c.getP(), null);
    }

    @Test public void performErrorWithValuesJSONCall() {
        MockTrans mt = new MockTrans();
        mt.err = new Exception("Error");
        BrwsrCtx ctx = Contexts.newBuilder().register(Transfer.class, mt, 1).build();

        Employee e = Models.bind(new Employee(), ctx);
        e.setCall(null);
        Person p = new Person();

        mt.result = new HashMap<String, String>();
        mt.result.put("firstName", "Jarda");
        mt.result.put("lastName", "Tulach");
        e.changePersonalitiesWithParam(1, 2.0, "3", p);
        final Call c = e.getCall();
        assertNotNull(c, "A call has been made");
        assertTrue(c.getData().isEmpty(), "No data provided");

        assertEquals(c.getI(), 1);
        assertEquals(c.getD(), 2.0);
        assertEquals(c.getS(), "3");
        assertEquals(c.getP(), p);
    }

    
    public static class MockTrans implements Transfer {
        Map<String,String> result;
        Exception err;
        
        @Override
        public void extract(Object obj, String[] props, Object[] values) {
            assertTrue(obj instanceof Map, "It is a map: " + obj);
            Map<?,?> mt = (Map<?,?>) obj;
            for (int i = 0; i < props.length; i++) {
                values[i] = mt.get(props[i]);
            }
        }

        @Override
        public Object toJSON(InputStream is) throws IOException {
            throw new IOException();
        }

        @Override
        public void loadJSON(JSONCall call) {
            Object r = result;
            assertNotNull(r, "We need a reply!");
            result = null;
            if (err != null) {
                call.notifyError(err);
            } else {
                call.notifySuccess(r);
            }
        }
    }
}
