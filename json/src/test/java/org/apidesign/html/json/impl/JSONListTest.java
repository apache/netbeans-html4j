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
package org.apidesign.html.json.impl;

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
        
        People people = Models.bind(new People(), c);
        people.getInfo().add(p);
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
        
        People people = Models.bind(new People(), c);
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
        
        People people = Models.bind(new People(), c);
        people.getInfo().add(p);

        Object real = WrapperObject.find(people.getInfo());
        assertEquals(real, this, "I am the model of the array");
    }

    @Test public void bindingsOnArray() {
        this.replaceArray = true;
        BrwsrCtx c = Contexts.newBuilder().register(Technology.class, this, 1).build();
        
        People p = Models.bind(new People(), c);
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
