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

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

public class DbgrTest implements Callback<String, Void> {

    public DbgrTest() {
    }

    @Test
    public void initializeDebuggerForWebView() throws Exception {
        Dbgr[] instance = { null };
        CountDownLatch cdl = new CountDownLatch(1);
        Platform.runLater(() -> {
            WebView wv = new WebView();
            WebEngine engine = wv.getEngine();
            instance[0] = new Dbgr(engine, this);
            cdl.countDown();
        });

        cdl.await();
        assertNotNull(instance[0], "Instance of Dbgr class created");
        assertNotNull(instance[0].dbg, "Debugger initialized");
        assertNotNull(instance[0].sendMsg, "Send method initialized");
    }

    @Override
    public Void call(String p) {
        return null;
    }
}
