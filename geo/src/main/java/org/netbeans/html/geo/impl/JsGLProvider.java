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
