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
    private final PhaseExecutor[] phases = { null };

    @Model(className = "Fullname", properties = {
        @Property(name = "firstName", type = String.class),
        @Property(name = "lastName", type = String.class)
    })
    static class FullnameCntrl {
    }

    class Data {
        GC m;
        BrwsrCtx ctx;
        Fullname removed;

        Data(GC m, BrwsrCtx ctx) {
            this.m = m;
            this.ctx = ctx;
        }
    }

    @KOTest
    public void noLongerNeededArrayElementsCanDisappear() throws Exception {
        PhaseExecutor.schedule(phases, () -> {
            BrwsrCtx ctx = Utils.newContext(GCKnockoutTest.class);
            Object exp = Utils.exposeHTML(GCKnockoutTest.class, """
            <ul id='ul' data-bind='foreach: all'>
              <li data-bind='text: firstName'/>
            </ul>
            """);
            GC m = Models.bind(new GC(), ctx);
            m.getAll().add(Models.bind(new Fullname("Jarda", "Tulach"), ctx));
            Models.applyBindings(m);
            return new Data(m, ctx);
        }).then((data) -> {
            int cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assertEquals(cnt, 1, "One child, but was " + cnt);

            var m = data.m;
            m.getAll().add(Models.bind(new Fullname("HTML", "Java"), data.ctx));
        }).then((data) -> {
            int cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assertEquals(cnt, 2, "Now two " + cnt);

            data.removed = data.m.getAll().remove(0);
        }).then((data) -> {
            var cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assertEquals(cnt, 1, "Again One " + cnt);

            Reference<?> ref = new WeakReference<Object>(data.removed);
            data.removed = null;
            assertGC(ref, "Can removed object disappear?");

            ref = new WeakReference<Object>(data.m);
            data.m = null;
            assertNotGC(ref, "Root model cannot GC");
        }).finalize((data) -> {
            Utils.exposeHTML(GCKnockoutTest.class, "");
        }).start();
    }

    private void assertGC(Reference<?> ref, String msg) throws Exception {
        for (int i = 0; i < 100; i++) {
            if (ref.get() == null) {
                return;
            }
            String gc = """
                        var max = arguments[0];
                        var arr = [];
                        for (var i = 0; i < max; i++) {
                          arr.push(i);
                        }
                        return arr.length;""";
            Object cnt = Utils.executeScript(GCKnockoutTest.class, gc, Math.pow(2.0, i));
            forceGC();
        }
        throw new OutOfMemoryError(msg);
    }

    @SuppressWarnings("deprecation")
    private static void forceGC() {
        System.gc();
        System.runFinalization();
    }

    private void assertNotGC(Reference<?> ref, String msg) throws Exception {
        for (int i = 0; i < 10; i++) {
            if (ref.get() == null) {
                throw new IllegalStateException(msg);
            }
            String gc = """
                        var max = arguments[0];
                        var arr = [];
                        for (var i = 0; i < max; i++) {
                          arr.push(i);
                        }
                        return arr.length;""";
            Object cnt = Utils.executeScript(GCKnockoutTest.class, gc, Math.pow(2.0, i));
            forceGC();
        }
    }

}
