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

import java.util.List;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import static net.java.html.json.tests.Utils.assertEquals;
import org.netbeans.html.json.tck.KOTest;

public class ObtainAndComputeTest {
    private PhaseExecutor[] phases = new PhaseExecutor[1];

    @KOTest
    public void obtainAndComputeTest() throws Throwable {
        PhaseExecutor.schedule(phases, () -> {
            BrwsrCtx ctx = Utils.newContext(ObtainAndComputeTest.class);
            Utils.exposeHTML(ObtainAndComputeTest.class, """
            <input type="text" data-bind="textInput: filter">
            <ul id='list' data-bind="foreach: filteredUsers">
               <li><div data-bind="text: email"></div></li>
            </ul>
            <button id='button' data-bind="click: nextUsers">more...</button>""");
            String d = """
            {
                "data": [{
                        "id": 1,
                        "email": "george.bluth@gmail.com"
                    }, {
                        "id": 2,
                        "email": "janet.weaver@gmail.com"
                    }, {
                        "id": 3,
                        "email": "emma.wong@gmail.com"
                    }, {
                        "email": "eve.holt@gmail.com"
                    }, {
                        "email": "charles.morris@rgmail.com"
                    }, {
                        "id": 6,
                        "email": "tracey.ramos@gmail.com"
                    }]
            }
            """;
            String url = Utils.prepareURL(ObtainAndComputeTest.class, d, "application/json");

            var m = Models.bind(new ObtainData("holt", url, false), ctx);
            m.applyBindings();
            return m;
        }).then((m) -> {
            int cnt = Utils.countChildren(ObtainAndComputeTest.class, "list");
            assertEquals(cnt, 0, "No filtered users so far: " + cnt);
            Utils.scheduleClick(ObtainAndComputeTest.class, "button", 100);
        }).then((m) -> {
            Utils.assertTrue(m.isGotReply(), "Expecting got reply: " + m);
        }).then((m) -> {
            assertEquals(6, m.getUsers().size(), "Expecting some users: " + m);
            assertEquals(1, m.getFilteredUsers().size(), "Only one holt matching filter: " + m);
            int cnt = Utils.countChildren(ObtainAndComputeTest.class, "list");
            assertEquals(cnt, 1, "Also one user: " + cnt);
        }).finalize((m) -> {
            Utils.exposeHTML(ObtainAndComputeTest.class, "");
        }).start();
    }

    //
    // application logic
    //

    @Model(className = "ObtainData", targetId = "", instance = true, properties = {
        @Property(name = "users", type = ObtainUser.class, array = true),
        @Property(name = "filter", type = String.class),
        @Property(name = "url", type = String.class),
        @Property(name = "gotReply", type = boolean.class)
    })
    static final class ObtainModel {
        @ComputedProperty
        public static List<ObtainUser> filteredUsers(List<ObtainUser> users, String filter) {
            List<ObtainUser> res = Models.asList();
            for (ObtainUser user : users) {
                if (user.getEmail().contains(filter)) {
                    res.add(user);
                }
            }
            return res;
        }

        @Function
        public static void nextUsers(ObtainData model) {
            model.loadUsers(model.getUrl());
        }

        @OnReceive(method = "GET", url = "{url}")
        public static void loadUsers(ObtainData model, ObtainMessage reply) {
            model.getUsers().addAll(reply.getData());
            model.setGotReply(true);
        }

        @Model(className = "ObtainMessage", properties = {
            @Property(name = "data", type = ObtainUser.class, array = true)})
        public static class MessageVMD {
        }

        @Model(className = "ObtainUser", properties = {
            @Property(name = "email", type = java.lang.String.class),}
        )
        public static class UserVMD {
        }
    }

}
