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
package org.netbeans.html.json.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.PropertyBinding;
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
        @ComputedProperty @Transitive(deep = true) 
        static String oneName(MyY one) {
            return one.getValue();
        }
        @ComputedProperty
        static String sndName(MyY one) {
            return one.getValue().toUpperCase();
        }
        @ComputedProperty @Transitive(deep = false) 
        static String noName(MyY one) {
            return one.getValue().toUpperCase();
        }
        @ComputedProperty @Transitive(deep = true) 
        static String thrdName(MyY one) {
            return "X" + one.getCount();
        }
        
        @ComputedProperty
        static String allNames(List<MyY> all) {
            StringBuilder sb = new StringBuilder();
            for (MyY y : all) {
                sb.append(y.getValue());
            }
            return sb.toString();
        }

        @ComputedProperty @Transitive(deep = true)
        static String firstFromNames(List<MyY> all) {
            for (MyY y : all) {
                if (y != null && y.getValue() != null) {
                    return y.getValue();
                }
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
        @ComputedProperty @Transitive(deep = true) 
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
        
        y.setValue("Zdravim");

        assertEquals(o.changes, 3, "3rd change so far");
        assertEquals(o.get(), "HiHelloZdravim");
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
    
    @Test public void firstChangeInArrayToNull() throws Exception {
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

        p.getAll().get(0).setValue(null);
        
        assertEquals(o.get(), "Hello");
        assertEquals(o.changes, 1, "One change so far");
        
        p.getAll().get(0).setValue("Nazdar");

        assertEquals(o.get(), "Nazdar");
        assertEquals(o.changes, 2, "2nd change so far");
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

    static final class One {

        int changes;
        final PropertyBinding pb;
        final FunctionBinding fb;

        One(Object m, PropertyBinding pb) throws NoSuchMethodException {
            this.pb = pb;
            this.fb = null;
        }

        One(Object m, FunctionBinding fb) throws NoSuchMethodException {
            this.pb = null;
            this.fb = fb;
        }

        Object get() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return pb.getValue();
        }

        void set(Object v) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            pb.setValue(v);
        }
    }

    static final class MapTechnology
            implements Technology<Map<String, One>>, Transfer {

        @Override
        public Map<String, One> wrapModel(Object model) {
            return new HashMap<String, One>();
        }

        @Override
        public void valueHasMutated(Map<String, One> data, String propertyName) {
            One p = data.get(propertyName);
            if (p != null) {
                p.changes++;
            }
        }

        @Override
        public void bind(PropertyBinding b, Object model, Map<String, One> data) {
            try {
                One o = new One(model, b);
                data.put(b.getPropertyName(), o);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void expose(FunctionBinding fb, Object model, Map<String, One> data) {
            try {
                data.put(fb.getFunctionName(), new One(model, fb));
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void applyBindings(Map<String, One> data) {
        }

        @Override
        public Object wrapArray(Object[] arr) {
            return arr;
        }

        @Override
        public void extract(Object obj, String[] props, Object[] values) {
            Map<?, ?> map = obj instanceof Map ? (Map<?, ?>) obj : null;
            for (int i = 0; i < Math.min(props.length, values.length); i++) {
                if (map == null) {
                    values[i] = null;
                } else {
                    values[i] = map.get(props[i]);
                    if (values[i] instanceof One) {
                        values[i] = ((One) values[i]).pb.getValue();
                    }
                }
            }
        }

        @Override
        public void loadJSON(JSONCall call) {
            call.notifyError(new UnsupportedOperationException());
        }

        @Override
        public <M> M toModel(Class<M> modelClass, Object data) {
            return modelClass.cast(data);
        }

        @Override
        public Object toJSON(InputStream is) throws IOException {
            throw new IOException();
        }

        @Override
        public void runSafe(Runnable r) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
}
