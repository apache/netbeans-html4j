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
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

public class ReactionChainTest {
    @Model(className = "ReactiveChain", properties = {
        @Property(name = "state", type = int.class),
        @Property(name = "next", type = ReactiveChain.class)
    })
    static final class ReactiveChainCntrl {
    }

    class UseReactiveState {
        int counter;
        int value;

        void observeState() throws Exception {
            ReactiveChain fst = new ReactiveChain(6, null);
            final ReactiveChain snd;

            try (var handle = Models.react(() -> {
                counter++;
                ReactiveChain s = fst;
                value = 1;
                while (s != null) {
                    value *= s.getState();
                    s = s.getNext();
                }
            }, (c) -> {
                c.run();
            })) {
                assertEquals(counter, 1, "reaction performed once initially");
                assertEquals(value, fst.getState(), "value recorded");
                snd = new ReactiveChain(7, null);
                fst.setNext(snd);
                assertEquals(counter, 2, "reaction performed again");
                assertEquals(value, 42, "value re-recorded");
                snd.setState(10);
                assertEquals(counter, 3, "reaction performed again 3");
                assertEquals(value, 60, "value re-recorded 3");
                snd.setNext(new ReactiveChain(2, null));
                assertEquals(counter, 4, "reaction performed again 4");
                assertEquals(value, 120, "value re-recorded 4");
                fst.setNext(null);
                assertEquals(counter, 5, "reaction performed again 5");
                assertEquals(value, 6, "value re-recorded 5");
            }

            fst.setNext(snd);

            assertEquals(counter, 5, "changes are no longer observed");
            assertEquals(value, 6, "value hasn't been updated");
        }
    }

    @Test
    public void observeAState() throws Exception {
        UseReactiveState urs = new UseReactiveState();
        urs.observeState();
    }
}
