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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

final class DumpStack extends TimerTask {

    private static final Timer TIMER = new Timer("Dump Stack Watchdog");
    private final long created = System.currentTimeMillis();
    private int count = 5 * 12;

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        long after = (System.currentTimeMillis() - created) / 1000;
        sb.append("Thread dump after ").append(after).append(" s from start:\n");
        for (Map.Entry<Thread, StackTraceElement[]> info : Thread.getAllStackTraces().entrySet()) {
            sb.append(info.getKey().getName()).append("\n");
            for (StackTraceElement e : info.getValue()) {
                sb.append("    ").append(e.getClassName()).append(".").
                        append(e.getMethodName()).append("(").append(e.getFileName()).
                        append(":").append(e.getLineNumber()).append(")\n");
            }
        }
        System.err.println(sb.toString());
        if (count-- < 0) {
            System.err.println("DumpStack timeout. Exiting.");
            System.exit(1);
        }
    }

    public static void initialize() {
        final int fiveSec = 5000;
        TIMER.schedule(new DumpStack(), fiveSec, 2 * fiveSec);
    }
}
