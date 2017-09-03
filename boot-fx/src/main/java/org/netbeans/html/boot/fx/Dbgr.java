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

import java.lang.reflect.Method;
import java.util.logging.Level;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;
import static org.netbeans.html.boot.fx.AbstractFXPresenter.LOG;

/** Debugger bridge to shield us from propriatory impl APIs.
 *
 * @author Jaroslav Tulach
 */
final class Dbgr {
    final Object dbg;
    final Method sendMsg;
    
    Dbgr(WebEngine eng, Callback<String,Void> callback) {
        Object d;
        Method m;
        try {
            d = eng.getClass().getMethod("impl_getDebugger").invoke(eng); // NOI18N
            Class<?> debugger = eng.getClass().getClassLoader().loadClass("com.sun.javafx.scene.web.Debugger"); // NOI18N
            debugger.getMethod("setEnabled", boolean.class).invoke(d, true); // NOI18N
            debugger.getMethod("setMessageCallback", Callback.class).invoke(d, callback); // NOI18N
            m = debugger.getMethod("sendMessage", String.class); // NOI18N
        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
            d = null;
            m = null;
        }
        dbg = d;
        sendMsg = m;
    }

    void sendMessage(String msg) {
        try {
            if (dbg != null) {
                sendMsg.invoke(dbg, msg);
            }
        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }
    
}
