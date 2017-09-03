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
package org.netbeans.html.boot.fx;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import net.java.html.js.JavaScriptBody;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class FXPresenterTst {
    @Test public void showClassLoader() {
        R run = new R();
        callback(run);
        assertEquals(run.cnt, 1, "Can call even private implementation classes");
    }
    
    @Test public void checkConsoleLogging() {
        class H extends Handler {
            LogRecord record;
            
            @Override
            public void publish(LogRecord record) {
                assert this.record == null;
                this.record = record;
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        H h = new H();
        FXConsole.LOG.addHandler(h);

        log("Ahoj");
        
        assert h.record != null : "Some log record obtained";
        assert "Ahoj".equals(h.record.getMessage()) : "It is our Ahoj: " + h.record.getMessage();
    }
    
    @JavaScriptBody(args = { "r" }, javacall = true, body = "r.@java.lang.Runnable::run()();")
    private static native void callback(Runnable r);

    @JavaScriptBody(args = { "msg" }, body = "console.log(msg);")
    private static native void log(String msg);

    private static class R implements Runnable {
        int cnt;

        @Override
        public void run() {
            cnt++;
        }
    }
}
