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
package org.apidesign.html.geo.impl;

import net.java.html.js.JavaScriptBody;

/** Implementation class to deal with browser's <code>navigator.geolocation</code> 
 * object.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class JsG {
    protected JsG() {
        if (!getClass().getName().equals("net.java.html.geo.Position$Handle$JsH")) {
            throw new IllegalStateException();
        }
    }
    
    public abstract void onLocation(Object position);
    public abstract void onError(Object error);
    
    @JavaScriptBody(args = {}, body = "return !!navigator.geolocation;")
    public static boolean hasGeolocation() {
        return false;
    }

    @JavaScriptBody(
        args = { "onlyOnce", "enableHighAccuracy", "timeout", "maximumAge" }, 
        javacall = true, 
        body = 
        "var self = this;\n" +
        "var ok = function (position) {\n" +
        "  self.@org.apidesign.html.geo.impl.JsG::onLocation(Ljava/lang/Object;)(position);\n" +
        "};\n" +
        "var fail = function (error) {\n" +
        "  self.@org.apidesign.html.geo.impl.JsG::onError(Ljava/lang/Object;)(error);\n" +
        "};\n" +
        "var options = {};\n" +
        "options.enableHighAccuracy = enableHighAccuracy;\n" +
        "if (timeout >= 0) options.timeout = timeout;\n" +
        "if (maximumAge >= 0) options.maximumAge = maximumAge;\n" +
        "if (onlyOnce) {\n" +
        "  navigator.geolocation.getCurrentPosition(ok, fail);\n" +
        "  return 0;\n" +
        "} else {\n" +
        "  return navigator.geolocation.watchPosition(ok, fail);\n" +
        "}\n"
    )
    protected long start(
        boolean onlyOnce, 
        boolean enableHighAccuracy,
        long timeout,
        long maximumAge
    ) {
        return -1;
    }
    
    @JavaScriptBody(args = { "watch" }, body = "navigator.geolocation.clearWatch(watch);")
    protected void stop(long watch) {
    }

    @JavaScriptBody(args = { "self", "property" }, body = "return self[property];")
    public static Object get(Object self, String property) {
        return null;
    }

}
