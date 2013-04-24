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

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "Person", properties = {
    @Property(name = "firstName", type = String.class),
    @Property(name = "lastName", type = String.class),
    @Property(name = "sex", type = Sex.class)
})
final class PersonImpl {
    @ComputedProperty 
    public static String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
    
    @ComputedProperty
    public static String sexType(Sex sex) {
        return sex == null ? "unknown" : sex.toString();
    }
    
    @Function
    static void changeSex(Person p) {
        if (p.getSex() == Sex.MALE) {
            p.setSex(Sex.FEMALE);
        } else {
            p.setSex(Sex.MALE);
        }
    }
    
    @Model(className = "People", properties = {
        @Property(array = true, name = "info", type = Person.class),
        @Property(array = true, name = "nicknames", type = String.class),
        @Property(array = true, name = "age", type = int.class),
        @Property(array = true, name = "sex", type = Sex.class)
    })
    public class PeopleImpl {
    }
}
