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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
package org.netbeans.html.json.impl;

import java.util.List;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.impl.DeepChangeTest.MapTechnology;
import org.netbeans.html.json.impl.DeepChangeTest.One;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "TodoUI", properties = {
    @Property(name = "todos", type = Todo.class, array = true),
    @Property(name = "todoText", type = String.class)
})
public class ToDoTest {
    @Model(className = "Todo", properties = {
        @Property(name = "text", type = String.class),
        @Property(name = "done", type = boolean.class)
    })
    static class ItemCtrl {
    }

    @ComputedProperty
    static int remaining(
        List<Todo> todos, String todoText
    ) {
        int count = 0;
        for (Todo d : todos) {
            if (!d.isDone()) {
                count++;
            }
        }
        return count;
    }
    
    private MapTechnology t;
    private BrwsrCtx c;

    @BeforeMethod
    public void initTechnology() {
        t = new MapTechnology();
        c = Contexts.newBuilder().register(Technology.class, t, 1).
                register(Transfer.class, t, 1).build();
    }
    
    
    @Test public void checkAndUncheckFirstItem() throws Exception {
        TodoUI ui = Models.bind(
                new TodoUI(
                    null,
                    new Todo("First", false),
                    new Todo("2nd", true),
                    new Todo("Third", false)
                ), c);
        Models.applyBindings(ui);

        Map m = (Map) Models.toRaw(ui);
        Object v = m.get("remaining");
        assertNotNull(v, "Value should be in the map");
        assertEquals(v.getClass(), One.class, "It is instance of One");
        One o = (One) v;
        assertEquals(o.changes, 0, "No changes so far");
        assertTrue(o.pb.isReadOnly(), "Derived property");
        assertEquals(o.get(), 2);

        ui.getTodos().get(0).setDone(true);

        assertEquals(o.get(), 1);
        assertEquals(o.changes, 1, "One change so far");

        ui.getTodos().get(0).setDone(false);

        assertEquals(o.get(), 2);
        assertEquals(o.changes, 2, "2nd change so far");
        
    }
    
}
