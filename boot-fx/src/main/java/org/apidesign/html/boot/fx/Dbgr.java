/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */

package org.apidesign.html.boot.fx;

import java.lang.reflect.Method;
import java.util.logging.Level;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;
import static org.apidesign.html.boot.fx.AbstractFXPresenter.LOG;

/** Debugger bridge to shield us from propriatory impl APIs.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
