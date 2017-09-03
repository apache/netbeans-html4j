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
