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
package org.netbeans.html.json.impl;

import java.util.List;
import net.java.html.json.Model;
import net.java.html.json.OnReceive;
import net.java.html.json.Person;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "Employee", properties = {
    @Property(name = "person", type = Person.class),
    @Property(name = "employer", type = Employer.class),
    @Property(name = "call", type = Call.class)
})
public class EmployeeImpl {
    @OnReceive(url = "some/url")
    static void changePersonality(Employee e, Person p) {
        e.setPerson(p);
    }

    private static void callChangePers(Employee e) {
        Person per = new Person();
        e.changePersonalities(10, 3.14, "Ahoj", per);
        e.updatePersonalities("kuk", new Person(), 1, 2, "3", new Person());
        e.socketPersonalities("where", null);
    }

    @OnReceive(url = "some/other/url")
    static void changePersonalities(Employee e, List<Person> data, int i, double d, String s, Person o) {
        e.setCall(new Call(i, d, s, o, data.toArray(new Person[0])));
    }

    @OnReceive(url = "some/other/url", onError = "errorPersonalitiesWithEx")
    static void changePersonalitiesWithEx(Employee e, List<Person> data, int i, double d, String s, Person o) {
        e.setCall(new Call(i, d, s, o, data.toArray(new Person[0])));
    }

    static void errorPersonalitiesWithEx(Employee e, Exception ex) {
        e.setCall(new Call(-1, -1, null, null));
    }

    @OnReceive(url = "some/other/url", onError = "errorPersonalitiesWithParam")
    static void changePersonalitiesWithParam(Employee e, List<Person> data, int i, double d, String s, Person o) {
        e.setCall(new Call(i, d, s, o, data.toArray(new Person[0])));
    }

    static void errorPersonalitiesWithParam(Employee e, Exception ex, int i, double d, String s, Person o) {
        e.setCall(new Call(i, d, s, o));
    }

    @OnReceive(url = "{url}", method = "PUT", data = Person.class)
    static void updatePersonalities(Employee e, List<Person> p, int i, double d, String s, Person o) {
        e.setPerson(p.get(0));
    }

    @OnReceive(url = "{url}", method = "WebSocket", data = Person.class)
    static void socketPersonalities(Employee e, List<Person> p) {
        e.setPerson(p.get(0));
    }
    @OnReceive(url = "{url}", method = "WebSocket", data = Person.class)
    static void socketArrayPersonalities(Employee e, Person[] p) {
        e.setPerson(p[0]);
    }
    
    @Model(className="Call", properties = {
        @Property(name = "i", type=int.class),
        @Property(name = "d", type=double.class),
        @Property(name = "s", type=String.class),
        @Property(name = "p", type=Person.class),
        @Property(name = "data", type=Person.class, array = true)
    })
    static class CallModel {
    }
}
