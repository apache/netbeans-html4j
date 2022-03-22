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

import java.util.List;
import net.java.html.json.Models;
import static net.java.html.json.tests.Utils.fail;

final class PhaseExecutor<T> {
    private final T data;
    private int at = -1;
    private int retry = 10;
    private List<Action<T>> tasks = Models.asList();
    private List<Action<T>> clean = Models.asList();


    private PhaseExecutor(T data) {
        this.data = data;
    }
    // check whether all PhaseExecutor instances are started:
    /*
        this.alloc = new RuntimeException("Allocated for " + data);
        if (prev != null && prev.at == -1) {
            throw prev.alloc;
        }
        prev = this;
    }
    private final RuntimeException alloc;
    private static PhaseExecutor prev;
    */
    
    static <T> PhaseExecutor<T> schedule(PhaseExecutor[] phases, Init<T> data) throws Exception {
        if (phases[0] == null) {
            phases[0] = new PhaseExecutor<T>(data.initialize());
        } else {
            if (phases[0].at == -1) {
                fail("A PhaseExecutor hasn't been started! " + phases[0]);
            }
        }
        return phases[0];
    }

    PhaseExecutor<T> then(Action<T> a) {
        if (at == -1) {
            tasks.add(a);
        }
        return this;
    }

    PhaseExecutor<T> finalize(Action<T> a) {
        if (at == -1) {
            clean.add(a);
        }
        return this;
    }

    void start() throws Exception {
        if (at < 0) {
            at = 0;
        }

        while (at < tasks.size()) {
            Action a = tasks.get(at);
            try {
                a.run(data);
            } catch (Exception | Error ex) {
                if (retry-- == 0) {
                    cleanUp();
                    throw ex;
                } else {
                    throw (InterruptedException) new InterruptedException().initCause(ex);
                }
            }
            at++;
            retry = 10;
        }
        cleanUp();
    }

    private final void cleanUp() throws Exception {
        for (Action a : clean) {
            a.run(data);
        }
    }

    @FunctionalInterface
    public interface Init<T> {
        public T initialize() throws Exception;
    }

    @FunctionalInterface
    public interface Action<T> {
        public void run(T data) throws Exception;
    }
}
