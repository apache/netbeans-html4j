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
package net.java.html.json;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;


@Model(className = "Chain", builder = "put", properties = {
    @Property(name = "value", type = int.class),
    @Property(name = "next1", type = Chain.class),
    @Property(name = "next2", type = Chain.class),
    @Property(name = "next3", type = Chain.class),
    @Property(name = "next4", type = Chain.class),
})
public class ComputedChainTest {
    @ComputedProperty
    static int value1234(int value, Chain next1) {
        return value + next1.getValue234();
    }

    @ComputedProperty
    static int value234(int value, Chain next2) {
        return value + next2.getValue34();
    }

    @ComputedProperty
    static int value34(int value, Chain next3) {
        return value + next3.getValue4();
    }

    @ComputedProperty
    static int value4(int value, Chain next4) {
        return value + next4.getValue();
    }

    @Test
    public void chainOfValues() {
        Chain root = new Chain();
        Chain n1 = new Chain();
        Chain n2 = new Chain();
        Chain n3 = new Chain();
        Chain n4 = new Chain();

        root.setNext1(n1);
        n1.setNext2(n2);
        n2.setNext3(n3);
        n3.setNext4(n4);

        root.setValue(3);
        n1.setValue(4);
        n2.setValue(5);
        n3.setValue(6);
        n4.setValue(10);

        assertEquals(root.getValue1234(), 28);
    }

    @Test
    public void cyclicChain() {
        Chain root = new Chain();
        root.setValue(11);
        Chain next = new Chain();
        next.setValue(7);

        root.setNext1(next);
        next.setNext2(root);
        root.setNext3(next);
        next.setNext4(root);

        assertEquals(root.getValue1234(), 47);
    }

    @Test
    public void selfChain() {
        Chain root = new Chain();
        root.setValue(6);

        root.setNext1(root);
        root.setNext2(root);
        root.setNext3(root);
        root.setNext4(root);

        assertEquals(root.getValue1234(), 30);
    }


}
