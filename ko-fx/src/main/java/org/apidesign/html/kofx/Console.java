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

import java.util.logging.Logger;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Console {
    private static final Logger LOG = Logger.getLogger(Console.class.getName());
    
    private Console() {
    }

    static void register(WebEngine web) {
        ((JSObject)web.executeScript("window")).setMember("jconsole", new Console());
        web.executeScript("console.log = function(m) { jconsole.log(m); };");
        web.executeScript("console.info = function(m) { jconsole.log(m); };");
        web.executeScript("console.error = function(m) { jconsole.log(m); };");
        web.executeScript("console.warn = function(m) { jconsole.log(m); };");
    }
    
    public void log(String msg) {
        LOG.info(msg);
    }
}
