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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apidesign.html.json.spi.ContextBuilder;
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
    }
    
    

    private static final class One {
        int changes;
        private final Method getter;
        private final Object model;
    
        One(Object m, String get) throws NoSuchMethodException {
            this.model = m;
            this.getter = m.getClass().getMethod(get);
        }
        
        Object get() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return getter.invoke(model);
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
                One o = new One(model, b.getGetterName());
                data.put(b.getPropertyName(), o);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            }
        }
    
    }
}
