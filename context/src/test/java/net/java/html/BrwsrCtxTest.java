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
package net.java.html;

import org.netbeans.html.context.spi.Contexts;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class BrwsrCtxTest {
    
    public BrwsrCtxTest() {
    }


    @org.testng.annotations.Test
    public void canSetAssociateCtx() {
        final BrwsrCtx ctx = Contexts.newBuilder().build();
        final boolean[] arr = { false };
        
        assertNotSame(BrwsrCtx.findDefault(BrwsrCtxTest.class), ctx, "Not associated yet");
        ctx.execute(new Runnable() {
            @Override public void run() {
                assertSame(BrwsrCtx.findDefault(BrwsrCtxTest.class), ctx, "Once same");
                assertSame(BrwsrCtx.findDefault(BrwsrCtxTest.class), ctx, "2nd same");
                arr[0] = true;
            }
        });
        assertNotSame(BrwsrCtx.findDefault(BrwsrCtxTest.class), ctx, "Not associated again");
        assertTrue(arr[0], "Runnable was executed");
    }
    
    
    @Test public void defaultOrderOfRegistrations() {
        BrwsrCtx ctx = registerRs(Contexts.newBuilder());
        Class<? extends Runnable> clazz = Contexts.find(ctx, Runnable.class).getClass();
        assertEquals(clazz, R1.class, "R1 is registered at value 10");
    }
    
    @Test public void preferOne() {
        BrwsrCtx ctx = registerRs(Contexts.newBuilder("one"));
        Class<? extends Runnable> clazz = Contexts.find(ctx, Runnable.class).getClass();
        assertEquals(clazz, R1.class, "R1 is registered at value 10");
    }

    @Test public void preferTwo() {
        BrwsrCtx ctx = registerRs(Contexts.newBuilder("two"));
        Class<? extends Runnable> clazz = Contexts.find(ctx, Runnable.class).getClass();
        assertEquals(clazz, R2.class, "R2 is preferred");
    }

    @Test public void preferBoth() {
        BrwsrCtx ctx = registerRs(Contexts.newBuilder("one", "two"));
        Class<? extends Runnable> clazz = Contexts.find(ctx, Runnable.class).getClass();
        assertEquals(clazz, R1.class, "R1 is registered at value 10");
    }
    
    private static BrwsrCtx registerRs(Contexts.Builder b) {
        b.register(Runnable.class, new R1(), 10);
        b.register(Runnable.class, new R2(), 20);
        return b.build();
    }

    @Contexts.Id("one")
    static final class R1 implements Runnable {
        @Override
        public void run() {
        }
    }
    @Contexts.Id("two")
    static final class R2 implements Runnable {
        @Override
        public void run() {
        }
    }
    
}