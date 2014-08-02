/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package net.java.html.json;

import java.io.IOException;
import java.util.Arrays;
import net.java.html.BrwsrCtx;
import org.apidesign.html.context.spi.Contexts;
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
