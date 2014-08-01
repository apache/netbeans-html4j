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
package net.java.html.json;

import java.util.List;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.MapModelTest.MapTechnology;
import net.java.html.json.MapModelTest.One;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DeepChangeTest {
    private MapTechnology t;
    private BrwsrCtx c;

    @BeforeMethod public void initTechnology() {
        t = new MapTechnology();
        c = Contexts.newBuilder().register(Technology.class, t, 1).
            register(Transfer.class, t, 1).build();
    }
    
    @Model(className = "MyX", properties = {
        @Property(name = "one", type = MyY.class),
        @Property(name = "all", type = MyY.class, array = true)
    })
    static class X {
        @ComputedProperty(deep = true) 
        static String oneName(MyY one) {
            return one.getValue();
        }
        @ComputedProperty(deep = true) 
        static String sndName(MyY one) {
            return one.getValue().toUpperCase();
        }
        @ComputedProperty(deep = false) 
        static String noName(MyY one) {
            return one.getValue().toUpperCase();
        }
        @ComputedProperty(deep = true) 
        static String thrdName(MyY one) {
            return "X" + one.getCount();
        }
        
        @ComputedProperty(deep = true)
        static String allNames(List<MyY> all) {
            StringBuilder sb = new StringBuilder();
            for (MyY y : all) {
                sb.append(y.getValue());
            }
            return sb.toString();
        }

        @ComputedProperty(deep = true)
        static String firstFromNames(List<MyY> all) {
            if (all.size() > 0) {
                return all.get(0).getValue();
            }
            return null;
        }
    }
    @Model(className = "MyY", properties = {
        @Property(name = "value", type = String.class),
        @Property(name = "count", type = int.class)
    })
    static class Y {
    }
    @Model(className = "MyOverall", properties = {
        @Property(name = "x", type = MyX.class)
    })
    static class Overall {
        @ComputedProperty(deep = true) 
        static String valueAccross(MyX x) {
            return x.getFirstFromNames();
        }
    }
    
    @Test public void isTransitiveChangeNotifiedProperly() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("oneName");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Ahoj");

        p.getOne().setValue("Nazdar");
        
        assertEquals(o.get(), "Nazdar");
        assertEquals(o.changes, 1, "One change so far");
    }
    
    @Test public void isTransitiveChangeInArrayNotifiedProperly() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("allNames");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "HiHello");

        p.getAll().get(0).setValue("Nazdar");
        
        assertEquals(o.get(), "NazdarHello");
        assertEquals(o.changes, 1, "One change so far");
    }

    @Test public void addingIntoArray() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("allNames");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "HiHello");

        MyY y = new MyY("Cus", 1);
        p.getAll().add(y);
        
        assertEquals(o.changes, 1, "One change so far");
        assertEquals(o.get(), "HiHelloCus");
        
        y.setValue("Nazdar");
        
        assertEquals(o.changes, 2, "2nd change so far");
        assertEquals(o.get(), "HiHelloNazdar");
        
    }
    
    @Test public void firstChangeInArrayNotifiedProperly() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("firstFromNames");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Hi");

        p.getAll().get(0).setValue("Nazdar");
        
        assertEquals(o.get(), "Nazdar");
        assertEquals(o.changes, 1, "One change so far");
    }
    
    @Test public void firstChangeInArrayNotifiedTransitively() throws Exception {
        MyOverall p = Models.bind(
            new MyOverall(new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999))
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("valueAccross");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Hi");

        p.getX().getAll().get(0).setValue("Nazdar");
        
        assertEquals(o.get(), "Nazdar");
        assertEquals(o.changes, 1, "One change so far");
    }
    
    @Test public void secondChangeInArrayIgnored() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("firstFromNames");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Hi");

        p.getAll().get(1).setValue("Nazdar");
        
        assertEquals(o.get(), "Hi");
        assertEquals(o.changes, 0, "No change so far");
    }
    
    @Test public void changeInArraySizeNeedsToBeRecomputed() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("firstFromNames");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Hi");

        p.getAll().remove(1);
        
        assertEquals(o.get(), "Hi");
        assertEquals(o.changes, 1, "This required a change");
    }
    
    @Test public void doublePropertyChangeNotified() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("oneName");
        assertNotNull(v, "Value should be in the map");
        Object v2 = m.get("sndName");
        assertNotNull(v2, "Value2 should be in the map");
        One o = (One)v;
        One o2 = (One)v2;
        assertEquals(o.changes, 0, "No changes so far");
        assertEquals(o2.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Ahoj");
        assertEquals(o2.get(), "AHOJ");

        p.getOne().setValue("Nazdar");
        
        assertEquals(o.get(), "Nazdar");
        assertEquals(o.changes, 1, "One change so far");
        assertEquals(o2.changes, 1, "One change so far");
        assertEquals(o2.get(), "NAZDAR");
    }
    
    @Test public void onlyAffectedPropertyChangeNotified() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("oneName");
        assertNotNull(v, "Value should be in the map");
        Object v2 = m.get("thrdName");
        assertNotNull(v2, "Value2 should be in the map");
        One o = (One)v;
        One o2 = (One)v2;
        assertEquals(o.changes, 0, "No changes so far");
        assertEquals(o2.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Ahoj");
        assertEquals(o2.get(), "X0");

        p.getOne().setCount(10);
        
        assertEquals(o.get(), "Ahoj");
        assertEquals(o.changes, 0, "Still no change");
        assertEquals(o2.changes, 1, "One change so far");
        assertEquals(o2.get(), "X10");
    }
    
    @Test public void onlyDeepPropsAreNotified() throws Exception {
        MyX p = Models.bind(
            new MyX(new MyY("Ahoj", 0), new MyY("Hi", 333), new MyY("Hello", 999)
        ), c).applyBindings();
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("oneName");
        assertNotNull(v, "Value should be in the map");
        Object v2 = m.get("noName");
        assertNotNull(v2, "Value2 should be in the map");
        One o = (One)v;
        One o2 = (One)v2;
        assertEquals(o.changes, 0, "No changes so far");
        assertEquals(o2.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), "Ahoj");
        assertEquals(o2.get(), "AHOJ");

        p.getOne().setValue("Nazdar");
        
        assertEquals(o.get(), "Nazdar");
        assertEquals(o.changes, 1, "One change so far");
        assertEquals(o2.changes, 0, "This change is not noticed");
        assertEquals(o2.get(), "NAZDAR", "but property value changes when computed");
    }
    
}
