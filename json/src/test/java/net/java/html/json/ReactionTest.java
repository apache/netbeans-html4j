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

import java.io.Closeable;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

public class ReactionTest {
    // BEGIN: net.java.html.json.ReactionTest
    @Model(className = "ReactiveState", properties = {
        @Property(name = "state", type = int.class)
    })
    static final class ReactiveStateCntrl {
    }

    class UseReactiveState {
        int counter;
        int value;

        void observeState() throws Exception {
            ReactiveState state = new ReactiveState(7);

            Closeable handle = Models.react(() -> {
                counter++;
                value = state.getState();
            }, (c) -> c.run());

            assertEquals(counter, 1, "reaction performed once initially");
            assertEquals(value, state.getState(), "value recorded");

            state.setState(state.getState() * 2);

            assertEquals(counter, 2, "reaction performed again");
            assertEquals(value, state.getState(), "value re-recorded");

            handle.close();

            state.setState(state.getState() * 3);

            assertEquals(counter, 2, "changes are no longer observed");
            assertNotEquals(value, state.getState(), "value hasn't been updated");

            assertEquals(state.getState(), 42, "the meaning is clear");
        }
    }
    // END: net.java.html.json.ReactionTest

    @Test
    public void observeAState() throws Exception {
        UseReactiveState urs = new UseReactiveState();
        urs.observeState();
    }
}
