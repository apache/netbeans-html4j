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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/** This is an implementation package - just
 * include its JAR on classpath and use official browser builder API
 * to access the functionality.
 * <p>
 * Redirects JavaScript's messages to Java's {@link Logger}.
 *
 * @author Jaroslav Tulach
 */
public final class FXConsole {
    static final Logger LOG = Logger.getLogger(FXConsole.class.getName());
    
    private FXConsole() {
    }

    static void register(WebEngine eng) {
        JSObject fn = (JSObject) eng.executeScript(""
            + "(function(attr, l, c) {"
            + "  window.console[attr] = function(msg) { c.log(l, msg); };"
            + "})"
        );
        FXConsole c = new FXConsole();
        c.registerImpl(fn, "log", Level.INFO);
        c.registerImpl(fn, "info", Level.INFO);
        c.registerImpl(fn, "warn", Level.WARNING);
        c.registerImpl(fn, "error", Level.SEVERE);
    }
    
    private void registerImpl(JSObject eng, String attr, Level l) {
        eng.call("call", new Object[] { null, attr, l, this });
    }
    
    public void log(Level l, String msg) {
        LOG.log(l, msg);
    }
}
