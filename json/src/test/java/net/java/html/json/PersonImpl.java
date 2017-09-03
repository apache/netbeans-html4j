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
package net.java.html.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jaroslav Tulach
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
    public static List<String> bothNames(String firstName, String lastName) {
        return Arrays.asList(firstName, lastName);
    }
    
    @ComputedProperty
    public static String sexType(Sex sex) {
        return sex == null ? "unknown" : sex.toString();
    }
    
    @ComputedProperty static Sex attractedBy(Sex sex) {
        if (sex == null) {
            return null;
        }
        return sex == Sex.MALE ? Sex.FEMALE : Sex.MALE;
    }
    
    @Function
    static void changeSex(Person p, String data) {
        if (data != null) {
            p.setSex(Sex.valueOf(data));
            return;
        }
        if (p.getSex() == Sex.MALE) {
            p.setSex(Sex.FEMALE);
        } else {
            p.setSex(Sex.MALE);
        }
    }
    
    @Model(className = "People", instance = true, targetId="myPeople", properties = {
        @Property(array = true, name = "info", type = Person.class),
        @Property(array = true, name = "nicknames", type = String.class),
        @Property(array = true, name = "age", type = int.class),
        @Property(array = true, name = "sex", type = Sex.class)
    })
    public static class PeopleImpl {
        private int addAgeCount;
        private Runnable onInfoChange;
        
        @ModelOperation void onInfoChange(People self, Runnable r) {
            onInfoChange = r;
        }
        
        @ModelOperation void addAge42(People p) {
            p.getAge().add(42);
            addAgeCount++;
        }

        @OnReceive(url = "url", method = "WebSocket", data = String.class)
        void innerClass(People p, String d) {
        }
        
        @Function void inInnerClass(People p, Person data, int x, double y, String nick) throws IOException {
            p.getInfo().add(data);
            p.getAge().add(x);
            p.getAge().add((int)y);
            p.getNicknames().add(nick);
        }
        
        @ModelOperation void readAddAgeCount(People p, int[] holder, Runnable whenDone) {
            holder[0] = addAgeCount;
            whenDone.run();
        }
        
        @OnPropertyChange("age") void infoChange(People p) {
            if (onInfoChange != null) {
                onInfoChange.run();
            }
        }
    }
}
