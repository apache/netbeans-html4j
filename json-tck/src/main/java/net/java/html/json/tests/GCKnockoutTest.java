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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.netbeans.html.json.tck.KOTest;
import static net.java.html.json.tests.Utils.assertEquals;

@Model(className = "GC", properties = {
    @Property(name = "all", type = Fullname.class, array = true)
})
public class GCKnockoutTest {
    @Model(className = "Fullname", properties = {
        @Property(name = "firstName", type = String.class),
        @Property(name = "lastName", type = String.class)
    })
    static class FullnameCntrl {
    }
    
    @KOTest public void noLongerNeededArrayElementsCanDisappear() throws Exception {
        BrwsrCtx ctx = Utils.newContext(GCKnockoutTest.class);
        Object exp = Utils.exposeHTML(GCKnockoutTest.class,
            "<ul id='ul' data-bind='foreach: all'>\n"
            + "  <li data-bind='text: firstName'/>\n"
            + "</ul>\n"
        );
        try {
            GC m = Models.bind(new GC(), ctx);
            m.getAll().add(new Fullname("Jarda", "Tulach"));
            Models.applyBindings(m);

            int cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            m.getAll().add(new Fullname("HTML", "Java"));

            cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Now two " + cnt);

            Fullname removed = m.getAll().get(0);
            m.getAll().remove(0);

            cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assertEquals(cnt, 1, "Again One " + cnt);

            Reference<?> ref = new WeakReference<Object>(removed);
            removed = null;
            assertGC(ref, "Can removed object disappear?");
            
            ref = new WeakReference<Object>(m);
            m = null;
            assertNotGC(ref, "Root model cannot GC");
        } finally {
            Utils.exposeHTML(GCKnockoutTest.class, "");
        }
        
    }
    
    private void assertGC(Reference<?> ref, String msg) throws Exception {
        for (int i = 0; i < 100; i++) {
            if (ref.get() == null) {
                return;
            }
            String gc = "var max = arguments[0];\n"
                    +  "var arr = [];\n"
                    + "for (var i = 0; i < max; i++) {\n"
                    + "  arr.push(i);\n"
                    + "}\n"
                    + "return arr.length;";
            Object cnt = Utils.executeScript(GCKnockoutTest.class, gc, Math.pow(2.0, i));
            System.gc();
            System.runFinalization();
        }
        throw new OutOfMemoryError(msg);
    }
    
    private void assertNotGC(Reference<?> ref, String msg) throws Exception {
        for (int i = 0; i < 10; i++) {
            if (ref.get() == null) {
                throw new IllegalStateException(msg);
            }
            String gc = "var max = arguments[0];\n"
                    +  "var arr = [];\n"
                    + "for (var i = 0; i < max; i++) {\n"
                    + "  arr.push(i);\n"
                    + "}\n"
                    + "return arr.length;";
            Object cnt = Utils.executeScript(GCKnockoutTest.class, gc, Math.pow(2.0, i));
            System.gc();
            System.runFinalization();
        }
    }
    
}
