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

import java.lang.reflect.Field;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Proto;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class PairModelTest {

    public PairModelTest() {
    }

    @Test
    public void testClonePairs() throws Exception {
        BrwsrCtx ctx = Contexts.newBuilder().build();
        Pair p = new Pair(null, null, new Pair("First", "Last", null));

        assertCtx(p, p.getNext());

        Pair c = Models.bind(p, ctx);

        assertCtx(c, c.getNext());
    }

    @Test
    public void testCloneDefaultPair() throws Exception {
        BrwsrCtx ctx = Contexts.newBuilder().build();
        Pair p = new Pair();
        Pair c = Models.bind(p, ctx);

        Assert.assertNotNull(p.getNext(), "Default value of p.next is created");
        Assert.assertNotNull(c.getNext(), "Default value of c.next is created");

        assertCtx(p, p.getNext());
        assertCtx(c, c.getNext());
    }

    private static void assertCtx(Pair first, Pair second) throws Exception {
        Field field = first.getClass().getDeclaredField("proto");
        field.setAccessible(true);
        Proto proto1 = (Proto) field.get(first);
        Proto proto2 = (Proto) field.get(second);
        assertEquals(proto1.getContext(), proto2.getContext());
    }
}
