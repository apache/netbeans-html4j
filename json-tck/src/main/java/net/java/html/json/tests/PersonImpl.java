/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.java.html.json.tests;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "Person", properties = {
    @Property(name = "firstName", type = String.class),
    @Property(name = "lastName", type = String.class),
    @Property(name = "sex", type = Sex.class),
    @Property(name = "address", type = Address.class)
})
final class PersonImpl {
    @OnPropertyChange("firstName")
    static void onFirstNameChange(Person model) {
        // System.err.println("model updated to " + toDebug(model));
    }

    static String toDebug(Person model) {
        return model + "@0x" + Integer.toHexString(System.identityHashCode(model));
    }

    @ComputedProperty(write = "parseNames")
    public static String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    static void parseNames(Person p, String fullName) {
        String[] arr = fullName.split(" ");
        p.setFirstName(arr[0]);
        p.setLastName(arr[1]);
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

    @Model(className = "Address", properties = {
        @Property(name = "street", type = String.class)
    })
    static class Addrss {
    }
}
