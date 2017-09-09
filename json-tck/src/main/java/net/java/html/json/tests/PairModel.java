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
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "Pair", targetId = "", properties = {
    @Property(name = "firstName", type = String.class),
    @Property(name = "lastName", type = String.class),
    @Property(name = "next", type = Pair.class)
})
class PairModel {
    static BrwsrCtx ctx;
    
    @ComputedProperty 
    static List<String> bothNames(String firstName, String lastName) {
        return Models.asList(firstName, lastName);
    }
    
    @ComputedProperty
    static Pair nextOne(Pair next) {
        return next;
    }
    
    @Function 
    static void assignFirstName(Pair m, String data) {
        ctx = BrwsrCtx.findDefault(Pair.class);
        m.setFirstName(data);
    }
}
