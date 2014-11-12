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

import net.java.html.BrwsrCtx;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach
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
        
        Map m = (Map)Models.toRaw(p);
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
        
        Map m = (Map)Models.toRaw(p);
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
    
    @Test public void canSetEnumAsString() throws Exception {
        Person p = Models.bind(new Person(), c);
        p.setFirstName("Jirka");
        p.setSex(Sex.MALE);
        
        Map m = (Map)Models.toRaw(p);
        Object v = m.get("sex");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One)v;
        
        o.set("FEMALE");

        assertEquals(p.getSex(), Sex.FEMALE, "Changed to female");
    }
    
    @Test public void derivedProperty() throws Exception {
        Person p = Models.bind(new Person(), c);
        
        Map m = (Map)Models.toRaw(p);
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
        
        Map m = (Map)Models.toRaw(p);
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
        
        Map m = (Map)Models.toRaw(p);
        Object o = m.get("changeSex");
        assertNotNull(o, "Function registered in the model");
        assertEquals(o.getClass(), One.class);
        
        One one = (One)o;
        assertNotNull(one.fb, "Function binding specified");
        
        one.fb.call("FEMALE", new Object());
        
        assertEquals(p.getSex(), Sex.FEMALE, "Changed");
    }
    
    @Test public void removeViaIterator() {
        People p = Models.bind(new People(), c);
        p.getNicknames().add("One");
        p.getNicknames().add("Two");
        p.getNicknames().add("Three");

        Map m = (Map)Models.toRaw(p);
        Object o = m.get("nicknames");
        assertNotNull(o, "List registered in the model");
        assertEquals(o.getClass(), One.class);
        One one = (One)o;
        
        
        assertEquals(one.changes, 0, "No change");
        
        Iterator<String> it = p.getNicknames().iterator();
        assertEquals(it.next(), "One");
        assertEquals(it.next(), "Two");
        it.remove();
        assertEquals(it.next(), "Three");
        assertFalse(it.hasNext());
        
        
        assertEquals(one.changes, 1, "One change");
    }
    
    @Test public void removeViaListIterator() {
        People p = Models.bind(new People(), c);
        p.getNicknames().add("One");
        p.getNicknames().add("Two");
        p.getNicknames().add("Three");

        Map m = (Map)Models.toRaw(p);
        Object o = m.get("nicknames");
        assertNotNull(o, "List registered in the model");
        assertEquals(o.getClass(), One.class);
        One one = (One)o;
        
        
        assertEquals(one.changes, 0, "No change");
        
        ListIterator<String> it = p.getNicknames().listIterator(1);
        assertEquals(it.next(), "Two");
        it.remove();
        assertEquals(it.next(), "Three");
        assertFalse(it.hasNext());
        
        
        assertEquals(one.changes, 1, "One change");
        
        it.set("3");
        assertEquals(p.getNicknames().get(1), "3");
        
        assertEquals(one.changes, 2, "Snd change");
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
