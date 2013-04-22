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

import net.java.html.json.Context;
import net.java.html.json.People;
import net.java.html.json.Person;
import net.java.html.json.Sex;
import org.apidesign.html.json.spi.ContextBuilder;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JSONListTest implements Technology<Object> {
    
    public JSONListTest() {
    }

    @Test public void testConvertorOnAnObject() {
        Context c = ContextBuilder.create().withTechnology(this).build();
        
        Person p = new Person(c);
        p.setFirstName("1");
        p.setLastName("2");
        p.setSex(Sex.MALE);

        Object real = WrapperObject.find(p);
        assertEquals(this, real, "I am the right model");
    }
    
    @Test public void testConvertorOnAnArray() {
        Context c = ContextBuilder.create().withTechnology(this).build();
        
        Person p = new Person(c);
        p.setFirstName("1");
        p.setLastName("2");
        p.setSex(Sex.MALE);
        
        People people = new People(c);
        people.getInfo().add(p);

        Object real = WrapperObject.find(people.getInfo());
        assertTrue(real instanceof Object[], "It is an array: " + real);
        Object[] arr = (Object[])real;
        assertEquals(arr.length, 1, "Size is one");
        assertEquals(this, arr[0], "I am the right model");
    }

    @Override
    public Object wrapModel(Object model) {
        return this;
    }

    @Override
    public void bind(PropertyBinding b, Object model, Object data) {
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
    
}
