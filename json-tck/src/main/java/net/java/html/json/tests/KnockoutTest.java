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

import java.util.List;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.HtmlFragment;
import org.apidesign.bck2brwsr.vmtest.VMTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className="KnockoutModel", properties={
    @Property(name="name", type=String.class),
    @Property(name="results", type=String.class, array = true),
    @Property(name="callbackCount", type=int.class),
    @Property(name="people", type=PersonImpl.class, array = true),
    @Property(name="enabled", type=boolean.class)
}) 
public final class KnockoutTest {
    
    @HtmlFragment(
        "<h1 data-bind=\"text: helloMessage\">Loading Bck2Brwsr's Hello World...</h1>\n" +
        "Your name: <input id='input' data-bind=\"value: name\"></input>\n" +
        "<button id=\"hello\">Say Hello!</button>\n"
    )
    @BrwsrTest public void modifyValueAssertChangeInModel() throws Exception {
        KnockoutModel m = Models.bind(new KnockoutModel(), Utils.newContext());
        m.setName("Kukuc");
        m.applyBindings();
        
        String v = getSetInput(null);
        assert "Kukuc".equals(v) : "Value is really kukuc: " + v;
        
        getSetInput("Jardo");
        triggerEvent("input", "change");
        
        assert "Jardo".equals(m.getName()) : "Name property updated: " + m.getName();
    }
    
    private static String getSetInput(String value) throws Exception {
        String s = "var value = arguments[0];\n"
        + "var n = window.document.getElementById('input'); \n "
        + "if (value != null) n['value'] = value; \n "
        + "return n['value'];";
        return (String)Utils.executeScript(s, value);
    }
    
    public static void triggerEvent(String id, String ev) throws Exception {
        Utils.executeScript(
            "ko.utils.triggerEvent(window.document.getElementById(arguments[0]), arguments[1]);",
            id, ev
        );
    }
    
    @HtmlFragment(
        "<ul id='ul' data-bind='foreach: results'>\n"
        + "  <li data-bind='text: $data, click: $root.call'/>\n"
        + "</ul>\n"
    )
    @BrwsrTest public void displayContentOfArray() throws Exception {
        KnockoutModel m = Models.bind(new KnockoutModel(), Utils.newContext());
        m.getResults().add("Ahoj");
        m.applyBindings();
        
        int cnt = countChildren("ul");
        assert cnt == 1 : "One child, but was " + cnt;
        
        m.getResults().add("Hi");

        cnt = countChildren("ul");
        assert cnt == 2 : "Two children now, but was " + cnt;
        
        triggerChildClick("ul", 1);
        
        assert 1 == m.getCallbackCount() : "One callback " + m.getCallbackCount();
        assert "Hi".equals(m.getName()) : "We got callback from 2nd child " + m.getName();
    }

    @HtmlFragment(
        "<input type='checkbox' id='b' data-bind='checked: enabled'></input>\n"
    )
    @BrwsrTest public void checkBoxToBooleanBinding() throws Exception {
        KnockoutModel m = Models.bind(new KnockoutModel(), Utils.newContext());
        m.applyBindings();
        
        assert !m.isEnabled() : "Is disabled";

        triggerClick("b");
        
        assert m.isEnabled() : "Now the model is enabled";
    }
    
    
    
    @HtmlFragment(
        "<ul id='ul' data-bind='foreach: cmpResults'>\n"
        + "  <li><b data-bind='text: $data'></b></li>\n"
        + "</ul>\n"
    )
    @BrwsrTest public void displayContentOfDerivedArray() throws Exception {
        KnockoutModel m = Models.bind(new KnockoutModel(), Utils.newContext());
        m.getResults().add("Ahoj");
        m.applyBindings();
        
        int cnt = countChildren("ul");
        assert cnt == 1 : "One child, but was " + cnt;
        
        m.getResults().add("hello");

        cnt = countChildren("ul");
        assert cnt == 2 : "Two children now, but was " + cnt;
    }
    
    @HtmlFragment(
        "<ul id='ul' data-bind='foreach: people'>\n"
        + "  <li data-bind='text: $data.firstName, click: $root.removePerson'></li>\n"
        + "</ul>\n"
    )
    @BrwsrTest public void displayContentOfArrayOfPeople() throws Exception {
        KnockoutModel m = Models.bind(new KnockoutModel(), Utils.newContext());
        
        final Person first = Models.bind(new Person(), Utils.newContext());
        first.setFirstName("first");
        m.getPeople().add(first);
        
        m.applyBindings();
        
        int cnt = countChildren("ul");
        assert cnt == 1 : "One child, but was " + cnt;
        
        final Person second = Models.bind(new Person(), Utils.newContext());
        second.setFirstName("second");
        m.getPeople().add(second);

        cnt = countChildren("ul");
        assert cnt == 2 : "Two children now, but was " + cnt;

        triggerChildClick("ul", 1);
        
        assert 1 == m.getCallbackCount() : "One callback " + m.getCallbackCount();

        cnt = countChildren("ul");
        assert cnt == 1 : "Again one child, but was " + cnt;
        
        String txt = childText("ul", 0);
        assert "first".equals(txt) : "Expecting 'first': " + txt;
        
        first.setFirstName("changed");
        
        txt = childText("ul", 0);
        assert "changed".equals(txt) : "Expecting 'changed': " + txt;
    }
    
    @ComputedProperty
    static Person firstPerson(List<Person> people) {
        return people.isEmpty() ? null : people.get(0);
    }
    
    @HtmlFragment(
        "<p id='ul' data-bind='with: firstPerson'>\n"
        + "  <span data-bind='text: firstName, click: changeSex'></span>\n"
        + "</p>\n"
    )
    @BrwsrTest public void accessFirstPersonWithOnFunction() throws Exception {
        trasfertToFemale();
    }
    
    @HtmlFragment(
        "<ul id='ul' data-bind='foreach: people'>\n"
        + "  <li data-bind='text: $data.firstName, click: changeSex'></li>\n"
        + "</ul>\n"
    )
    @BrwsrTest public void onPersonFunction() throws Exception {
        trasfertToFemale();
    }
    
    private void trasfertToFemale() throws Exception {
        KnockoutModel m = Models.bind(new KnockoutModel(), Utils.newContext());

        final Person first = Models.bind(new Person(), Utils.newContext());
        first.setFirstName("first");
        first.setSex(Sex.MALE);
        m.getPeople().add(first);


        m.applyBindings();

        int cnt = countChildren("ul");
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
    
    static Object[] create() {
        return VMTest.create(KnockoutTest.class);
    }
    
    private static int countChildren(String id) throws Exception {
        return ((Number)Utils.executeScript(
          "var e = window.document.getElementById(arguments[0]);\n "
        + "if (typeof e === 'undefined') return -2;\n "
        + "return e.children.length;", 
            id
        )).intValue();
    }

    private static void triggerClick(String id) throws Exception {
        String s = "var id = arguments[0];"
            + "var e = window.document.getElementById(id);\n "
            + "var ev = window.document.createEvent('MouseEvents');\n "
            + "ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n "
            + "e.dispatchEvent(ev);\n ";
        Utils.executeScript(s, id);
    }
    private static void triggerChildClick(String id, int pos) throws Exception {
        String s = "var id = arguments[0]; var pos = arguments[1];"
            + "var e = window.document.getElementById(id);\n "
            + "var ev = window.document.createEvent('MouseEvents');\n "
            + "ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n "
            + "e.children[pos].dispatchEvent(ev);\n ";
        Utils.executeScript(s, id, pos);
    }

    private static String childText(String id, int pos) throws Exception {
        String s = "var id = arguments[0]; var pos = arguments[1];"
        + "var e = window.document.getElementById(id);\n "
        + "var t = e.children[pos].innerHTML;\n "
        + "return t ? t : null;";
        return (String)Utils.executeScript(s, id, pos);
    }
}
