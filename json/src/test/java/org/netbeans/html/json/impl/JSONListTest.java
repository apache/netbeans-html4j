/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import net.java.html.json.People;
import net.java.html.json.Person;
import net.java.html.json.Sex;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JSONListTest implements Technology<Object> {
    private boolean replaceArray;
    private final Map<String,PropertyBinding> bindings = new HashMap<String,PropertyBinding>();
    
    public JSONListTest() {
    }
    
    @BeforeMethod public void clear() {
        replaceArray = false;
    }

    @Test public void testConvertorOnAnObject() {
        BrwsrCtx c = Contexts.newBuilder().register(Technology.class, this, 1).build();
        
        Person p = Models.bind(new Person(), c);
        p.setFirstName("1");
        p.setLastName("2");
        p.setSex(Sex.MALE);

        Object real = WrapperObject.find(p);
        assertEquals(this, real, "I am the right model");
    }
    
    @Test public void testConvertorOnAnArray() {
        BrwsrCtx c = Contexts.newBuilder().register(Technology.class, this, 1).build();
        
        Person p = Models.bind(new Person(), c);
        p.setFirstName("1");
        p.setLastName("2");
        p.setSex(Sex.MALE);
        
        People people = Models.bind(new People(p), c).applyBindings();
        assertEquals(people.getInfo().toString(), "[{\"firstName\":\"1\",\"lastName\":\"2\",\"sex\":\"MALE\"}]", "Converted to real JSON");
        
        PropertyBinding pb = bindings.get("info");
        assertNotNull(pb, "Binding for info found");
        
        Object real = pb.getValue();
        assertTrue(real instanceof Object[], "It is an array: " + real);
        Object[] arr = (Object[])real;
        assertEquals(arr.length, 1, "Size is one");
        assertEquals(this, arr[0], "I am the right model");
    }
    
    @Test public void testNicknames() {
        BrwsrCtx c = Contexts.newBuilder().register(Technology.class, this, 1).build();
        
        People people = Models.bind(new People(), c).applyBindings();
        people.getNicknames().add("One");
        people.getNicknames().add("Two");
        
        PropertyBinding pb = bindings.get("nicknames");
        assertNotNull(pb, "Binding for info found");
        
        Object real = pb.getValue();
        assertTrue(real instanceof Object[], "It is an array: " + real);
        Object[] arr = (Object[])real;
        assertEquals(arr.length, 2, "Length two");
        assertEquals(arr[0], "One", "Text should be in the model");
        assertEquals(arr[1], "Two", "2nd text in the model");
    }
    
    @Test public void testConvertorOnAnArrayWithWrapper() {
        this.replaceArray = true;
        BrwsrCtx c = Contexts.newBuilder().register(Technology.class, this, 1).build();
        
        Person p = Models.bind(new Person(), c);
        p.setFirstName("1");
        p.setLastName("2");
        p.setSex(Sex.MALE);
        
        People people = Models.bind(new People(), c).applyBindings();
        people.getInfo().add(p);
        
        Object real = WrapperObject.find(people.getInfo());
        assertEquals(real, this, "I am the model of the array");
    }

    @Test public void bindingsOnArray() {
        this.replaceArray = true;
        BrwsrCtx c = Contexts.newBuilder().register(Technology.class, this, 1).build();
        
        People p = Models.bind(new People(), c).applyBindings();
        p.getAge().add(30);
        
        PropertyBinding pb = bindings.get("age");
        assertNotNull(pb, "There is a binding for age list");
        
        assertEquals(pb.getValue(), this, "I am the model of the array");
    }

    @Override
    public Object wrapModel(Object model) {
        return this;
    }

    @Override
    public void bind(PropertyBinding b, Object model, Object data) {
        bindings.put(b.getPropertyName(), b);
    }

    @Override
    public void valueHasMutated(Object data, String propertyName) {
    }

    @Override
    public void expose(FunctionBinding fb, Object model, Object d) {
    }

    @Override
    public void applyBindings(Object data) {
    }

    @Override
    public Object wrapArray(Object[] arr) {
        return replaceArray ? this : arr;
    }

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        return modelClass.cast(data);
    }

    @Override
    public void runSafe(Runnable r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
