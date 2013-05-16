/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package net.java.html.json.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import net.java.html.json.Context;
import net.java.html.json.Models;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.apidesign.html.json.impl.JSON;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ConvertTypesTest {
    private static InputStream createIS(boolean includeSex) 
    throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"firstName\" : \"son\",\n");
        sb.append("  \"lastName\" : \"dj\" \n");
        if (includeSex) {
            sb.append(",  \"sex\" : \"MALE\" ");
        }
        sb.append("}\n");
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
        return Utils.createObject(map);
    }
    
    @BrwsrTest
    public void testConvertToPeople() throws Exception {
        final Object o = createJSON(true);
        
        Person p = JSON.read(Utils.newContext(), Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert Sex.MALE.equals(p.getSex()) : "Sex: " + p.getSex();
    }

    @BrwsrTest
    public void parseConvertToPeople() throws Exception {
        final Context c = Utils.newContext();
        final InputStream o = createIS(true);
        
        Person p = Models.parse(c, Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert Sex.MALE.equals(p.getSex()) : "Sex: " + p.getSex();
    }

    @BrwsrTest
    public void testConvertToPeopleWithoutSex() throws Exception {
        final Object o = createJSON(false);
        
        Person p = JSON.read(Utils.newContext(), Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert p.getSex() == null : "No sex: " + p.getSex();
    }
    
    @BrwsrTest
    public void parseConvertToPeopleWithoutSex() throws Exception {
        final Context c = Utils.newContext();
        final InputStream o = createIS(false);
        Person p = Models.parse(c, Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert p.getSex() == null : "No sex: " + p.getSex();
    }
    
    static Object[] create() {
        return VMTest.create(ConvertTypesTest.class);
    }
}