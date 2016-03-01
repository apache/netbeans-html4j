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

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
    private KnockoutModel js;

    enum Choice {
        A, B;
    }

    @ComputedProperty static List<Integer> resultLengths(List<String> results) {
        Integer[] arr = new Integer[results.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = results.get(i).length();
        }
        return Arrays.asList(arr);
    }

    @KOTest public void modifyValueAssertChangeInModelOnEnum() throws Throwable {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "Latitude: <input id='input' data-bind=\"value: choice\"></input>\n"
        );
        try {

            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setChoice(Choice.A);
            m.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("A", v, "Value is really A: " + v);

            getSetInput("input", "B");
            triggerEvent("input", "change");

            assertEquals(Choice.B, m.getChoice(), "Enum property updated: " + m.getChoice());
        } catch (Throwable t) {
            throw t;
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }


    @KOTest public void modifyRadioValueOnEnum() throws Throwable {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<input id='i1' type=\"radio\" name=\"choice\" value=\"A\" data-bind=\"checked: choice\"></input>Right\n" +
            "<input id='input' type=\"radio\" name=\"choice\" value=\"B\" data-bind=\"checked: choice\"></input>Never\n" +
            "\n"
        );
        try {

            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setChoice(Choice.B);
            m.applyBindings();

            assertFalse(isChecked("i1"), "B should be checked now");
            assertTrue(isChecked("input"), "B should be checked now");

            triggerEvent("i1", "click");
            assertEquals(Choice.A, m.getChoice(), "Switched to A");
            assertTrue(isChecked("i1"), "A should be checked now");
            assertFalse(isChecked("input"), "A should be checked now");

            triggerEvent("input", "click");

            assertEquals(Choice.B, m.getChoice(), "Enum property updated: " + m.getChoice());
        } catch (Throwable t) {
            throw t;
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void modifyValueAssertChangeInModelOnDouble() throws Throwable {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "Latitude: <input id='input' data-bind=\"value: latitude\"></input>\n"
        );
        try {

            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setLatitude(50.5);
            m.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("50.5", v, "Value is really 50.5: " + v);

            getSetInput("input", "49.5");
            triggerEvent("input", "change");

            assertEquals(49.5, m.getLatitude(), "Double property updated: " + m.getLatitude());
        } catch (Throwable t) {
            throw t;
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void rawObject() throws Exception {
        if (js == null) {
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

            js = new KnockoutModel();
            js.getPeople().add(p1);
            js.getPeople().add(p2);
        }

        Person p1 = js.getPeople().get(0);
        Person p2 = js.getPeople().get(1);

        if (js.getPeople().size() == 2) {
            if (!"Jirka".equals(p1.getFirstName())) {
                throw new InterruptedException();
            }

            assertEquals(p1.getFirstName(), "Jirka", "First name updated in original object");

            p1.setFirstName("Ondra");
            assertEquals(p1.getFirstName(), "Ondra", "1st name updated in original object");

            js.getPeople().add(p1);
        }

        if (!"Ondra".equals(p2.getFirstName())) {
            throw new InterruptedException();
        }
        assertEquals(p2.getFirstName(), "Ondra", "1st name updated in copied object");
    }

    @KOTest public void modifyComputedProperty() throws Throwable {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "Full name: <div data-bind='with:firstPerson'>\n"
                + "<input id='input' data-bind=\"value: fullName\"></input>\n"
                + "</div>\n"
        );
        try {
            KnockoutModel m = new KnockoutModel();
            m.getPeople().add(new Person());

            m = Models.bind(m, newContext());
            m.getFirstPerson().setFirstName("Jarda");
            m.getFirstPerson().setLastName("Tulach");
            m.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("Jarda Tulach", v, "Value: " + v);

            getSetInput("input", "Mickey Mouse");
            triggerEvent("input", "change");

            assertEquals("Mickey", m.getFirstPerson().getFirstName(), "First name updated");
            assertEquals("Mouse", m.getFirstPerson().getLastName(), "Last name updated");
        } catch (Throwable t) {
            throw t;
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void modifyValueAssertChangeInModelOnBoolean() throws Throwable {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "Latitude: <input id='input' data-bind=\"value: enabled\"></input>\n"
        );
        try {

            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setEnabled(true);
            m.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("true", v, "Value is really true: " + v);

            getSetInput("input", "false");
            triggerEvent("input", "change");

            assertFalse(m.isEnabled(), "Boolean property updated: " + m.isEnabled());
        } catch (Throwable t) {
            throw t;
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void modifyValueAssertChangeInModel() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<h1 data-bind=\"text: helloMessage\">Loading Bck2Brwsr's Hello World...</h1>\n" +
            "Your name: <input id='input' data-bind=\"value: name\"></input>\n" +
            "<button id=\"hello\">Say Hello!</button>\n"
        );
        try {

            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setName("Kukuc");
            m.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("Kukuc", v, "Value is really kukuc: " + v);

            getSetInput("input", "Jardo");
            triggerEvent("input", "change");

            assertEquals("Jardo", m.getName(), "Name property updated: " + m.getName());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    private static String getSetSelected(int index, Object value) throws Exception {
        String s = "var index = arguments[0];\n"
        + "var value = arguments[1];\n"
        + "var n = window.document.getElementById('input'); \n "
        + "if (value != null) {\n"
        + "  n.options[index].value = 'me'; \n"
        + "  n.value = 'me'; \n"
        + "  ko.dataFor(n.options[index]).archetype(value); // haven't found better way to trigger ko change yet \n"
        + "} \n "
        + "var op = n.options[n.selectedIndex]; \n"
        + "return op ? op.text : n.selectedIndex;\n";
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
        if (js == null) {
            Utils.exposeHTML(KnockoutTest.class,
                "<select id='input' data-bind=\"options: archetypes,\n" +
"                       optionsText: 'name',\n" +
"                       value: archetype\">\n" +
"                  </select>\n" +
""
            );

            js = Models.bind(new KnockoutModel(), newContext());
            js.getArchetypes().add(new ArchetypeData("ko4j", "org.netbeans.html", "0.8.3", "ko4j", "ko4j", null));
            js.getArchetypes().add(new ArchetypeData("crud", "org.netbeans.html", "0.8.3", "crud", "crud", null));
            js.getArchetypes().add(new ArchetypeData("3rd", "org.netbeans.html", "0.8.3", "3rd", "3rd", null));
            js.setArchetype(js.getArchetypes().get(1));
            js.applyBindings();

            String v = getSetSelected(0, null);
            assertEquals("crud", v, "Second index (e.g. crud) is selected: " + v);

            String sel = getSetSelected(2, Models.toRaw(js.getArchetypes().get(2)));
            assertEquals("3rd", sel, "3rd is selected now: " + sel);
        }

        if (js.getArchetype() != js.getArchetypes().get(2)) {
            throw new InterruptedException();
        }

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void nestedObjectEqualsChange() throws Exception {
        nestedObjectEqualsChange(true);
    }

    @KOTest public void nestedObjectChange() throws Exception {
        nestedObjectEqualsChange(false);
    }
    private  void nestedObjectEqualsChange(boolean preApply) throws Exception {
        Utils.exposeHTML(KnockoutTest.class,
"            <div data-bind='with: archetype'>\n" +
"                <input id='input' data-bind='value: groupId'></input>\n" +
"            </div>\n"
        );

        js = Models.bind(new KnockoutModel(), newContext());
        if (preApply) {
            js.applyBindings();
        }
        js.setArchetype(new ArchetypeData());
        js.getArchetype().setGroupId("org.netbeans.html");
        js.applyBindings();

        String v = getSetInput("input", null);
        assertEquals("org.netbeans.html", v, "groupId has been changed");
        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void modifyValueAssertAsyncChangeInModel() throws Exception {
        if (js == null) {
            Utils.exposeHTML(KnockoutTest.class,
                "<h1 data-bind=\"text: helloMessage\">Loading Bck2Brwsr's Hello World...</h1>\n" +
                "Your name: <input id='input' data-bind=\"value: name\"></input>\n" +
                "<button id=\"hello\">Say Hello!</button>\n"
            );

            js = Models.bind(new KnockoutModel(), newContext());
            js.setName("Kukuc");
            js.applyBindings();

            String v = getSetInput("input", null);
            assertEquals("Kukuc", v, "Value is really kukuc: " + v);

            Timer t = new Timer("Set to Jardo");
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    js.setName("Jardo");
                }
            }, 1);
        }

        String v = getSetInput("input", null);
        if (!"Jardo".equals(v)) {
            throw new InterruptedException();
        }

        Utils.exposeHTML(KnockoutTest.class, "");
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
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: typeof doubleValue\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignDoubleValue(10.0);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "number", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void nonMutableString() throws Exception {
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: typeof stringValue\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignDoubleValue(10.0);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "string", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void nonMutableBoolean() throws Exception {
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: typeof boolValue\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignBoolValue(true);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "boolean", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    @KOTest public void nonMutableIntArray() throws Exception {
        Utils.exposeHTML(KnockoutTest.class,
            "Type: <input id='input' data-bind=\"value: typeof intArray\"></input>\n"
        );

        ConstantModel model = Models.bind(new ConstantModel(), newContext());
        model.assignStringValue("Hello").assignDoubleValue(Long.MAX_VALUE).assignIntArray(1, 2, 3, 4);
        model.applyBindings();

        String v = getSetInput("input", null);
        assertEquals(v, "object", "Right type found: " + v);

        Utils.exposeHTML(KnockoutTest.class, "");
    }

    private static String getSetInput(String id, String value) throws Exception {
        String s = "var value = arguments[0];\n"
        + "var n = window.document.getElementById(arguments[1]); \n "
        + "if (value != null) n['value'] = value; \n "
        + "return n['value'];";
        Object ret = Utils.executeScript(
            KnockoutTest.class,
            s, value, id
        );
        return ret == null ? null : ret.toString();
    }

    private static boolean isChecked(String id) throws Exception {
        String s = ""
        + "var n = window.document.getElementById(arguments[0]); \n "
        + "return n['checked'];";
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

    @KOTest public void displayContentOfArray() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<ul id='ul' data-bind='foreach: results'>\n"
            + "  <li data-bind='text: $data, click: $root.call'/>\n"
            + "</ul>\n"
        );
        try {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            m.getResults().add("Hi");

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);

            assertEquals(1, m.getCallbackCount(), "One callback " + m.getCallbackCount());
            assertEquals("Hi", m.getName(), "We got callback from 2nd child " + m.getName());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void displayContentOfAsyncArray() throws Exception {
        if (js == null) {
            Utils.exposeHTML(KnockoutTest.class,
                "<ul id='ul' data-bind='foreach: results'>\n"
                + "  <li data-bind='text: $data, click: $root.call'/>\n"
                + "</ul>\n"
            );
            js = Models.bind(new KnockoutModel(), newContext());
            js.getResults().add("Ahoj");
            js.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            Timer t = new Timer("add to array");
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    js.getResults().add("Hi");
                }
            }, 1);
        }


        int cnt = Utils.countChildren(KnockoutTest.class, "ul");
        if (cnt != 2) {
            throw new InterruptedException();
        }

        try {
            triggerChildClick("ul", 1);

            assertEquals(1, js.getCallbackCount(), "One callback " + js.getCallbackCount());
            assertEquals("Hi", js.getName(), "We got callback from 2nd child " + js.getName());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void displayContentOfComputedArray() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<ul id='ul' data-bind='foreach: bothNames'>\n"
            + "  <li data-bind='text: $data, click: $root.assignFirstName'/>\n"
            + "</ul>\n"
        );
        try {
            Pair m = Models.bind(new Pair("First", "Last", null), newContext());
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);

            assertEquals("Last", m.getFirstName(), "We got callback from 2nd child " + m.getFirstName());

            m.setLastName("Verylast");

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);

            assertEquals("Verylast", m.getFirstName(), "We got callback from 2nd child " + m.getFirstName());

        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void displayContentOfComputedArrayOnASubpair() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
              "<div data-bind='with: next'>\n"
            + "<ul id='ul' data-bind='foreach: bothNames'>\n"
            + "  <li data-bind='text: $data, click: $root.assignFirstName'/>\n"
            + "</ul>"
            + "</div>\n"
        );
        try {
            final BrwsrCtx ctx = newContext();
            Pair m = Models.bind(new Pair(null, null, new Pair("First", "Last", null)), ctx);
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);

            assertEquals(PairModel.ctx, ctx, "Context remains the same");

            assertEquals("Last", m.getFirstName(), "We got callback from 2nd child " + m.getFirstName());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void displayContentOfComputedArrayOnComputedASubpair() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
              "<div data-bind='with: nextOne'>\n"
            + "<ul id='ul' data-bind='foreach: bothNames'>\n"
            + "  <li data-bind='text: $data, click: $root.assignFirstName'/>\n"
            + "</ul>"
            + "</div>\n"
        );
        try {
            Pair m = Models.bind(new Pair(null, null, new Pair("First", "Last", null)), newContext());
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);

            assertEquals("Last", m.getFirstName(), "We got callback from 2nd child " + m.getFirstName());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void checkBoxToBooleanBinding() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<input type='checkbox' id='b' data-bind='checked: enabled'></input>\n"
        );
        try {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.applyBindings();

            assertFalse(m.isEnabled(), "Is disabled");

            triggerClick("b");

            assertTrue(m.isEnabled(), "Now the model is enabled");
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }



    @KOTest public void displayContentOfDerivedArray() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<ul id='ul' data-bind='foreach: cmpResults'>\n"
            + "  <li><b data-bind='text: $data'></b></li>\n"
            + "</ul>\n"
        );
        try {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            m.getResults().add("hello");

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void displayContentOfArrayOfPeople() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<ul id='ul' data-bind='foreach: people'>\n"
            + "  <li data-bind='text: $data.firstName, click: $root.removePerson'></li>\n"
            + "</ul>\n"
        );
        try {
            final BrwsrCtx c = newContext();
            KnockoutModel m = Models.bind(new KnockoutModel(), c);

            final Person first = Models.bind(new Person(), c);
            first.setFirstName("first");
            m.getPeople().add(first);

            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            final Person second = Models.bind(new Person(), c);
            second.setFirstName("second");
            m.getPeople().add(second);

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children now, but was " + cnt);

            triggerChildClick("ul", 1);

            assertEquals(1, m.getCallbackCount(), "One callback " + m.getCallbackCount());

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt , 1, "Again one child, but was " + cnt);

            String txt = childText("ul", 0);
            assertEquals("first", txt, "Expecting 'first': " + txt);

            first.setFirstName("changed");

            txt = childText("ul", 0);
            assertEquals("changed", txt, "Expecting 'changed': " + txt);
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @ComputedProperty
    static Person firstPerson(List<Person> people) {
        return people.isEmpty() ? null : people.get(0);
    }

    @KOTest public void accessFirstPersonWithOnFunction() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<p id='ul' data-bind='with: firstPerson'>\n"
            + "  <span data-bind='text: firstName, click: changeSex'></span>\n"
            + "</p>\n"
        );
        try {
            trasfertToFemale();
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void onPersonFunction() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
            "<ul id='ul' data-bind='foreach: people'>\n"
            + "  <li data-bind='text: $data.firstName, click: changeSex'></li>\n"
            + "</ul>\n"
        );
        try {
            trasfertToFemale();
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    private void trasfertToFemale() throws Exception {
        KnockoutModel m = Models.bind(new KnockoutModel(), newContext());

        final Person first = Models.bind(new Person(), newContext());
        first.setFirstName("first");
        first.setSex(Sex.MALE);
        m.getPeople().add(first);


        m.applyBindings();

        int cnt = Utils.countChildren(KnockoutTest.class, "ul");
        assertEquals(cnt, 1, "One child, but was " + cnt);


        triggerChildClick("ul", 0);

        assertEquals(first.getSex(), Sex.FEMALE, "Transverted to female: " + first.getSex());
    }

    @KOTest public void stringArrayModificationVisible() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
                "<div>\n"
                + "<ul id='ul' data-bind='foreach: results'>\n"
                + "  <li data-bind='text: $data'></li>\n"
                + "</ul>\n"
              + "</div>\n"
        );
        try {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.getResults().add("Hello");
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "results", "Hi");
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 3, "Three elements in the array " + len);

            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 3, "Three children in the DOM: " + newCnt);

            assertEquals(m.getResults().size(), 3, "Three java strings: " + m.getResults());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void intArrayModificationVisible() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
                "<div>\n"
                + "<ul id='ul' data-bind='foreach: numbers'>\n"
                + "  <li data-bind='text: $data'></li>\n"
                + "</ul>\n"
              + "</div>\n"
        );
        try {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getNumbers().add(1);
            m.getNumbers().add(31);
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "numbers", 42);
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 3, "Three elements in the array " + len);

            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 3, "Three children in the DOM: " + newCnt);

            assertEquals(m.getNumbers().size(), 3, "Three java ints: " + m.getNumbers());
            assertEquals(m.getNumbers().get(2), 42, "Meaning of world: " + m.getNumbers());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void derivedIntArrayModificationVisible() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
                "<div>\n"
                + "<ul id='ul' data-bind='foreach: resultLengths'>\n"
                + "  <li data-bind='text: $data'></li>\n"
                + "</ul>\n"
              + "</div>\n"
        );
        try {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.getResults().add("Ahoj");
            m.getResults().add("Hello");
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Two children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "results", "Hi");
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 3, "Three elements in the array " + len);

            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 3, "Three children in the DOM: " + newCnt);

            assertEquals(m.getResultLengths().size(), 3, "Three java ints: " + m.getResultLengths());
            assertEquals(m.getResultLengths().get(2), 2, "Size is two: " + m.getResultLengths());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }

    @KOTest public void archetypeArrayModificationVisible() throws Exception {
        Object exp = Utils.exposeHTML(KnockoutTest.class,
                "<div>\n"
                + "<ul id='ul' data-bind='foreach: archetypes'>\n"
                + "  <li data-bind='text: artifactId'></li>\n"
                + "</ul>\n"
              + "</div>\n"
        );
        try {
            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.applyBindings();

            int cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(cnt, 0, "No children " + cnt);

            Object arr = Utils.addChildren(KnockoutTest.class, "ul", "archetypes", new ArchetypeData("aid", "gid", "v", "n", "d", "u"));
            assertTrue(arr instanceof Object[], "Got back an array: " + arr);
            final int len = ((Object[])arr).length;

            assertEquals(len, 1, "One element in the array " + len);

            int newCnt = Utils.countChildren(KnockoutTest.class, "ul");
            assertEquals(newCnt, 1, "One child in the DOM: " + newCnt);

            assertEquals(m.getArchetypes().size(), 1, "One archetype: " + m.getArchetypes());
            assertNotNull(m.getArchetypes().get(0), "Not null: " + m.getArchetypes());
            assertEquals(m.getArchetypes().get(0).getArtifactId(), "aid", "'aid' == " + m.getArchetypes());
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
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
        String s = "var id = arguments[0];"
            + "var e = window.document.getElementById(id);\n "
            + "if (e.checked) throw 'It should not be checked yet: ' + e;\n "
            + "var ev = window.document.createEvent('MouseEvents');\n "
            + "ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n "
            + "e.dispatchEvent(ev);\n "
            + "if (!e.checked) {\n"
            + "  e.checked = true;\n "
            + "  e.dispatchEvent(ev);\n "
            + "}\n";
        Utils.executeScript(
            KnockoutTest.class,
            s, id);
    }
    private static void triggerChildClick(String id, int pos) throws Exception {
        String s =
            "var id = arguments[0]; var pos = arguments[1];\n" +
            "var e = window.document.getElementById(id);\n " +
            "var ev = window.document.createEvent('MouseEvents');\n " +
            "ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n " +
            "var list = e.childNodes;\n" +
            "var cnt = -1;\n" +
            "for (var i = 0; i < list.length; i++) {\n" +
            "  if (list[i].nodeType == 1) cnt++;\n" +
            "  if (cnt == pos) return list[i].dispatchEvent(ev);\n" +
            "}\n" +
            "return null;\n";
        Utils.executeScript(
            KnockoutTest.class,
            s, id, pos);
    }

    private static String childText(String id, int pos) throws Exception {
        String s =
            "var id = arguments[0]; var pos = arguments[1];" +
            "var e = window.document.getElementById(id);\n" +
            "var list = e.childNodes;\n" +
            "var cnt = -1;\n" +
            "for (var i = 0; i < list.length; i++) {\n" +
            "  if (list[i].nodeType == 1) cnt++;\n" +
            "  if (cnt == pos) return list[i].innerHTML;\n" +
            "}\n" +
            "return null;\n";
        return (String)Utils.executeScript(
            KnockoutTest.class,
            s, id, pos);
    }

    private static BrwsrCtx newContext() {
        return Utils.newContext(KnockoutTest.class);
    }
}
