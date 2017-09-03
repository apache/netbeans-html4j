/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.html.geo.impl;

import net.java.html.js.JavaScriptBody;
import org.netbeans.html.geo.spi.GLProvider;

/** Implementation class to deal with browser's <code>navigator.geolocation</code> 
 * object.
 *
 * @author Jaroslav Tulach
 */
public final class JsGLProvider extends GLProvider<Object, Long> {
    public JsGLProvider() {
    }
    
    @JavaScriptBody(args = {}, body = "return !!navigator.geolocation;")
    private static boolean hasGeolocation() {
        return false;
    }

    @JavaScriptBody(
        args = { "c", "onlyOnce", "enableHighAccuracy", "timeout", "maximumAge" }, 
        javacall = true, 
        body = 
        "var self = this;\n" +
        "var ok = function (position) {\n" +
        "  self.@org.netbeans.html.geo.impl.JsGLProvider::onLocation(Ljava/lang/Object;Ljava/lang/Object;)(c, position);\n" +
        "};\n" +
        "var fail = function (error) {\n" +
        "  self.@org.netbeans.html.geo.impl.JsGLProvider::onError(Ljava/lang/Object;Ljava/lang/String;I)(c, error.message, error.code);\n" +
        "};\n" +
        "var options = {};\n" +
        "options.enableHighAccuracy = enableHighAccuracy;\n" +
        "if (timeout >= 0) options.timeout = timeout;\n" +
        "if (maximumAge >= 0) options.maximumAge = maximumAge;\n" +
        "if (onlyOnce) {\n" +
        "  navigator.geolocation.getCurrentPosition(ok, fail, options);\n" +
        "  return 0;\n" +
        "} else {\n" +
        "  return navigator.geolocation.watchPosition(ok, fail, options);\n" +
        "}\n"
    )
    private long doStart(
        Query c,
        boolean onlyOnce, 
        boolean enableHighAccuracy,
        long timeout,
        long maximumAge
    ) {
        return -1;
    }
    
    protected void stop(long watch) {
    }

    @Override
    public Long start(Query c) {
        if (!hasGeolocation()) {
            return null;
        }
        return doStart(c, c.isOneTime(), c.isHighAccuracy(), c.getTimeout(), c.getMaximumAge());
    }
    
    final void onLocation(Object c, Object p) {
        callback((Query)c, timeStamp(p), p, null);
    }
    
    final void onError(Object c, final String msg, int code) {
        final Exception err = new Exception(msg + " errno: " + code) {
            @Override
            public String getLocalizedMessage() {
                return msg;
            }
        };
        callback((Query)c, 0L, null, err);
    }

    @Override
    @JavaScriptBody(args = {"watch"}, body = "navigator.geolocation.clearWatch(watch);")
    public native void stop(Long watch);

    @JavaScriptBody(args = { "p" }, body = "return p.timestamp;")
    private static native long timeStamp(Object position);

    @Override
    @JavaScriptBody(args = { "coords" }, body = "return coords.coords.latitude;")
    protected native double latitude(Object coords);

    @Override
    @JavaScriptBody(args = { "coords" }, body = "return coords.coords.longitude;")
    protected native double longitude(Object coords);

    @Override
    @JavaScriptBody(args = { "coords" }, body = "return coords.coords.accuracy;")
    protected native double accuracy(Object coords);

    @Override
    @JavaScriptBody(args = {"coords"}, body = "return coords.coords.altitude ? coords.coords.altitude : null;")
    protected native Double altitude(Object coords);

    @Override
    @JavaScriptBody(args = {"coords"}, body = "return coords.coords.altitudeAccuracy ? coords.coords.altitudeAccuracy : null;")
    protected native Double altitudeAccuracy(Object coords);

    @Override
    @JavaScriptBody(args = {"coords"}, body = "return coords.coords.heading ? coords.coords.heading : null;")
    protected native Double heading(Object coords);

    @Override
    @JavaScriptBody(args = {"coords"}, body = "return coords.coords.speed ? coords.coords.speed : null;")
    protected native Double speed(Object coords);

}
