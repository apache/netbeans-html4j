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

import net.java.html.json.Model;
import net.java.html.json.Property;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className="Man", properties={
    @Property(name = "name", type = String.class),
    @Property(name = "other", type = Address.class, array = true),
    @Property(name = "primary", type = Address.class),
    @Property(name = "childrenNames", type = String.class, array = true)
})
public class ConstructorTest {
    @Model(className = "Address", properties = {
        @Property(name = "place", type = String.class)
    })
    static final class AddressModel {
    }
    
    @Test public void initializedByDefault() {
        Man m = new Man();
        assertNotNull(m.getPrimary(), "Single subobjects are initialized");
    }
    
    @Test public void hasRichConstructor() {
        Man m = new Man("Jarda", new Address("home"), new Address("work"), new Address("hotel"));
        assertEquals(m.getName(), "Jarda");
        assertNotNull(m.getPrimary(), "Primary address specified");
        assertNotNull(m.getPrimary().getPlace(), "home");
        assertEquals(m.getOther().size(), 2, "Two other addresses");
    }
}
