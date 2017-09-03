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
package net.java.html.json.tests;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import org.netbeans.html.json.tck.KOTest;
import static net.java.html.json.tests.Utils.assertEquals;
import static net.java.html.json.tests.Utils.assertNull;
import static net.java.html.json.tests.Utils.assertNotNull;

/**
 *
 * @author Jaroslav Tulach
 */
public final class ConvertTypesTest {
    private static InputStream createIS(String prefix, boolean includeSex, boolean includeAddress, int array, String suffix)
    throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        }
        int repeat;
        if (array != -1) {
            sb.append("[\n");
            repeat = array;
        } else {
            repeat = 1;
        }
        for (int i = 0; i < repeat; i++) {
            sb.append("{ \"firstName\" : \"son\",\n");
            sb.append("  \"lastName\" : \"dj\" \n");
            if (includeSex) {
                sb.append(",  \"sex\" : \"MALE\" \n");
            }
            if (includeAddress) {
                sb.append(",  \"address\" : { \"street\" : \"Schnirchova\" } \n");
            }
            sb.append("}\n");
            if (i < array - 1) {
                sb.append(",");
            }
        }
        if (array != -1) {
            sb.append(']');
        }
        if (suffix != null) {
            sb.append(suffix);
        }
        return new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
    }
    private static Object createJSON(boolean includeSex) 
    throws UnsupportedEncodingException {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("firstName", "son");
        map.put("lastName", "dj");
        if (includeSex) {
            map.put("sex", "MALE");
        }
        return Utils.createObject(map, ConvertTypesTest.class);
    }
    
    @KOTest
    public void testConvertToPeople() throws Exception {
        final Object o = createJSON(true);
        
        Person p = Models.fromRaw(newContext(), Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertEquals(Sex.MALE, p.getSex(), "Sex: " + p.getSex());
    }

    @KOTest
    public void parseConvertToPeople() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, true, false, -1, null);
        
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertEquals(Sex.MALE, p.getSex(), "Sex: " + p.getSex());
    }
    
    @KOTest
    public void parseConvertToPeopleWithAddress() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, true, true, -1, null);
        
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertEquals(Sex.MALE, p.getSex(), "Sex: " + p.getSex());
        assertNotNull(p.getAddress(), "Some address provided");
        assertEquals(p.getAddress().getStreet(), "Schnirchova", "Is Schnirchova: " + p.getAddress());
    }

    @KOTest
    public void parseConvertToPeopleWithAddressIntoAnArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, true, true, -1, null);
        
        List<Person> arr = new ArrayList<Person>();
        Models.parse(c, Person.class, o, arr);
        
        assertEquals(arr.size(), 1, "There is one item in " + arr);
        
        Person p = arr.get(0);
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertEquals(Sex.MALE, p.getSex(), "Sex: " + p.getSex());
        assertNotNull(p.getAddress() , "Some address provided");
        assertEquals(p.getAddress().getStreet(), "Schnirchova", "Is Schnirchova: " + p.getAddress());
    }
    
    @KOTest 
    public void parseNullValue() throws Exception {
        final BrwsrCtx c = newContext();
        
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"firstName\" : \"son\",\n");
        sb.append("  \"lastName\" : null } \n");  
        
        final ByteArrayInputStream is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        Person p = Models.parse(c, Person.class, is);

        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertNull(p.getLastName(), "Last name: " + p.getLastName());
    }

    @KOTest 
    public void parseNullArrayValue() throws Exception {
        final BrwsrCtx c = newContext();
        
        StringBuilder sb = new StringBuilder();
        sb.append("[ null, { \"firstName\" : \"son\",\n");
        sb.append("  \"lastName\" : null } ]\n");  
        
        final ByteArrayInputStream is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        List<Person> arr = new ArrayList<Person>();
        Models.parse(c, Person.class, is, arr);
        
        assertEquals(arr.size(), 2, "There are two items in " + arr);
        assertNull(arr.get(0), "first is null " + arr);
        
        Person p = arr.get(1);
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertNull(p.getLastName(), "Last name: " + p.getLastName());
    }

    @KOTest
    public void testConvertToPeopleWithoutSex() throws Exception {
        final Object o = createJSON(false);
        
        Person p = Models.fromRaw(newContext(), Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertNull(p.getSex(), "No sex: " + p.getSex());
    }
    
    @KOTest
    public void parseConvertToPeopleWithoutSex() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, false, false, -1, null);
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertNull(p.getSex(), "No sex: " + p.getSex());
    }
    
    @KOTest
    public void parseConvertToPeopleWithAddressOnArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, true, true, 1, null);
        
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertEquals(Sex.MALE, p.getSex(), "Sex: " + p.getSex());
        assertNotNull(p.getAddress(), "Some address provided");
        assertEquals(p.getAddress().getStreet(), "Schnirchova", "Is Schnirchova: " + p.getAddress());
    }

    @KOTest
    public void parseConvertToPeopleWithoutSexOnArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, false, false, 1, null);
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertNull(p.getSex(), "No sex: " + p.getSex());
    }

    @KOTest
    public void parseFirstElementFromAbiggerArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, false, false, 5, null);
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertNull(p.getSex(), "No sex: " + p.getSex());
    }

    @KOTest
    public void parseAllElementFromAbiggerArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, false, false, 5, null);
        
        List<Person> res = new ArrayList<Person>();
        Models.parse(c, Person.class, o, res);
        
        assertEquals(res.size(), 5, "Five elements found" + res);
        
        for (Person p : res) {
            assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
            assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
            assertNull(p.getSex(), "No sex: " + p.getSex());
        }
    }

    @KOTest
    public void parseFiveElementsAsAnArray() throws Exception {
        doParseInnerArray(5, 5);
    }

    @KOTest
    public void parseInnerElementAsAnArray() throws Exception {
        doParseInnerArray(-1, 1);
    }
    private void doParseInnerArray(int array, int expect) throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS("{ \"info\" : ", false, false, array, "}");

        List<People> res = new ArrayList<People>();
        Models.parse(c, People.class, o, res);

        assertEquals(res.size(), 1, "One people" + res);

        int cnt = 0;
        for (Person p : res.get(0).getInfo()) {
            assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
            assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
            assertNull(p.getSex(), "No sex: " + p.getSex());
            cnt++;
        }

        assertEquals(cnt, expect, "Person found in info");
    }
    
    @KOTest
    public void parseOnEmptyArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(null, false, false, 0, null);
        
        try {
            Models.parse(c, Person.class, o);
        } catch (EOFException ex) {
            // OK
            return;
        }
        throw new IllegalStateException("Should throw end of file exception, as the array is empty");
    }
    
    private static BrwsrCtx newContext() {
        return Utils.newContext(ConvertTypesTest.class);
    }
}