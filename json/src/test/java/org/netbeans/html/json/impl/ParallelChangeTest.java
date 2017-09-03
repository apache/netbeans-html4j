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

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.impl.DeepChangeTest.One;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class ParallelChangeTest {
    private DeepChangeTest.MapTechnology t;
    private BrwsrCtx c;

    @BeforeMethod public void initTechnology() {
        t = new DeepChangeTest.MapTechnology();
        c = Contexts.newBuilder().register(Technology.class, t, 1).
            register(Transfer.class, t, 1).build();
    }

    @Test
    public void multipleValues() throws InterruptedException {
        doTest(true);
    }

    @Test
    public void singleValue() throws InterruptedException {
        doTest(false);
    }

    private void doTest(boolean multipleValues) throws InterruptedException {
        class Test implements Runnable {
            final int offset;
            final Depending dep;
            private Error error;

            public Test(int index, Depending dep) {
                this.offset = index;
                this.dep = dep;
            }

            @Override
            public void run() {
                try {
                    int value = dep.getValuePlusAdd();
                    assertEquals(value, offset + 11, "Offset " + offset + " plus one plus ten");
                } catch (Error err) {
                    this.error = err;
                }
            }

            private void assertException() {
                if (error != null) {
                    throw error;
                }
            }
        }

        Depending[] deps = new Depending[2];
        BlockingValue[] values = new BlockingValue[deps.length];
        CountDownLatch blockInCall = new CountDownLatch(deps.length);
        Test[] runs = new Test[deps.length];
        ExecutorService exec = Executors.newFixedThreadPool(deps.length);
        for (int i = 0; i < deps.length; i++) {
            if (multipleValues) {
                values[i] = BlockingValueCntrl.create(c);
            } else {
                values[i] = i == 0 ? BlockingValueCntrl.create(c) : values[0];
            }
            deps[i] = DependingCntrl.create(c, values[i], 10);
            runs[i] = new Test(0, deps[i]);
        }
        BlockingValueCntrl.initialize(blockInCall);
        for (int i = 0; i < deps.length; i++) {
            exec.execute(runs[i]);
        }

        exec.awaitTermination(1, TimeUnit.SECONDS);

        for (int i = 0; i < deps.length; i++) {
            Map raw = (Map) Models.toRaw(deps[i]);
            One value = (One) raw.get("valuePlusAdd");
            value.assertNoChange("No changes yet for index " + i);
        }

        for (int i = 0; i < deps.length; i++) {
            runs[i].assertException();
            values[i].setValue(30);
        }

        for (int i = 0; i < deps.length; i++) {
            Map raw = (Map) Models.toRaw(deps[i]);
            One value = (One) raw.get("valuePlusAdd");
            value.assertChange("A change for index " + i);
        }

        for (int i = 0; i < deps.length; i++) {
            assertEquals(deps[i].getValuePlusAdd(), 41, "[" + i + "] = 0 plus 30 plus one plus 10");
        }
    }

    @Model(className="BlockingValue", properties = {
        @Property(name = "value", type = int.class)
    })
    static class BlockingValueCntrl {
        private static CountDownLatch latch;

        static void initialize(CountDownLatch l) {
            latch = l;
        }

        @ComputedProperty
        static int plusOne(int value)  {
            if (latch != null) {
                latch.countDown();
                try {
                    latch.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return value + 1;
        }

        static BlockingValue create(BrwsrCtx c) {
            return Models.bind(new BlockingValue(), c);
        }
    }

    @Model(className = "Depending", properties = {
        @Property(name = "add", type = int.class),
        @Property(name = "dep", type = BlockingValue.class)
    })
    static class DependingCntrl {
        @ComputedProperty
        static int valuePlusAdd(BlockingValue dep, int add) {
            return dep.getPlusOne() + add;
        }

        static Depending create(BrwsrCtx c, BlockingValue value, int add) {
            Depending d = Models.bind(new Depending(add, null), c);
            d.setDep(value);
            return d;
        }
    }

}
