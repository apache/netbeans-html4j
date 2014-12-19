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
package net.java.html.json.tests;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.netbeans.html.json.tck.KOTest;

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
            assert cnt == 1 : "One child, but was " + cnt;

            m.getAll().add(new Fullname("HTML", "Java"));

            cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assert cnt == 2 : "Now two " + cnt;

            Fullname removed = m.getAll().get(0);
            m.getAll().remove(0);

            cnt = Utils.countChildren(GCKnockoutTest.class, "ul");
            assert cnt == 1 : "Again One " + cnt;

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
