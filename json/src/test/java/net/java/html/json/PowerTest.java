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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

// BEGIN: net.java.html.json.PowerTest
@Model(className = "Power", properties = {
    @Property(name = "value", type = double.class)
})
public class PowerTest {
    @ComputedProperty
    static double pow(double value) {
        return value * value;
    }
// FINISH: net.java.html.json.PowerTest

// BEGIN: net.java.html.json.PowerTest#sqrt
    @ComputedProperty(write = "sqrt")
    static double power(double value) {
        return value * value;
    }

    static void sqrt(Power model, double value) {
        model.setValue(Math.sqrt(value));
    }
// END: net.java.html.json.PowerTest#sqrt

    @Test
    public void computesPower() {
// BEGIN: net.java.html.json.PowerTest#computesPower
        Power p = new Power(3);
        assertEquals(p.getPow(), 9, 0.01);
// END: net.java.html.json.PowerTest#computesPower
    }

    @Test
    public void canSetComputedProperty() {
// BEGIN: net.java.html.json.PowerTest#canSetComputedProperty
        Power p = new Power();
        p.setPower(4.0);
        assertEquals(2.0, p.getValue(), 0.01, "Adjusted to square of four");
        assertEquals(4.0, p.getPower(), 0.01, "Was set to four");
        assertEquals(4.0, p.getPow(), 0.01, "Kept in sync with power property");
// END: net.java.html.json.PowerTest#canSetComputedProperty
    }
}
