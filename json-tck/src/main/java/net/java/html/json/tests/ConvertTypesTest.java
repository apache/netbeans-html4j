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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import org.apidesign.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ConvertTypesTest {
    private static InputStream createIS(boolean includeSex, boolean includeAddress, boolean array) 
    throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (array) {
            sb.append("[\n");
        }
        sb.append("{ \"firstName\" : \"son\",\n");
        sb.append("  \"lastName\" : \"dj\" \n");
        if (includeSex) {
            sb.append(",  \"sex\" : \"MALE\" \n");
        }
        if (includeAddress) {
            sb.append(",  \"address\" : { \"street\" : \"Schnirchova\" } \n");
        }
        sb.append("}\n");
        if (array) {
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
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert Sex.MALE.equals(p.getSex()) : "Sex: " + p.getSex();
    }

    @KOTest
    public void parseConvertToPeople() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(true, false, false);
        
        Person p = Models.parse(c, Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert Sex.MALE.equals(p.getSex()) : "Sex: " + p.getSex();
    }
    
    @KOTest
    public void parseConvertToPeopleWithAddress() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(true, true, false);
        
        Person p = Models.parse(c, Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert Sex.MALE.equals(p.getSex()) : "Sex: " + p.getSex();
        assert p.getAddress() != null : "Some address provided";
        assert p.getAddress().getStreet().equals("Schnirchova") : "Is Schnirchova: " + p.getAddress();
    }

    @KOTest
    public void testConvertToPeopleWithoutSex() throws Exception {
        final Object o = createJSON(false);
        
        Person p = Models.fromRaw(newContext(), Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert p.getSex() == null : "No sex: " + p.getSex();
    }
    
    @KOTest
    public void parseConvertToPeopleWithoutSex() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(false, false, false);
        Person p = Models.parse(c, Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert p.getSex() == null : "No sex: " + p.getSex();
    }
    
    @KOTest
    public void parseConvertToPeopleWithAddressOnArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(true, true, true);
        
        Person p = Models.parse(c, Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert Sex.MALE.equals(p.getSex()) : "Sex: " + p.getSex();
        assert p.getAddress() != null : "Some address provided";
        assert p.getAddress().getStreet().equals("Schnirchova") : "Is Schnirchova: " + p.getAddress();
    }

    @KOTest
    public void parseConvertToPeopleWithoutSexOnArray() throws Exception {
        final BrwsrCtx c = newContext();
        final InputStream o = createIS(false, false, true);
        Person p = Models.parse(c, Person.class, o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert p.getSex() == null : "No sex: " + p.getSex();
    }
    
    private static BrwsrCtx newContext() {
        return Utils.newContext(ConvertTypesTest.class);
    }
}