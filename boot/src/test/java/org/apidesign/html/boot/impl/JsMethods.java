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
package org.apidesign.html.boot.impl;

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;


/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
@JavaScriptResource("jsmethods.js")
public class JsMethods {
    @JavaScriptBody(args = {}, body = "return 42;")
    public static Object fortyTwo() {
        return -42;
    }
    
    @JavaScriptBody(args = {"x", "y" }, body = "return x + y;")
    public static native int plus(int x, int y);
    
    @JavaScriptBody(args = {"x"}, body = "return x;")
    public static native int plus(int x);
    
    @JavaScriptBody(args = {}, body = "return this;")
    public static native Object staticThis();
    
    @JavaScriptBody(args = {}, body = "return this;")
    public native Object getThis();
    @JavaScriptBody(args = {"x"}, body = "return x;")
    public native int plusInst(int x);
    
    @JavaScriptBody(args = {}, body = "return true;")
    public static boolean truth() {
        return false;
    }
    
    @JavaScriptBody(args = { "r" }, javacall=true, body = "r.@java.lang.Runnable::run()();")
    public static native void callback(Runnable r);
    
    @JavaScriptBody(args = { "at", "arr" }, javacall = true, body =
          "var a = 0;\n"
        + "for (var i = 0; i < arr.length; i++) {\n"
        + "  a = at.@org.apidesign.html.boot.impl.Arithm::sumTwo(II)(a, arr[i]);\n"
        + "}\n"
        + "return a;"
    )
    private static native int sumArr(Arithm at, int... arr);
    
    public static int sumArr(int... arr) {
        return sumArr(new Arithm(), arr);
    }
    
    @JavaScriptBody(args = { "x", "y" }, body = "return mul(x, y);")
    public static native int useExternalMul(int x, int y);
    
    @JavaScriptBody(args = { "m" }, javacall = true, body = "return m.@org.apidesign.html.boot.impl.JsMethods::getThis()();")
    public static native JsMethods returnYourSelf(JsMethods m);
    
    @JavaScriptBody(args = { "x", "y" }, javacall = true, body = "return @org.apidesign.html.boot.impl.JsMethods::useExternalMul(II)(x, y);")
    public static native int staticCallback(int x, int y);

    @JavaScriptBody(args = { "v" }, javacall = true, body = "return @java.lang.Integer::parseInt(Ljava/lang/String;)(v);")
    public static native int parseInt(String v);
    
    @JavaScriptBody(args = { "useA", "useB", "a", "b" }, body = "var l = 0;"
        + "if (useA) l += a;\n"
        + "if (useB) l += b;\n"
        + "return l;\n"
    )
    public static native long chooseLong(boolean useA, boolean useB, long a, long b);
}
