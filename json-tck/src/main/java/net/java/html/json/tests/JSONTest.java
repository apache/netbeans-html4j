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
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import org.apidesign.html.json.impl.JSON;
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
        
        Person p2 = JSON.read(newContext(), Person.class, json);
        
        assert p2.getFirstName().equals(p.getFirstName()) : 
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName();
    }
    
    @OnReceive(url="{url}")
    static void fetch(Person p, JSONik model) {
        model.setFetched(p);
    }

    @OnReceive(url="{url}")
    static void fetchArray(Person[] p, JSONik model) {
        model.setFetchedCount(p.length);
        model.setFetched(p[0]);
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

    private static BrwsrCtx newContext() {
        return Utils.newContext(JSONTest.class);
    }
    
}
