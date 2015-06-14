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
package net.java.html.json.tests;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.SequenceInputStream;
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
    private static InputStream createIS(boolean includeSex, boolean includeAddress, int array) 
    throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
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
        final InputStream o = createIS(true, false, -1);
        
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertEquals(Sex.MALE, p.getSex(), "Sex: " + p.getSex());
    }
    
    @KOTest
    public void parseConvertToPeopleWithAddress() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(true, true, -1);
        
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
        final InputStream o = createIS(true, true, -1);
        
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
        final InputStream o = createIS(false, false, -1);
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertNull(p.getSex(), "No sex: " + p.getSex());
    }
    
    @KOTest
    public void parseConvertToPeopleWithAddressOnArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(true, true, 1);
        
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
        final InputStream o = createIS(false, false, 1);
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertNull(p.getSex(), "No sex: " + p.getSex());
    }

    @KOTest
    public void parseFirstElementFromAbiggerArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(false, false, 5);
        Person p = Models.parse(c, Person.class, o);
        
        assertEquals("son", p.getFirstName(), "First name: " + p.getFirstName());
        assertEquals("dj", p.getLastName(), "Last name: " + p.getLastName());
        assertNull(p.getSex(), "No sex: " + p.getSex());
    }

    @KOTest
    public void parseAllElementFromAbiggerArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(false, false, 5);
        
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
        final InputStream o = createIS(false, false, array);
        SequenceInputStream is = new SequenceInputStream(
            new ByteArrayInputStream("{ \"info\" : ".getBytes("UTF-8")),
            new SequenceInputStream(
                o,
                new ByteArrayInputStream("}".getBytes("UTF-8"))
            )
        );

        List<People> res = new ArrayList<People>();
        Models.parse(c, People.class, is, res);

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
        final InputStream o = createIS(false, false, 0);
        
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