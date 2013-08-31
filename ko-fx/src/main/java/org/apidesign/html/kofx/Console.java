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
package org.apidesign.html.kofx;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.js.JavaScriptBody;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Redirects JavaScript's messages to Java's {@link Logger}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Console {
    private static final Logger LOG = Logger.getLogger(Console.class.getName());
    
    private Console() {
    }

    static void register() {
        registerImpl("log", Level.INFO);
        registerImpl("info", Level.INFO);
        registerImpl("warn", Level.WARNING);
        registerImpl("error", Level.SEVERE);
    }
    
    @JavaScriptBody(args = { "attr", "l" }, 
        javacall = true, body = 
        "  window.console[attr] = function(m) {\n"
      + "    @org.apidesign.html.kofx.Console::log(Ljava/util/logging/Level;Ljava/lang/String;)(l, m);\n"
      + "  };\n"
    )
    private static native void registerImpl(String attr, Level l);
    
    static void log(Level l, String msg) {
        LOG.log(l, msg);
    }
}
