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

import java.util.List;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.apidesign.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className="KnockoutModel", properties={
    @Property(name="name", type=String.class),
    @Property(name="results", type=String.class, array = true),
    @Property(name="callbackCount", type=int.class),
    @Property(name="people", type=PersonImpl.class, array = true),
    @Property(name="enabled", type=boolean.class),
    @Property(name="latitude", type=double.class)
}) 
public final class KnockoutTest {
    
    @KOTest public void modifyValueAssertChangeInModelOnDouble() throws Throwable {
        Object exp = Utils.exposeHTML(KnockoutTest.class, 
            "Latitude: <input id='input' data-bind=\"value: latitude\"></input>\n"
        );
        try {

            KnockoutModel m = Models.bind(new KnockoutModel(), newContext());
            m.setLatitude(50.5);
            m.applyBindings();

            String v = getSetInput(null);
            assert "50.5".equals(v) : "Value is really 50.5: " + v;

            getSetInput("49.5");
            triggerEvent("input", "change");

            assert 49.5 == m.getLatitude() : "Double property updated: " + m.getLatitude();
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

            String v = getSetInput(null);
            assert "true".equals(v) : "Value is really true: " + v;

            getSetInput("false");
            triggerEvent("input", "change");

            assert false == m.isEnabled(): "Boolean property updated: " + m.isEnabled();
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

            String v = getSetInput(null);
            assert "Kukuc".equals(v) : "Value is really kukuc: " + v;

            getSetInput("Jardo");
            triggerEvent("input", "change");

            assert "Jardo".equals(m.getName()) : "Name property updated: " + m.getName();
        } finally {
            Utils.exposeHTML(KnockoutTest.class, "");
        }
    }
    
    private static String getSetInput(String value) throws Exception {
        String s = "var value = arguments[0];\n"
        + "var n = window.document.getElementById('input'); \n "
        + "if (value != null) n['value'] = value; \n "
        + "return n['value'];";
        return (String)Utils.executeScript(
            KnockoutTest.class,
            s, value
        );
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
            assert cnt == 1 : "One child, but was " + cnt;

            m.getResults().add("Hi");

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assert cnt == 2 : "Two children now, but was " + cnt;

            triggerChildClick("ul", 1);

            assert 1 == m.getCallbackCount() : "One callback " + m.getCallbackCount();
            assert "Hi".equals(m.getName()) : "We got callback from 2nd child " + m.getName();
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
            assert cnt == 2 : "Two children now, but was " + cnt;

            triggerChildClick("ul", 1);

            assert "Last".equals(m.getFirstName()) : "We got callback from 2nd child " + m.getFirstName();
            
            m.setLastName("Verylast");

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assert cnt == 2 : "Two children now, but was " + cnt;
            
            triggerChildClick("ul", 1);

            assert "Verylast".equals(m.getFirstName()) : "We got callback from 2nd child " + m.getFirstName();
            
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
            assert cnt == 2 : "Two children now, but was " + cnt;

            triggerChildClick("ul", 1);
            
            assert PairModel.ctx == ctx : "Context remains the same";

            assert "Last".equals(m.getFirstName()) : "We got callback from 2nd child " + m.getFirstName();
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
            assert cnt == 2 : "Two children now, but was " + cnt;

            triggerChildClick("ul", 1);

            assert "Last".equals(m.getFirstName()) : "We got callback from 2nd child " + m.getFirstName();
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

            assert !m.isEnabled() : "Is disabled";

            triggerClick("b");

            assert m.isEnabled() : "Now the model is enabled";
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
            assert cnt == 1 : "One child, but was " + cnt;

            m.getResults().add("hello");

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assert cnt == 2 : "Two children now, but was " + cnt;
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
            assert cnt == 1 : "One child, but was " + cnt;

            final Person second = Models.bind(new Person(), c);
            second.setFirstName("second");
            m.getPeople().add(second);

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assert cnt == 2 : "Two children now, but was " + cnt;

            triggerChildClick("ul", 1);

            assert 1 == m.getCallbackCount() : "One callback " + m.getCallbackCount();

            cnt = Utils.countChildren(KnockoutTest.class, "ul");
            assert cnt == 1 : "Again one child, but was " + cnt;

            String txt = childText("ul", 0);
            assert "first".equals(txt) : "Expecting 'first': " + txt;

            first.setFirstName("changed");

            txt = childText("ul", 0);
            assert "changed".equals(txt) : "Expecting 'changed': " + txt;
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
        assert cnt == 1 : "One child, but was " + cnt;


        triggerChildClick("ul", 0);

        assert first.getSex() == Sex.FEMALE : "Transverted to female: " + first.getSex();
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
            + "var ev = window.document.createEvent('MouseEvents');\n "
            + "ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n "
            + "e.dispatchEvent(ev);\n ";
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
            "  if (cnt === pos) return list[i].dispatchEvent(ev);\n" + 
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
            "  if (cnt === pos) return list[i].innerHTML;\n" + 
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
