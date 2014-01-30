/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import org.apidesign.html.json.tck.KOTest;

/** Need to verify that models produce reasonable JSON objects.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "JSONik", properties = {
    @Property(name = "fetched", type = Person.class),
    @Property(name = "fetchedCount", type = int.class),
    @Property(name = "fetchedResponse", type = String.class),
    @Property(name = "fetchedSex", type = Sex.class, array = true)
})
public final class JSONTest {
    private JSONik js;
    private Integer orig;
    private String url;
    
    @ModelOperation static void assignFetched(JSONik m, Person p) {
        m.setFetched(p);
    }
    
    @KOTest public void toJSONInABrowser() throws Throwable {
        Person p = Models.bind(new Person(), newContext());
        p.setSex(Sex.MALE);
        p.setFirstName("Jarda");
        p.setLastName("Tulach");

        Object json;
        try {
            json = parseJSON(p.toString());
        } catch (Throwable ex) {
            throw new IllegalStateException("Can't parse " + p).initCause(ex);
        }
        
        Person p2 = Models.fromRaw(newContext(), Person.class, json);
        
        assert p2.getFirstName().equals(p.getFirstName()) : 
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName();
    }
    
    @KOTest public void toJSONWithEscapeCharactersInABrowser() throws Throwable {
        Person p = Models.bind(new Person(), newContext());
        p.setSex(Sex.MALE);
        p.setFirstName("/*\n * Copyright (c) 2013");

        
        final String txt = p.toString();
        Object json;
        try {
            json = parseJSON(txt);
        } catch (Throwable ex) {
            throw new IllegalStateException("Can't parse " + txt).initCause(ex);
        }
        
        Person p2 = Models.fromRaw(newContext(), Person.class, json);
        
        assert p2.getFirstName().equals(p.getFirstName()) : 
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName();
    }
    
    @KOTest public void toJSONWithDoubleSlashInABrowser() throws Throwable {
        Person p = Models.bind(new Person(), newContext());
        p.setSex(Sex.MALE);
        p.setFirstName("/*\\n * Copyright (c) 2013");

        
        final String txt = p.toString();
        Object json;
        try {
            json = parseJSON(txt);
        } catch (Throwable ex) {
            throw new IllegalStateException("Can't parse " + txt).initCause(ex);
        }
        
        Person p2 = Models.fromRaw(newContext(), Person.class, json);
        
        assert p2.getFirstName().equals(p.getFirstName()) : 
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName();
    }
    
    @KOTest public void toJSONWithApostrophInABrowser() throws Throwable {
        Person p = Models.bind(new Person(), newContext());
        p.setSex(Sex.MALE);
        p.setFirstName("Jimmy 'Jim' Rambo");

        
        final String txt = p.toString();
        Object json;
        try {
            json = parseJSON(txt);
        } catch (Throwable ex) {
            throw new IllegalStateException("Can't parse " + txt).initCause(ex);
        }
        
        Person p2 = Models.fromRaw(newContext(), Person.class, json);
        
        assert p2.getFirstName().equals(p.getFirstName()) : 
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName();
    }
    
    @OnReceive(url="{url}")
    static void fetch(Person p, JSONik model) {
        model.setFetched(p);
    }

    @OnReceive(url="{url}", onError = "setMessage")
    static void fetchArray(Person[] p, JSONik model) {
        model.setFetchedCount(p.length);
        model.setFetched(p[0]);
    }
    
    static void setMessage(JSONik m, Exception t) {
        assert t != null;
        m.setFetchedResponse("Exception");
    }
    
    @OnReceive(url="{url}")
    static void fetchPeople(People p, JSONik model) {
        model.setFetchedCount(p.getInfo().size());
        model.setFetched(p.getInfo().get(0));
    }

    @OnReceive(url="{url}")
    static void fetchPeopleAge(People p, JSONik model) {
        int sum = 0;
        for (int a : p.getAge()) {
            sum += a;
        }
        model.setFetchedCount(sum);
    }
    
    @KOTest public void loadAndParseJSON() throws InterruptedException {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "{'firstName': 'Sitar', 'sex': 'MALE'}",
                "application/json"
            );
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            js.fetch(url);
        }
    
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
    }
    
    @OnReceive(url="{url}?callme={me}", jsonp = "me")
    static void fetchViaJSONP(Person p, JSONik model) {
        model.setFetched(p);
    }
    
    @KOTest public void loadAndParseJSONP() throws InterruptedException, Exception {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "$0({'firstName': 'Mitar', 'sex': 'MALE'})", 
                "application/javascript",
                "callme"
            );
            orig = scriptElements();
            assert orig > 0 : "There should be some scripts on the page";
            
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            js.fetchViaJSONP(url);
        }
    
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Mitar".equals(p.getFirstName()) : "Unexpected: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
        
        int now = scriptElements();
        
        assert orig == now : "The set of elements is unchanged. Delta: " + (now - orig);
    }
    
    
    
    @OnReceive(url="{url}", method = "PUT", data = Person.class)
    static void putPerson(JSONik model, String reply) {
        model.setFetchedCount(1);
        model.setFetchedResponse(reply);
    }

    @KOTest public void putPeopleUsesRightMethod() throws InterruptedException, Exception {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "$0\n$1", 
                "text/plain",
                "http.method", "http.requestBody"
            );
            orig = scriptElements();
            assert orig > 0 : "There should be some scripts on the page";
            
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            Person p = Models.bind(new Person(), BrwsrCtx.EMPTY);
            p.setFirstName("Jarda");
            js.putPerson(url, p);
        }
    
        int cnt = js.getFetchedCount();
        if (cnt == 0) {
            throw new InterruptedException();
        }
        String res = js.getFetchedResponse();
        int line = res.indexOf('\n');
        String msg;
        if (line >= 0) {
            msg = res.substring(line + 1);
            res = res.substring(0, line);
        } else {
            msg = res;
        }
        
        assert "PUT".equals(res) : "Server was queried with PUT method: " + js.getFetchedResponse();
        
        assert msg.contains("Jarda") : "Data transferred to the server: " + msg;
    }
    
    private static int scriptElements() throws Exception {
        return ((Number)Utils.executeScript(
            JSONTest.class, 
            "return window.document.getElementsByTagName('script').length;")).intValue();
    }

    private static Object parseJSON(String s) throws Exception {
        return Utils.executeScript(
            JSONTest.class, 
            "return window.JSON.parse(arguments[0]);", s);
    }
    
    @KOTest public void loadAndParseJSONSentToArray() throws InterruptedException {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "{'firstName': 'Sitar', 'sex': 'MALE'}", 
                "application/json"
            );
            
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            js.fetchArray(url);
        }
        
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
    }
    
    @KOTest public void loadAndParseJSONArraySingle() throws InterruptedException {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "[{'firstName': 'Gitar', 'sex': 'FEMALE'}]", 
                "application/json"
            );
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
        
            js.setFetched(null);
            js.fetch(url);
        }
        
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Gitar".equals(p.getFirstName()) : "Expecting Gitar: " + p.getFirstName();
        assert Sex.FEMALE.equals(p.getSex()) : "Expecting FEMALE: " + p.getSex();
    }
    
    @KOTest public void loadAndParseArrayInPeople() throws InterruptedException {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "{'info':[{'firstName': 'Gitar', 'sex': 'FEMALE'}]}", 
                "application/json"
            );
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
        
            js.fetchPeople(url);
        }
        
        if (0 == js.getFetchedCount()) {
            throw new InterruptedException();
        }

        assert js.getFetchedCount() == 1 : "One person loaded: " + js.getFetchedCount();
        
        Person p = js.getFetched();
        
        assert p != null : "We should get our person back: " + p;
        assert "Gitar".equals(p.getFirstName()) : "Expecting Gitar: " + p.getFirstName();
        assert Sex.FEMALE.equals(p.getSex()) : "Expecting FEMALE: " + p.getSex();
    }
    
    @KOTest public void loadAndParseArrayOfIntegers() throws InterruptedException {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "{'age':[1, 2, 3]}", 
                "application/json"
            );
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
        
            js.fetchPeopleAge(url);
        }
        
        if (0 == js.getFetchedCount()) {
            throw new InterruptedException();
        }

        assert js.getFetchedCount() == 6 : "1 + 2 + 3 is " + js.getFetchedCount();
    }
    
    @OnReceive(url="{url}")
    static void fetchPeopleSex(People p, JSONik model) {
        model.setFetchedCount(1);
        model.getFetchedSex().addAll(p.getSex());
    }
    
    @KOTest public void loadAndParseArrayOfEnums() throws InterruptedException {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "{'sex':['FEMALE', 'MALE', 'MALE']}", 
                "application/json"
            );
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
        
            js.fetchPeopleSex(url);
        }
        
        if (0 == js.getFetchedCount()) {
            throw new InterruptedException();
        }

        assert js.getFetchedCount() == 1 : "Loaded";
        
        assert js.getFetchedSex().size() == 3 : "Three values " + js.getFetchedSex();
        assert js.getFetchedSex().get(0) == Sex.FEMALE : "Female first " + js.getFetchedSex();
        assert js.getFetchedSex().get(1) == Sex.MALE : "male 2nd " + js.getFetchedSex();
        assert js.getFetchedSex().get(2) == Sex.MALE : "male 3rd " + js.getFetchedSex();
    }
    
    @KOTest public void loadAndParseJSONArray() throws InterruptedException {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "[{'firstName': 'Gitar', 'sex': 'FEMALE'},"
                + "{'firstName': 'Peter', 'sex': 'MALE'}"
                + "]", 
                "application/json"
            );
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
            js.setFetched(null);
            
            js.fetchArray(url);
        }
        
        
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert js.getFetchedCount() == 2 : "We got two values: " + js.getFetchedCount();
        assert "Gitar".equals(p.getFirstName()) : "Expecting Gitar: " + p.getFirstName();
        assert Sex.FEMALE.equals(p.getSex()) : "Expecting FEMALE: " + p.getSex();
    }
    
    @KOTest public void loadError() throws InterruptedException {
        if (js == null) {
            js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
            js.setFetched(null);
            
            js.fetchArray("http://127.0.0.1:54253/does/not/exist.txt");
        }
        
        
        if (js.getFetchedResponse() == null) {
            throw new InterruptedException();
        }

        assert "Exception".equals(js.getFetchedResponse()) : js.getFetchedResponse();
    }
    
    @Model(className = "NameAndValue", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "value", type = long.class),
        @Property(name = "small", type = byte.class)
    })
    static class NandV {
    }
    
    @KOTest public void parseNullNumber() throws Exception {
        String txt = "{ \"name\":\"M\" }";
        ByteArrayInputStream is = new ByteArrayInputStream(txt.getBytes("UTF-8"));
        NameAndValue v = Models.parse(newContext(), NameAndValue.class, is);
        assert "M".equals(v.getName()) : "Name is 'M': " + v.getName();
        assert 0 == v.getValue() : "Value is empty: " + v.getValue();
        assert 0 == v.getSmall() : "Small value is empty: " + v.getSmall();
    }

    @KOTest public void deserializeWrongEnum() throws Exception {
        PrintStream prev = null;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try {
            prev = System.err;
            System.setErr(new PrintStream(err));
        } catch (LinkageError e) {
            err = null;
            prev = null;
        }
        
        String str = "{ \"sex\" : \"unknown\" }";
        ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
        Person p = Models.parse(newContext(), Person.class, is);
        assert p.getSex() == null : "Wrong sex means null, but was: " + p.getSex();
        
        if (err != null) {
            assert err.toString().contains("Sex.unknown") : "Expecting error: " + err.toString();
        }
        if (prev != null) {
            try {
                System.setErr(prev);
            } catch (LinkageError e) {
                // ignore
            }
        }
    }

    
    private static BrwsrCtx newContext() {
        return Utils.newContext(JSONTest.class);
    }
    
}
