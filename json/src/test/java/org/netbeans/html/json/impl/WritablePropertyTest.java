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

import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

@Model(className = "WritablePropertyModel", instance = true, properties = {
    @Property(name = "meaning", type = int.class)
})
public class WritablePropertyTest {
    private int value;

    @ComputedProperty(write = "changeMeaning")
    static int realMeaning(int meaning) {
        return 40 + meaning;
    }

    void changeMeaning(WritablePropertyModel model, int value) {
        this.value = value;
    }

    @ModelOperation
    void readValue(WritablePropertyModel model, int[] store) {
        store[0] = this.value;
    }

    @Test
    public void changeValueInTest() {
        WritablePropertyModel model = new WritablePropertyModel();
        model.setMeaning(2);

        assertEquals(model.getRealMeaning(), 42, "Changed to 42");

        model.setRealMeaning(84);

        assertEquals(model.getRealMeaning(), 42, "Unchanged...");

        int[] holder = { -1 };
        model.readValue(holder);
        assertEquals(holder[0], 84, "...but value changed!");

    }
}
