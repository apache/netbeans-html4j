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
