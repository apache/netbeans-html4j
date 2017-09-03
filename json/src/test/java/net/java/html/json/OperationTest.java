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
import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "OpModel", properties = {
    @Property(name = "names", type = String.class, array = true)
})
public class OperationTest {
    @ModelOperation static void add(OpModel m, String name, BrwsrCtx exp) {
        assertSame(BrwsrCtx.findDefault(OpModel.class), exp, "Context is passed in");
        m.getNames().add(name);
    }

    @ModelOperation static void add(OpModel m, int times, String name) throws IOException {
        while (times-- > 0) {
            m.getNames().add(name.toUpperCase());
        }
    }
    
    @ModelOperation static void copy(OpModel m, OpModel orig) {
        m.getNames().clear();
        m.getNames().addAll(orig.getNames());
    }

    @Test public void addOneToTheModel() {
        BrwsrCtx ctx = Contexts.newBuilder().build();
        OpModel m = Models.bind(new OpModel("One"), ctx);
        m.add("Second", ctx);
        assertEquals(m.getNames().size(), 2, "Both are there: " + m.getNames());
    }

    @Test public void addTwoUpperCasesToTheModel() {
        BrwsrCtx ctx = Contexts.newBuilder().build();
        OpModel m = Models.bind(new OpModel("One"), ctx);
        m.add(2, "Second");
        assertEquals(m.getNames().size(), 3, "Both are there: " + m.getNames());
        assertEquals(m.getNames().get(1), "SECOND", "Converted to upper case");
        assertEquals(m.getNames().get(2), "SECOND", "Also converted to upper case");
    }
    
    @Test public void noAnnonymousInnerClass() {
        int cnt = 0;
        for (Class<?> c : OpModel.class.getDeclaredClasses()) {
            cnt++;
            int dolar = c.getName().lastIndexOf('$');
            assertNotEquals(dolar, -1, "There is dolar in : " + c.getName());
            String res = c.getName().substring(dolar + 1);
            try {
                int number = Integer.parseInt(res);
                if (number == 1) {
                    // one is OK, #2 was a problem
                    continue;
                }
                fail("There seems to annonymous innerclass! " + c.getName() + "\nImplements: " 
                    + Arrays.toString(c.getInterfaces()) + " extends: " + c.getSuperclass()
                );
            } catch (NumberFormatException ex) {
                // OK, go on
            }
        }
        if (cnt == 0) {
            fail("There should be at least one inner class: " + cnt);
        }
    }
    
    @Test public void copyOperation() {
        OpModel orig = new OpModel("Ahoj", "Jardo");
        OpModel n = new OpModel();
        n.copy(orig);
        assertEquals(n.getNames().size(), 2, "Two elems");
        assertEquals(n.getNames().get(0), "Ahoj", "1st");
        assertEquals(n.getNames().get(1), "Jardo", "2nd");
    }
}
