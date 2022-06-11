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

import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;
import net.java.html.json.sub.Street;
import net.java.html.json.sub.Telephone;
@Model(className = "Address", properties = {
    @Property(name = "street", type = net.java.html.json.sub.Street.class)
})
public class AdressTest {
    @Test
    public void addressHoldsAPerson() {
        Address address = new Address();
        assertNotNull(address.getStreet(), "Street is initialized");
    }
    
    @ComputedProperty
    public static String lowerCaseStreetName(Street street){
        return street.getName().toLowerCase();
    }
    
    @OnReceive(url = "")
    public static void getTelephone(Address model, Telephone phone){}
}
