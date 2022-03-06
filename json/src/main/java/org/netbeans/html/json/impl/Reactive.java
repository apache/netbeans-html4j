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
package org.netbeans.html.json.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
import org.netbeans.html.json.spi.Proto;

public class Reactive implements Closeable {
    private final Executor onChange;
    private final Runnable reaction;
    private final Proto proto;

    private Reactive(Runnable reaction, Executor onChange) {
        this.reaction = reaction;
        this.onChange = onChange;
        this.proto = ReactiveType.TYPE.createProto(this, ReactiveTech.CTX);
        proto.applyBindings();
    }

    private void reactionWithRecording() {
        reaction(true);
    }

    private void reaction(boolean run) {
        proto.acquireLock("reaction");
        if (run) {
            reaction.run();
        }
        proto.releaseLock();
    }

    Proto getProto() {
        return proto;
    }

    Runnable getReaction() {
        return reaction;
    }

    public static Closeable react(Runnable reaction, Executor onChange) {
        Reactive r = new Reactive(reaction, onChange);
        r.reactionWithRecording();
        return r;
    }

    void valueHasMutated(String propertyName) {
        onChange.execute(this::reactionWithRecording);
    }


    @Override
    public void close() throws IOException {
        reaction(false);
    }
}
