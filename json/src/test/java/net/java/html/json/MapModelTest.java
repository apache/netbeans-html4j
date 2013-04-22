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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apidesign.html.json.spi.ContextBuilder;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MapModelTest {
    private MapTechnology t;
    private Context c;

    @BeforeMethod public void initTechnology() {
        t = new MapTechnology();
        c = ContextBuilder.create().withTechnology(t).build();
    }
    
    @Test public void isThereABinding() throws Exception {
        Person p = new Person(c);
        p.setFirstName("Jarda");
        
        Map m = (Map)p.koData();
        Object v = m.get("firstName");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        assertEquals(o.changes, 1, "One change so far");
        
        assertEquals(o.get(), "Jarda", "Value should be in the map");
        
        o.set("Karle");
        
        assertEquals(p.getFirstName(), "Karle", "Model value updated");
        assertEquals(o.changes, 2, "Snd change");
    }
    
    @Test public void changeSex() {
        Person p = new Person(c);
        p.setFirstName("Trans");
        p.setSex(Sex.MALE);
        
        Map m = (Map)p.koData();
        Object o = m.get("changeSex");
        assertNotNull(o, "Function registered in the model");

        // TBD: invoke
    }

    private static final class One {
        int changes;
        private final Method getter;
        private final Object model;
        private final Method setter;
    
        One(Object m, String get, String set) throws NoSuchMethodException {
            this.model = m;
            if (get != null) {
                this.getter = m.getClass().getMethod(get);
            } else {
                this.getter = null;
            }
            if (set != null) {
                this.setter = m.getClass().getMethod(set, this.getter.getReturnType());
            } else {
                this.setter = null;
            }
        }
        
        Object get() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return getter.invoke(model);
        }
        
        void set(Object v) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            setter.invoke(model, v);
        }
    }
    
    private static final class MapTechnology implements Technology<Map<String,One>> {

        @Override
        public Map<String, One> wrapModel(Object model) {
            return new HashMap<>();
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
                One o = new One(model, b.getGetterName(), b.getSetterName());
                data.put(b.getPropertyName(), o);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void expose(FunctionBinding fb, Object model, Map<String, One> data) {
            try {
                data.put(fb.getFunctionName(), new One(model, null, null));
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            }
        }
    
    }
}
