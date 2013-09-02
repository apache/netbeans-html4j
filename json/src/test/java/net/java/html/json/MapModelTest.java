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
package net.java.html.json;

import net.java.html.BrwsrCtx;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.impl.WrapperObject;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MapModelTest {
    private MapTechnology t;
    private BrwsrCtx c;

    @BeforeMethod public void initTechnology() {
        t = new MapTechnology();
        c = Contexts.newBuilder().register(Technology.class, t, 1).
            register(Transfer.class, t, 1).build();
    }
    
    @Test public void isThereABinding() throws Exception {
        Person p = Models.bind(new Person(), c).applyBindings();
        p.setFirstName("Jarda");
        
        Map m = (Map)WrapperObject.find(p);
        Object v = m.get("firstName");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 1, "One change so far");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
        
        assertEquals(o.get(), "Jarda", "Value should be in the map");
        
        o.set("Karle");
        
        assertEquals(p.getFirstName(), "Karle", "Model value updated");
        assertEquals(o.changes, 2, "Snd change");
    }
    
    
    @Test public void dontNotifySameProperty() throws Exception {
        Person p = Models.bind(new Person(), c);
        p.setFirstName("Jirka");
        
        Map m = (Map)WrapperObject.find(p);
        Object v = m.get("firstName");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 0, "No change so far the only one change happened before we connected");
        
        p.setFirstName(new String("Jirka"));
        assertEquals(o.changes, 0, "No change so far, the value is the same");
        
        p.setFirstName("Jarda");
        assertFalse(o.pb.isReadOnly(), "Mutable property");
        
        assertEquals(o.get(), "Jarda", "Value should be in the map");
        
        o.set("Karle");
        
        assertEquals(p.getFirstName(), "Karle", "Model value updated");
        assertEquals(o.changes, 2, "Snd change");
    }
    
    @Test public void derivedProperty() throws Exception {
        Person p = Models.bind(new Person(), c);
        
        Map m = (Map)WrapperObject.find(p);
        Object v = m.get("fullName");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertTrue(o.pb.isReadOnly(), "Mutable property");
    }
    
    @Test public void changeSex() {
        Person p = Models.bind(new Person(), c);
        p.setFirstName("Trans");
        p.setSex(Sex.MALE);
        
        Map m = (Map)WrapperObject.find(p);
        Object o = m.get("changeSex");
        assertNotNull(o, "Function registered in the model");
        assertEquals(o.getClass(), One.class);
        
        One one = (One)o;
        assertNotNull(one.fb, "Function binding specified");
        
        one.fb.call(null, null);
        
        assertEquals(p.getSex(), Sex.FEMALE, "Changed");
    }
    
    @Test public void setSex() {
        Person p = Models.bind(new Person(), c);
        p.setFirstName("Trans");
        
        Map m = (Map)WrapperObject.find(p);
        Object o = m.get("changeSex");
        assertNotNull(o, "Function registered in the model");
        assertEquals(o.getClass(), One.class);
        
        One one = (One)o;
        assertNotNull(one.fb, "Function binding specified");
        
        one.fb.call("FEMALE", new Object());
        
        assertEquals(p.getSex(), Sex.FEMALE, "Changed");
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
    implements Technology<Map<String,One>>, Transfer {

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
            Map<?,?> map = obj instanceof Map ? (Map<?,?>)obj : null;
            for (int i = 0; i < Math.min(props.length, values.length); i++) {
                if (map == null) {
                    values[i] = null;
                } else {
                    values[i] = map.get(props[i]);
                    if (values[i] instanceof One) {
                        values[i] = ((One)values[i]).pb.getValue();
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
