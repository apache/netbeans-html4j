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
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

// BEGIN: net.java.html.json.SquaresTest
@Model(className = "Formula", properties = {
    @Property(name = "a", type = int.class),
    @Property(name = "b", type = int.class),
})
public class SquaresTest {
    @ComputedProperty
    static int plus(Formula model) {
        return model.getA() + model.getB();
    }

    @ComputedProperty
    static int minus(Formula model) {
        return model.getA() - model.getB();
    }

    @ComputedProperty
    static int aPlusBTimesAMinusB(Formula both) {
        return both.getPlus() * both.getMinus();
    }
// FINISH: net.java.html.json.SquaresTest

    @ComputedProperty(write = "unsquare")
// BEGIN: net.java.html.json.SquaresTest#aSquareMinusBSquareClassic
    static int aSquareMinusBSquareClassic(Formula squares) {
        return squares.getA() * squares.getA() - squares.getB() * squares.getB();
    }
// END: net.java.html.json.SquaresTest#aSquareMinusBSquareClassic

// BEGIN: net.java.html.json.SquaresTest#aSquareMinusBSquare
    @ComputedProperty
    static int aSquareMinusBSquare(int a, int b) {
        return a * a - b * b;
    }
// END: net.java.html.json.SquaresTest#aSquareMinusBSquare

    static void unsquare(Formula f, int compute) {
        for (int a = 0; a < 10; a++) {
            for (int b = 0; b < 10; b++) {
                Formula tmp = new Formula(a, b);
                if (tmp.getAPlusBTimesAMinusB() == compute) {
                    f.setA(a);
                    f.setB(b);
                    return;
                }
            }
        }
        throw new IllegalStateException("Cannot find values of a and b for " + compute);
    }

    @ComputedProperty
    static int dontTouchA(Formula both) {
        both.setA(10);
        return -1;
    }

    @Test
    public void threeAndTwo() {
// BEGIN: net.java.html.json.SquaresTest#threeAndTwo
        final Formula formula = new Formula(3, 2);
        assertEquals(formula.getAPlusBTimesAMinusB(), 5);
// END: net.java.html.json.SquaresTest#threeAndTwo
        assertEquals(formula.getASquareMinusBSquare(), 5);
    }

    @Test
    public void fiveAndTwo() {
// BEGIN: net.java.html.json.SquaresTest#fiveAndTwo
        final Formula formula = new Formula(5, 2);
        assertEquals(formula.getAPlusBTimesAMinusB(), 21);
// END: net.java.html.json.SquaresTest#fiveAndTwo
        assertEquals(formula.getASquareMinusBSquare(), 21);
    }

    @Test
    public void findTwoAndFive() {
        final Formula formula = new Formula();
        formula.setASquareMinusBSquareClassic(21);

        assertEquals(formula.getA(), 5);
        assertEquals(formula.getB(), 2);
    }

    @Test
    public void dontTouchA() {
        try {
            int result = new Formula(3, 2).getDontTouchA();
            fail("No result shall be produced" + result);
        } catch (IllegalStateException ex) {
            // OK
        }
    }
}
