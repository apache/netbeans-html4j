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
package org.netbeans.html.ko4j;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Technology;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Model(className = "Background", properties = {
    @Property(name = "identityHashCode", type = int.class),
})
public class OffThreadInitializationTest {
    private ScheduledExecutorService executor;

    @BeforeMethod
    public void initExecutor() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Test
    public void backgroundInitializationOfAModel() throws Exception {
        BrwsrCtx ctx = Contexts.newBuilder().register(Technology.class, new DummyTechnology(), 1).build();
        DummyTechnology.assertEquals(1, "One technology, created explicitly");
        final Background prototype = new Background();
        DummyTechnology.assertEquals(0, "No more technology on rebind");
        final Background b = Models.bind(prototype, ctx);
        Models.applyBindings(b);
        DummyTechnology.assertEquals(0, "Technology is shared!");


        Background b2 = executor.submit(new Callable<Background>() {
            @Override
            public Background call() throws Exception {
                return b.clone();
            }
        }).get();

        assertSameTech(b, b2);
        DummyTechnology.assertEquals(0, "Technology is still shared!");
    }

    private void assertSameTech(Background b, Background b2) {
        assertEquals(b.getIdentityHashCode(), b2.getIdentityHashCode(), "The hashcodes of the tech has to be the same");
    }

    private static final class DummyTechnology implements Technology<Object> {
        static int cnt;

        static void assertAtMax(int i, String msg) {
            if (cnt <= i) {
                cnt = 0;
            } else {
                fail(msg + " was: " + cnt);
            }
        }

        static void assertEquals(int i, String msg) {
            Assert.assertEquals(cnt, i, msg);
            cnt = 0;
        }

        DummyTechnology() {
            cnt++;
        }

        @Override
        public Object wrapModel(Object model) {
            return model;
        }

        @Override
        public <M> M toModel(Class<M> modelClass, Object data) {
            return modelClass.cast(data);
        }

        @Override
        public void bind(PropertyBinding b, Object model, Object data) {
            if (b.getPropertyName().equals("identityHashCode")) {
                ((Background)model).setIdentityHashCode(System.identityHashCode(this));
            }
        }

        @Override
        public void valueHasMutated(Object data, String propertyName) {
        }

        @Override
        public void expose(FunctionBinding fb, Object model, Object d) {
        }

        @Override
        public void applyBindings(Object data) {
        }

        @Override
        public Object wrapArray(Object[] arr) {
            return arr;
        }

        @Override
        public void runSafe(Runnable r) {
            r.run();
        }
    }

    public static final class DummyProvider implements Contexts.Provider {
        @Override
        public void fillContext(Contexts.Builder context, Class<?> requestor) {
            context.register(Technology.class, new DummyTechnology(), 0);
        }

    }
}
