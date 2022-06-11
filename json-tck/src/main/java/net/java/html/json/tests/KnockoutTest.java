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

import java.util.List;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.netbeans.html.json.tck.KOTest;
import static net.java.html.json.tests.Utils.assertEquals;
import static net.java.html.json.tests.Utils.assertNotNull;
import static net.java.html.json.tests.Utils.assertTrue;
import static net.java.html.json.tests.Utils.assertFalse;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className="KnockoutModel", targetId = "", properties={
    @Property(name="name", type=String.class),
    @Property(name="results", type=String.class, array = true),
    @Property(name="numbers", type=int.class, array = true),
    @Property(name="callbackCount", type=int.class),
    @Property(name="people", type=PersonImpl.class, array = true),
    @Property(name="enabled", type=boolean.class),
    @Property(name="latitude", type=double.class),
    @Property(name="choice", type=KnockoutTest.Choice.class),
    @Property(name="archetype", type=ArchetypeData.class),
    @Property(name="archetypes", type=ArchetypeData.class, array = true),
})
public final class KnockoutTest {
    private PhaseExecutor[] phases = new PhaseExecutor[1];

    enum Choice {
        A, B;
    }

    @ComputedProperty static List<Integer> resultLengths(List<String> results) {
        Integer[] arr = new Integer[results.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = results.get(i).length();
        }
        return Models.asList(arr);
    }

    @KOTest
    public void modifyValueAssertChangeInModelOnEnum() throws Throwable {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class,
                "Latitude: <input id='input' data-bind=\"value: choice\"></input>\n"
            );
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setChoice(Choice.A);
            m.applyBindings();
            return m;
        }).then((m) -> {
            String v = getSetInput("input", null);
            assertEquals("A", v, "Value is really A: " + v);

            getSetInput("input", "B");
            triggerEvent("input", "change");
        }).then((m) -> {
            assertEquals(Choice.B, m.getChoice(), "Enum property updated: " + m.getChoice());
        }).then((data) -> {
        }).finalize((data) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }


    @KOTest
    public void modifyRadioValueOnEnum() throws Throwable {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <input id='i1' type="radio" name="choice" value="A" data-bind="checked: choice"></input>Right
            <input id='input' type="radio" name="choice" value="B" data-bind="checked: choice"></input>Never
            """);
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setChoice(Choice.B);
            m.applyBindings();
            return m;
        }).then((m) -> {
            assertFalse(isChecked("i1"), "B should be checked now");
            assertTrue(isChecked("input"), "B should be checked now");

            triggerEvent("i1", "click");
        }).then((m) -> {
            assertEquals(Choice.A, m.getChoice(), "Switched to A");
            assertTrue(isChecked("i1"), "A should be checked now");
            assertFalse(isChecked("input"), "A should be checked now");

            triggerEvent("input", "click");
        }).then((m) -> {
            assertEquals(Choice.B, m.getChoice(), "Enum property updated: " + m.getChoice());
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest public void modifyValueAssertChangeInModelOnDouble() throws Throwable {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class,
                "Latitude: <input id='input' data-bind=\"value: latitude\"></input>\n"
            );
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setLatitude(50.5);
            m.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("50.5", v, "Value is really 50.5: " + v);

            getSetInput("input", "49.5");
            triggerEvent("input", "change");
            return m;
        }).then((m) -> {
            assertEquals(49.5, m.getLatitude(), "Double property updated: " + m.getLatitude());
        }).finalize((data) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void rawObject() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            final BrwsrCtx ctx = newContext();
            Person p1 = Models.bind(new Person(), ctx);
            p1.setFirstName("Jarda");
            p1.setLastName("Tulach");
            Object raw = Models.toRaw(p1);
            Person p2 = Models.fromRaw(ctx, Person.class, raw);

            assertEquals(p2.getFirstName(), "Jarda", "First name");
            assertEquals(p2.getLastName(), "Tulach", "Last name");

            p2.setFirstName("Jirka");
            assertEquals(p2.getFirstName(), "Jirka", "First name updated");

            var js = new KnockoutModel();
            js.getPeople().add(p1);
            js.getPeople().add(p2);

            return js;
        }).then((js) -> {
            Person p1 = js.getPeople().get(0);

            if (js.getPeople().size() == 2) {
                if (!"Jirka".equals(p1.getFirstName())) {
                    throw new InterruptedException();
                }

                assertEquals(p1.getFirstName(), "Jirka", "First name updated in original object");

                p1.setFirstName("Ondra");
                assertEquals(p1.getFirstName(), "Ondra", "1st name updated in original object");

                js.getPeople().add(p1);
            }
        }).then((js) -> {
            Person p2 = js.getPeople().get(1);
            if (!"Ondra".equals(p2.getFirstName())) {
                throw new InterruptedException();
            }
            assertEquals(p2.getFirstName(), "Ondra", "1st name updated in copied object");
        }).start();
    }

    @KOTest
    public void modifyComputedProperty() throws Throwable {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            Full name: <div data-bind='with:firstPerson'>
            <input id='input' data-bind="value: fullName"></input>
            </div>
            """);
            KnockoutModel m = new KnockoutModel();
            m.getPeople().add(new Person());

            m = Models.bind(m, newContext());
            m.getFirstPerson().setFirstName("Jarda");
            m.getFirstPerson().setLastName("Tulach");
            m.applyBindings();
            return m;
        }).then((m) -> {
            String v = getSetInput("input", null);
            assertEquals("Jarda Tulach", v, "Value: " + v);

            getSetInput("input", "Mickey Mouse");
            triggerEvent("input", "change");
        }).then((m) -> {
            assertEquals("Mickey", m.getFirstPerson().getFirstName(), "First name updated");
            assertEquals("Mouse", m.getFirstPerson().getLastName(), "Last name updated");
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void modifyValueAssertChangeInModelOnBoolean() throws Throwable {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class,
                "Latitude: <input id='input' data-bind=\"value: enabled\"></input>\n"
            );
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setEnabled(true);
            m.applyBindings();
            return m;
        }).then((m) -> {
            String v = getSetInput("input", null);
            assertEquals("true", v, "Value is really true: " + v);

            getSetInput("input", "false");
            triggerEvent("input", "change");
        }).then((m) -> {
            assertFalse(m.isEnabled(), "Boolean property updated: " + m.isEnabled());
        }).then((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void modifyValueAssertChangeInModel() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
                <h1 data-bind="text: helloMessage">Loading Bck2Brwsr's Hello World...</h1>
                Your name: <input id='input' data-bind="value: name"></input>
                <button id="hello">Say Hello!</button>
                """
            );
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setName("Kukuc");
            m.applyBindings();
            return m;
        }).then((m) -> {
            String v = getSetInput("input", null);
            assertEquals("Kukuc", v, "Value is really kukuc: " + v);
        }).then((m) -> {
            getSetInput("input", "Jardo");
            triggerEvent("input", "change");
        }).then((m) -> {
            assertEquals("Jardo", m.getName(), "Name property updated: " + m.getName());
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    private static String getSetSelected(int index, Object value) throws Exception {
        String s = """
                   var index = arguments[0];
                   var n = window.document.getElementById('input');
                    if (index >= 0) {
                       n.options.selectedIndex = index;
                       ko.utils.triggerEvent(n, 'change');
                   }
                    var op = n.options[n.selectedIndex];
                   return op ? op.text : n.selectedIndex;
                   """;
        Object ret = Utils.executeScript(
            KnockoutTest.class,
            s, index, value
        );
        return ret == null ? null : ret.toString();
    }

    @Model(className = "ArchetypeData", properties = {
        @Property(name = "artifactId", type = String.class),
        @Property(name = "groupId", type = String.class),
        @Property(name = "version", type = String.class),
        @Property(name = "name", type = String.class),
        @Property(name = "description", type = String.class),
        @Property(name = "url", type = String.class),
    })
    static class ArchModel {
    }

    @KOTest public void selectWorksOnModels() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Utils.exposeHTML(KnockoutTest.class, """
            <select id='input' data-bind="options: archetypes,
                                   optionsText: 'name',
                                   value: archetype">
                              </select>
            """);

            KnockoutModel km = new KnockoutModel();
            km.getArchetypes().add(new ArchetypeData("ko4j", "org.netbeans.html", "0.8.3", "ko4j", "ko4j", null));
            km.getArchetypes().add(new ArchetypeData("crud", "org.netbeans.html", "0.8.3", "crud", "crud", null));
            km.getArchetypes().add(new ArchetypeData("3rd", "org.netbeans.html", "0.8.3", "3rd", "3rd", null));
            var js = Models.bind(km, newContext());
            js.setArchetype(js.getArchetypes().get(1));
            js.applyBindings();
            return js;
        }).then((js) -> {
            String v = getSetSelected(-1, null);
            assertEquals("crud", v, "Second index (e.g. crud) is selected: " + v);

            String sel = getSetSelected(2, Models.toRaw(js.getArchetypes().get(2)));
            assertEquals("3rd", sel, "3rd is selected now: " + sel);
        }).then((js) -> {
            if (js.getArchetype() != js.getArchetypes().get(2)) {
                throw new InterruptedException();
            }
        }).finalize((js) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest public void nestedObjectEqualsChange() throws Exception {
        nestedObjectEqualsChange(true);
    }

    @KOTest public void nestedObjectChange() throws Exception {
        nestedObjectEqualsChange(false);
    }
    private void nestedObjectEqualsChange(boolean preApply) throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Utils.exposeHTML(KnockoutTest.class, """
            <div data-bind='with: archetype'>
                <input id='input' data-bind='value: groupId'></input>
            </div>
            """);

            var js = Models.bind(new KnockoutModel(), newContext());
            if (preApply) {
                js.applyBindings();
            }
            js.setArchetype(Models.bind(new ArchetypeData(), newContext()));
            js.getArchetype().setGroupId("org.netbeans.html");
            js.applyBindings();
            return js;
        }).then((js) -> {
            String v = getSetInput("input", null);
            assertEquals("org.netbeans.html", v, "groupId has been changed");
        }).finalize((js) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void modifyValueAssertAsyncChangeInModel() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Utils.exposeHTML(KnockoutTest.class, """
            <h1 data-bind="text: helloMessage">Loading Bck2Brwsr's Hello World...</h1>
            Your name: <input id='input' data-bind="value: name"></input>
            <button id="hello">Say Hello!</button>
            """);

            var js = Models.bind(new KnockoutModel(), newContext());
            js.setName("Kukuc");
            js.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("Kukuc", v, "Value is really kukuc: " + v);

            Utils.scheduleLater(1, new Runnable() {
                @Override
                public void run() {
                    js.setName("Jardo");
                }
            });
            return js;
        }).then((data) -> {
            String v = getSetInput("input", null);
            if (!"Jardo".equals(v)) {
                throw new InterruptedException();
            }
        }).finalize((data) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @Model(className = "ConstantModel", targetId = "", builder = "assign", properties = {
        @Property(name = "doubleValue", mutable = false, type = double.class),
        @Property(name = "stringValue", mutable = false, type = String.class),
        @Property(name = "boolValue", mutable = false, type = boolean.class),
        @Property(name = "intArray", mutable = false, type = int.class, array = true),
    })
    static class ConstantCntrl {
    }

    @KOTest public void nonMutableDouble() throws Exception {
        Utils.exposeTypeOf(KnockoutTest.class);
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: getTypeof(doubleValue)\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignDoubleValue(10.0);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "number", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void nonMutableString() throws Exception {
        Utils.exposeTypeOf(KnockoutTest.class);
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: getTypeof(stringValue)\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignDoubleValue(10.0);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "string", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void nonMutableBoolean() throws Exception {
        Utils.exposeTypeOf(KnockoutTest.class);
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: getTypeof(boolValue)\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignBoolValue(true);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "boolean", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void nonMutableIntArray() throws Exception {
        Utils.exposeTypeOf(KnockoutTest.class);
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: getTypeof(intArray)\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignDoubleValue(Long.MAX_VALUE).assignIntArray(1, 2, 3, 4);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "object", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    private static String getSetInput(String id, String value) throws Exception {
        String s = """
                   var value = arguments[0];
                   var n = window.document.getElementById(arguments[1]);
                    if (value != null) n['value'] = value;
                    return n['value'];""";
        Object ret = Utils.executeScript(
            KnockoutTest.class,
            s, value, id
        );
        return ret == null ? null : ret.toString();
    }

    private static boolean isChecked(String id) throws Exception {
        String s = """
                   var n = window.document.getElementById(arguments[0]);
                    return n['checked'];""";
        Object ret = Utils.executeScript(
            KnockoutTest.class,
            s, id
        );
        return Boolean.TRUE.equals(ret);
    }

    public static void triggerEvent(String id, String ev) throws Exception {
        Utils.executeScript(
            KnockoutTest.class,
            "ko.utils.triggerEvent(window.document.getElementById(arguments[0]), arguments[1]);",
            id, ev
        );
    }

    @KOTest
    public void displayContentOfArray() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <ul id='ul' data-bind='foreach: results'>
              <li data-bind='text: $data, click: $root.call'/>
            </ul>
            """);
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);
            m.getResults().add("Hi");
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);
            triggerChildClick("ul", 1);
        }).then((m) -> {
            assertEquals(1, m.getCallbackCount(), "One callback " + m.getCallbackCount());
            assertEquals("Hi", m.getName(), "We got callback from 2nd child " + m.getName());
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest public void displayContentOfAsyncArray() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Utils.exposeHTML(KnockoutTest.class, """
            <ul id='ul' data-bind='foreach: results'>
              <li data-bind='text: $data, click: $root.call'/>
            </ul>
            """);
            var js = Models.bind(new KnockoutModel(), newContext());
            js.getResults().add("Ahoj");
            js.applyBindings();
            return js;
        }).then((js) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            Utils.scheduleLater(1, new Runnable() {
                @Override
                public void run() {
                    js.getResults().add("Hi");
                }
            });
        }).then((js) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            if (cnt != 2) {
                throw new InterruptedException();
            }

            triggerChildClick("ul", 1);
        }).then((js) -> {
            assertEquals(1, js.getCallbackCount(), "One callback " + js.getCallbackCount());
            assertEquals("Hi", js.getName(), "We got callback from 2nd child " + js.getName());
        }).finalize((js) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void displayContentOfComputedArray() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <ul id='ul' data-bind='foreach: bothNames'>
              <li data-bind='text: $data, click: $root.assignFirstName'/>
            </ul>
            """);


            Pair m = Models.bind(new Pair("First", "Last", null), newContext());
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);
        }).then((m) -> {
            assertEquals("Last", m.getFirstName(), "We got callback from 2nd child " + m.getFirstName());

            m.setLastName("Verylast");

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);
        }).then((m) -> {
            assertEquals("Verylast", m.getFirstName(), "We got callback from 2nd child " + m.getFirstName());
        }).finalize((m) -> {
           Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void displayContentOfComputedArrayOnASubpair() throws Exception {
        class ModelCtx {
            final Pair m;
            final BrwsrCtx ctx;

            ModelCtx(Pair m, BrwsrCtx ctx) {
                this.m = m;
                this.ctx = ctx;
            }
        }
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <div data-bind='with: next'>
            <ul id='ul' data-bind='foreach: bothNames'>
              <li data-bind='text: $data, click: $root.assignFirstName'/>
            </ul></div>
            """);
            final BrwsrCtx ctx = newContext();
            Pair m = Models.bind(new Pair(null, null, new Pair("First", "Last", null)), ctx);
            m.applyBindings();
            return new ModelCtx(m, ctx);
        }).then((data) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);
        }).then((data) -> {
            assertEquals(PairModel.ctx, data.ctx, "Context remains the same");

            assertEquals("Last", data.m.getFirstName(), "We got callback from 2nd child " + data.m.getFirstName());
        }).finalize((data) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void displayContentOfComputedArrayOnComputedASubpair() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <div data-bind='with: nextOne'>
            <ul id='ul' data-bind='foreach: bothNames'>
              <li data-bind='text: $data, click: $root.assignFirstName'/>
            </ul></div>
            """);
            Pair m = Models.bind(new Pair(null, null, new Pair("First", "Last", null)), newContext());
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);
        }).then((m) -> {
            assertEquals("Last", m.getFirstName(), "We got callback from 2nd child " + m.getFirstName());
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void checkBoxToBooleanBinding() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class,
                "<input type='checkbox' id='b' data-bind='checked: enabled'></input>\n"
            );
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.applyBindings();

            assertFalse(m.isEnabled(), "Is disabled");

            triggerClick("b");
            return m;
        }).then((m) -> {
            assertTrue(m.isEnabled(), "Now the model is enabled");
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }



    @KOTest
    public void displayContentOfDerivedArray() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <ul id='ul' data-bind='foreach: cmpResults'>
              <li><b data-bind='text: $data'></b></li>
            </ul>
            """);
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            m.getResults().add("hello");
        }).then((m) -> {
            var cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest
    public void displayContentOfArrayOfPeople() throws Exception {
        class Data {
            final KnockoutModel m;
            final BrwsrCtx c;
            final Person first;

            Data(KnockoutModel m, BrwsrCtx c, Person first) {
                this.m = m;
                this.c = c;
                this.first = first;
            }
        }
        PhaseExecutor.schedule(phases, () -> {
                Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <ul id='ul' data-bind='foreach: people'>
              <li data-bind='text: $data.firstName, click: $root.removePerson'></li>
            </ul>
            """);

            var c = newContext();
            var m = Models.bind(new KnockoutModel(), c);

            var first = Models.bind(new Person(), c);
            first.setFirstName("first");
            m.getPeople().add(first);

            m.applyBindings();

            return new Data(m, c, first);
        }).then((test) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            final Person second = Models.bind(new Person(), test.c);
            second.setFirstName("second");
            test.m.getPeople().add(second);
        }).then((test) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);
        }).then((test) -> {
            var m = test.m;

            assertEquals(1, m.getCallbackCount(), "One callback " + m.getCallbackCount());

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt , 1, "Again one child, but was " + cnt);

            String txt = childText("ul", 0);
            assertEquals("first", txt, "Expecting 'first': " + txt);

            test.first.setFirstName("changed");

            txt = childText("ul", 0);
            assertEquals("changed", txt, "Expecting 'changed': " + txt);
        }).finalize((test) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @ComputedProperty
    static Person firstPerson(List<Person> people) {
        return people.isEmpty() ? null : people.get(0);
    }

    @KOTest public void accessFirstPersonWithOnFunction() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <p id='ul' data-bind='with: firstPerson'>
              <span data-bind='text: firstName, click: changeSex'></span>
            </p>
            """
        );
        try {
            trasfertToFemale();
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void onPersonFunction() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <ul id='ul' data-bind='foreach: people'>
              <li data-bind='text: $data.firstName, click: changeSex'></li>
            </ul>
            """
        );
        try {
            trasfertToFemale();
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    private void trasfertToFemale() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());

            final Person first = Models.bind(new Person(), newContext());
            first.setFirstName("first");
            first.setSex(Sex.MALE);
            m.getPeople().add(first);

            m.applyBindings();
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);
            triggerChildClick("ul", 0);
            return first;
        }).then((first) -> {
            assertEquals(first.getSex(), Sex.FEMALE, "Transverted to female: " + first.getSex());
        }).start();
    }

    @KOTest public void stringArrayModificationVisible() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <div>
            <ul id='ul' data-bind='foreach: results'>
              <li data-bind='text: $data'></li>
            </ul>
            </div>
            """);
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.getResults().add("Hello");
            m.applyBindings();
            return m;
        }).then((data) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "results", "Hi");
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 3, "Three elements in the array " + len);
        }).then((m) -> {
            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 3, "Three children in the DOM: " + newCnt);

            assertEquals(m.getResults().size(), 3, "Three java strings: " + m.getResults());
        }).finalize((data) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest public void intArrayModificationVisible() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <div>
            <ul id='ul' data-bind='foreach: numbers'>
              <li data-bind='text: $data'></li>
            </ul>
            </div>
            """);
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getNumbers().add(1);
            m.getNumbers().add(31);
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "numbers", 42);
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 3, "Three elements in the array " + len);
        }).then((m) -> {
            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 3, "Three children in the DOM: " + newCnt);

            assertEquals(m.getNumbers().size(), 3, "Three java ints: " + m.getNumbers());
            assertEquals(m.getNumbers().get(2), 42, "Meaning of world: " + m.getNumbers());
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest public void derivedIntArrayModificationVisible() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <div>
            <ul id='ul' data-bind='foreach: resultLengths'>
              <li data-bind='text: $data'></li>
            </ul>
            </div>
            """);
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.getResults().add("Hello");
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "results", "Hi");
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 3, "Three elements in the array " + len);
        }).then((m) -> {
            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 3, "Three children in the DOM: " + newCnt);

            assertEquals(m.getResultLengths().size(), 3, "Three java ints: " + m.getResultLengths());
            assertEquals(m.getResultLengths().get(2), 2, "Size is two: " + m.getResultLengths());
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @KOTest public void archetypeArrayModificationVisible() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            Object exp = Utils.exposeHTML(KnockoutTest.class, """
            <div>
            <ul id='ul' data-bind='foreach: archetypes'>
              <li data-bind='text: artifactId'></li>
            </ul>
            </div>
            """);
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 0, "No children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "archetypes", new ArchetypeData("aid", "gid", "v", "n", "d", "u"));
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 1, "One element in the array " + len);
        }).then((m) -> {
            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 1, "One child in the DOM: " + newCnt);

            assertEquals(m.getArchetypes().size(), 1, "One archetype: " + m.getArchetypes());
            assertNotNull(m.getArchetypes().get(0), "Not null: " + m.getArchetypes());
            assertEquals(m.getArchetypes().get(0).getArtifactId(), "aid", "'aid' == " + m.getArchetypes());
        }).finalize((m) -> {
            Utils.exposeHTML(KnockoutTest.class, "");
        }).start();
    }

    @Function
    static void call(KnockoutModel m, String data) {
        m.setName(data);
        m.setCallbackCount(m.getCallbackCount() + 1);
    }

    @Function
    static void removePerson(KnockoutModel model, Person data) {
        model.setCallbackCount(model.getCallbackCount() + 1);
        model.getPeople().remove(data);
    }


    @ComputedProperty
    static String helloMessage(String name) {
        return "Hello " + name + "!";
    }

    @ComputedProperty
    static List<String> cmpResults(List<String> results) {
        return results;
    }

    private static void triggerClick(String id) throws Exception {
        String s = """
                   var id = arguments[0];var e = window.document.getElementById(id);
                    if (e.checked) throw 'It should not be checked yet: ' + e;
                    var ev = window.document.createEvent('MouseEvents');
                    ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
                    e.dispatchEvent(ev);
                    if (!e.checked) {
                     e.checked = true;
                      e.dispatchEvent(ev);
                    }
                   """;
        Utils.executeScript(
            KnockoutTest.class,
            s, id);
    }
    private static void triggerChildClick(String id, int pos) throws Exception {
        String s =
            """
            var id = arguments[0]; var pos = arguments[1];
            var e = window.document.getElementById(id);
             var ev = window.document.createEvent('MouseEvents');
             ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
             var list = e.childNodes;
            var cnt = -1;
            for (var i = 0; i < list.length; i++) {
              if (list[i].nodeType == 1) cnt++;
              if (cnt == pos) return list[i].dispatchEvent(ev);
            }
            return null;
            """;
        Utils.executeScript(
            KnockoutTest.class,
            s, id, pos);
    }

    private static String childText(String id, int pos) throws Exception {
        String s =
            """
            var id = arguments[0]; var pos = arguments[1];var e = window.document.getElementById(id);
            var list = e.childNodes;
            var cnt = -1;
            for (var i = 0; i < list.length; i++) {
              if (list[i].nodeType == 1) cnt++;
              if (cnt == pos) return list[i].innerHTML;
            }
            return null;
            """;
        return (String)Utils.executeScript(
            KnockoutTest.class,
            s, id, pos);
    }

    private static BrwsrCtx newContext() {
        return Utils.newContext(KnockoutTest.class);
    }
}
