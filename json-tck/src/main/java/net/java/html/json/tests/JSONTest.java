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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import static net.java.html.json.tests.Utils.assertEquals;
import static net.java.html.json.tests.Utils.assertNotNull;
import static net.java.html.json.tests.Utils.assertNull;
import static net.java.html.json.tests.Utils.assertTrue;
import org.netbeans.html.json.tck.KOTest;

/** Need to verify that models produce reasonable JSON objects.
 *
 * @author Jaroslav Tulach
 */
@Model(className = "JSONik", targetId = "", properties = {
    @Property(name = "fetched", type = Person.class),
    @Property(name = "fetchedCount", type = int.class),
    @Property(name = "fetchedResponse", type = String.class),
    @Property(name = "fetchedSex", type = Sex.class, array = true)
})
public final class JSONTest {
    private final PhaseExecutor[] phases = { null };
    private static class Data {
        final JSONik js;
        final Integer orig;
        final String url;
        final BrwsrCtx ctx;

        Data(JSONik js, Integer orig, String url, BrwsrCtx ctx) {
            this.js = js;
            this.orig = orig;
            this.url = url;
            this.ctx = ctx;
        }
    }

    @ModelOperation static void assignFetched(JSONik m, Person p) {
        m.setFetched(p);
    }

    @KOTest
    public void toJSONInABrowser() throws Throwable {
        Person p = Models.bind(new Person(), newContext());
        p.setSex(Sex.MALE);
        p.setFirstName("Jára");
        p.setLastName("Tulachů");

        Object json;
        try {
            json = parseJSON(p.toString());
        } catch (Throwable ex) {
            throw new IllegalStateException("Can't parse " + p).initCause(ex);
        }

        Person p2 = Models.fromRaw(newContext(), Person.class, json);

        assertEquals(p2.getFirstName(), p.getFirstName(),
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName());
    }

    @KOTest public void fromJsonWithUTF8() throws Throwable {
        final BrwsrCtx c = newContext();
        Person p = Models.bind(new Person(), c);
        p.setSex(Sex.MALE);
        p.setFirstName("Jára");
        p.setLastName("Tulachů");

        byte[] arr = p.toString().getBytes("UTF-8");
        Person p2 = Models.parse(c, Person.class, new ByteArrayInputStream(arr));

        assertEquals(p2.getFirstName(), p.getFirstName(),
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName());
    }

    @KOTest public void fromJsonEmptyValues() throws Throwable {
        final BrwsrCtx c = newContext();
        Person p = Models.bind(new Person(), c);
        p.setSex(Sex.MALE);
        p.setFirstName("");
        p.setLastName("");

        byte[] arr = p.toString().getBytes("UTF-8");
        Person p2 = Models.parse(c, Person.class, new ByteArrayInputStream(arr));

        assertEquals(p2.getFirstName(), p.getFirstName(),
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName());
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

        assertEquals(p2.getFirstName(), p.getFirstName(),
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName());
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

        assertEquals(p2.getFirstName(), p.getFirstName(),
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName());
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

        assertEquals(p2.getFirstName(), p.getFirstName(),
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName());
    }

    private static BrwsrCtx onCallback;

    @OnReceive(url="{url}")
    static void fetch(JSONik model, Person p) {
        model.setFetched(p);
        onCallback = BrwsrCtx.findDefault(model.getClass());
    }

    @OnReceive(url="{url}")
    static void fetchPlain(JSONik model, String p) {
        onCallback = BrwsrCtx.findDefault(model.getClass());
        model.setFetchedResponse(p);
    }

    @OnReceive(url="{url}", onError = "setMessage")
    static void fetchArray(JSONik model, Person[] p) {
        model.setFetchedCount(p.length);
        model.setFetched(p[0]);
        onCallback = BrwsrCtx.findDefault(model.getClass());
    }

    static void setMessage(JSONik m, Exception t) {
        assertNotNull(t, "Exception provided");
        m.setFetchedResponse("Exception");
    }

    @OnReceive(url="{url}")
    static void fetchPeople(JSONik model, People p) {
        final int size = p.getInfo().size();
        if (size > 0) {
            model.setFetched(p.getInfo().get(0));
        }
        model.setFetchedCount(size);
    }

    @OnReceive(url="{url}")
    static void fetchPeopleAge(JSONik model, People p) {
        int sum = 0;
        for (int a : p.getAge()) {
            sum += a;
        }
        model.setFetchedCount(sum);
    }

    @KOTest
    public void loadAndParseJSON() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "{'firstName': 'Sitar', 'sex': 'MALE'}",
                "application/json"
            );
            var ctx = newContext();
            var js = Models.bind(new JSONik(), ctx);
            js.applyBindings();

            js.setFetched(null);
            js.fetch(url);
            return new Data(js, 0, url, ctx);
        }).then((d) -> {
            Person p = d.js.getFetched();
            assertEquals("Sitar", p.getFirstName(), "Expecting Sitar: " + p.getFirstName());
            assertEquals(Sex.MALE, p.getSex(), "Expecting MALE: " + p.getSex());
            assertEquals(d.ctx, onCallback, "Context is the same");
        }).start();
    }

    @KOTest
    public void loadAndParsePlainText() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "{'firstName': 'Sitar', 'sex': 'MALE'}",
                "text/plain"
            );
            var ctx = newContext();
            var js = Models.bind(new JSONik(), ctx);
            js.applyBindings();

            js.setFetched(null);
            js.fetchPlain(url);
            return new Data(js, 0, url, ctx);
        }).then((data) -> {
            String s = data.js.getFetchedResponse();
            assertTrue(s.contains("Sitar"), "The text contains Sitar value: " + s);
            assertTrue(s.contains("MALE"), "The text contains MALE value: " + s);
            Person p = Models.parse(data.ctx, Person.class, new ByteArrayInputStream(s.getBytes()));
            assertEquals("Sitar", p.getFirstName(), "Expecting Sitar: " + p.getFirstName());
            assertEquals(Sex.MALE, p.getSex(), "Expecting MALE: " + p.getSex());
            assertEquals(data.ctx, onCallback, "Same context");
        }).start();
    }

    @KOTest
    public void loadAndParsePlainTextOnArray() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "[ {'firstName': 'Sitar', 'sex': 'MALE'} ]",
                "text/plain"
            );
            var ctx = newContext();
            var js = Models.bind(new JSONik(), ctx);
            js.applyBindings();

            js.setFetched(null);
            js.fetchPlain(url);
            return new Data(js, 0, url, ctx);
        }).then((data) -> {
            String s = data.js.getFetchedResponse();
            if (s == null) {
                throw new InterruptedException();
            }

            assertTrue(s.contains("Sitar"), "The text contains Sitar value: " + s);
            assertTrue(s.contains("MALE"), "The text contains MALE value: " + s);

            Person p = Models.parse(data.ctx, Person.class, new ByteArrayInputStream(s.getBytes()));

            assertEquals("Sitar", p.getFirstName(), "Expecting Sitar: " + p.getFirstName());
            assertEquals(Sex.MALE, p.getSex(), "Expecting MALE: " + p.getSex());

            assertEquals(data.ctx, onCallback, "Same context");
        }).start();
    }

    @OnReceive(url="{url}?callme={me}", jsonp = "me")
    static void fetchViaJSONP(JSONik model, Person p) {
        model.setFetched(p);
    }

    @KOTest
    public void loadAndParseJSONP() throws InterruptedException, Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "$0({'firstName': 'Mitar', 'sex': 'MALE'})",
                "application/javascript",
                "callme"
            );
            var orig = scriptElements();
            assertTrue(orig > 0, "There should be some scripts on the page");

            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            js.fetchViaJSONP(url);
            return new Data(js, orig, url, null);
        }).then((data) -> {
            Person p = data.js.getFetched();
            if (p == null) {
                throw new InterruptedException();
            }

            assertEquals("Mitar", p.getFirstName(), "Unexpected: " + p.getFirstName());
            assertEquals(Sex.MALE, p.getSex(), "Expecting MALE: " + p.getSex());

            int now = scriptElements();

            assertEquals(data.orig, now, "The set of elements is unchanged. Delta: " + (now - data.orig));
        }).start();
    }

    @OnReceive(url="{url}", method = "PUT", data = Person.class)
    static void putPerson(JSONik model, String reply) {
        model.setFetchedCount(1);
        model.setFetchedResponse(reply);
    }

    @KOTest
    public void putPeopleUsesRightMethod() throws InterruptedException, Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "$0\n$1",
                "text/plain",
                "http.method", "http.requestBody"
            );
            var orig = scriptElements();
            assertTrue(orig > 0, "There should be some scripts on the page");

            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            Person p = Models.bind(new Person(), BrwsrCtx.EMPTY);
            p.setFirstName("Jarda");
            js.putPerson(url, p);

            return new Data(js, orig, url, null);
        }).then((data) -> {
            var js = data.js;
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

            assertEquals("PUT", res, "Server was queried with PUT method: " + js.getFetchedResponse());

            assertTrue(msg.contains("Jarda"), "Data transferred to the server: " + msg);
        }).start();
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

    @KOTest
    public void loadAndParseJSONSentToArray() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "{'firstName': 'Sitar', 'sex': 'MALE'}",
                "application/json"
            );

            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            js.fetchArray(url);
            return new Data(js, 0, url, null);
        }).then((data) -> {
            Person p = data.js.getFetched();
            if (p == null) {
                throw new InterruptedException();
            }

            assertEquals("Sitar", p.getFirstName(), "Expecting Sitar: " + p.getFirstName());
            assertEquals(Sex.MALE, p.getSex(), "Expecting MALE: " + p.getSex());
        }).start();
    }

    @KOTest
    public void loadAndParseJSONArraySingle() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "[{'firstName': 'Gitar', 'sex': 'FEMALE'}]",
                "application/json"
            );
            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            js.fetch(url);
            return new Data(js, 0, url, null);
        }).then((data) -> {
            Person p = data.js.getFetched();
            if (p == null) {
                throw new InterruptedException();
            }

            assertEquals("Gitar", p.getFirstName(), "Expecting Gitar: " + p.getFirstName());
            assertEquals(Sex.FEMALE, p.getSex(), "Expecting FEMALE: " + p.getSex());
        }).start();
    }

    @KOTest
    public void loadAndParseArrayInPeople() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "{'info':[{'firstName': 'Gitar', 'sex': 'FEMALE'}]}",
                "application/json"
            );
            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.fetchPeople(url);
            return new Data(js, 0, url, null);
        }).then((data) -> {
            var js = data.js;
            if (0 == js.getFetchedCount()) {
                throw new InterruptedException();
            }

            assertEquals(js.getFetchedCount(), 1, "One person loaded: " + js.getFetchedCount());

            Person p = js.getFetched();

            assertNotNull(p, "We should get our person back: " + p);
            assertEquals("Gitar", p.getFirstName(), "Expecting Gitar: " + p.getFirstName());
            assertEquals(Sex.FEMALE, p.getSex(), "Expecting FEMALE: " + p.getSex());
        }).start();
    }

    @KOTest
    public void loadAndParseArrayInPeopleWithHeaders() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "{'info':[{'firstName': '$0$1$2$3$4', 'sex': 'FEMALE'}]}",
                "application/json",
                "http.header.Easy",
                "http.header.H-a!r*d^e.r",
                "http.header.Repeat-ed",
                "http.header.Repeat*ed",
                "http.header.Same-URL"
            );
            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.fetchPeopleWithHeaders(url, "easy", "harder", "rep");
            return new Data(js, 0, url, null);
        }).then((data) -> {
            var js = data.js;
            var url = data.url;
            if (0 == js.getFetchedCount()) {
                throw new InterruptedException();
            }

            assertEquals(js.getFetchedCount(), 1, "One person loaded: " + js.getFetchedCount());

            Person p = js.getFetched();

            assertNotNull(p, "We should get our person back: " + p);
            assertEquals("easyharderreprep" + url, p.getFirstName(), "Expecting header mess: " + p.getFirstName());
            assertEquals(Sex.FEMALE, p.getSex(), "Expecting FEMALE: " + p.getSex());
        }).start();
    }

    @OnReceive(url="{url}", headers={
        "Easy: {easy}",
        "H-a!r*d^e.r: {harder}",
        "Repeat-ed: {rep}",
        "Repeat*ed: {rep}",
        "Same-URL: {url}"
    })
    static void fetchPeopleWithHeaders(JSONik model, People p) {
        final int size = p.getInfo().size();
        if (size > 0) {
            model.setFetched(p.getInfo().get(0));
        }
        model.setFetchedCount(size);
    }

    @KOTest
    public void loadAndParseArrayOfIntegers() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "{'age':[1, 2, 3]}",
                "application/json"
            );
            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.fetchPeopleAge(url);
            return new Data(js, 0, url, null);
        }).then((data) -> {
            var js = data.js;
            if (0 == js.getFetchedCount()) {
                throw new InterruptedException();
            }

            assertEquals(js.getFetchedCount(), 6, "1 + 2 + 3 is " + js.getFetchedCount());
        }).start();
    }

    @OnReceive(url="{url}")
    static void fetchPeopleSex(JSONik model, People p) {
        model.setFetchedCount(1);
        model.getFetchedSex().addAll(p.getSex());
    }

    @KOTest public void loadAndParseArrayOfEnums() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "{'sex':['FEMALE', 'MALE', 'MALE']}",
                "application/json"
            );
            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();

            js.fetchPeopleSex(url);
            return new Data(js, 0, url, null);
        }).then((data) -> {
            var js = data.js;
            if (0 == js.getFetchedCount()) {
                throw new InterruptedException();
            }

            assertEquals(js.getFetchedCount(), 1, "Loaded");

            assertEquals(js.getFetchedSex().size(), 3, "Three values " + js.getFetchedSex());
            assertEquals(js.getFetchedSex().get(0), Sex.FEMALE, "Female first " + js.getFetchedSex());
            assertEquals(js.getFetchedSex().get(1), Sex.MALE, "male 2nd " + js.getFetchedSex());
            assertEquals(js.getFetchedSex().get(2), Sex.MALE, "male 3rd " + js.getFetchedSex());
        }).start();
    }

    @KOTest public void loadAndParseJSONArray() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var url = Utils.prepareURL(
                JSONTest.class, "[{'firstName': 'Gitar', 'sex': 'FEMALE'},"
                + "{'firstName': 'Peter', 'sex': 'MALE'}"
                + "]",
                "application/json"
            );
            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
            js.setFetched(null);

            js.fetchArray(url);
            return new Data(js, 0, url, null);
        }).then((data) -> {
            var js = data.js;
            Person p = js.getFetched();
            if (p == null) {
                throw new InterruptedException();
            }

            assertEquals(js.getFetchedCount(), 2, "We got two values: " + js.getFetchedCount());
            assertEquals("Gitar", p.getFirstName(), "Expecting Gitar: " + p.getFirstName());
            assertEquals(Sex.FEMALE, p.getSex(), "Expecting FEMALE: " + p.getSex());
        }).start();
    }

    @KOTest
    public void loadError() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            var js = Models.bind(new JSONik(), newContext());
            js.applyBindings();
            js.setFetched(null);

            js.fetchArray("http://127.0.0.1:54253/does/not/exist.txt");
            return new Data(js, 0, null, null);
        }).then((data) -> {
            var js = data.js;
            if (js.getFetchedResponse() == null) {
                throw new InterruptedException();
            }
            assertEquals("Exception", js.getFetchedResponse(), "Response " + js.getFetchedResponse());
        }).start();
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
        assertEquals("M", v.getName(), "Name is 'M': " + v.getName());
        assertEquals(0L, v.getValue(), "Value is empty: " + v.getValue());
        assertEquals((byte)0, v.getSmall(), "Small value is empty: " + v.getSmall());
    }

    @KOTest public void deserializeWrongEnum() throws Exception {
        PrintStream prev;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try {
            prev = System.err;
            System.setErr(new PrintStream(err));
        } catch (Throwable e) {
            err = null;
            prev = null;
        }

        String str = "{ \"sex\" : \"unknown\" }";
        ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
        Person p = Models.parse(newContext(), Person.class, is);
        assertNull(p.getSex(), "Wrong sex means null, but was: " + p.getSex());

        if (err != null) {
            assertTrue(err.toString().contains("unknown") && err.toString().contains("Sex"), "Expecting error: " + err.toString());
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
