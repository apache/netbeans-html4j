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
package org.netbeans.html.boot.impl;

import java.util.Map;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;


/**
 *
 * @author Jaroslav Tulach
 */
@JavaScriptResource("jsmethods.js")
public class JsMethods {
    private java.lang.Object value;
    
    @JavaScriptBody(args = {}, body = "return 42;")
    public static java.lang.Object fortyTwo() {
        return -42;
    }
    
    @JavaScriptBody(args = {"x", "y" }, body = "return x + y;")
    public static native int plus(int x, int y);
    
    @JavaScriptBody(args = {"x"}, body = "return x;")
    public static native int plus(int x);
    
    @JavaScriptBody(args = {}, body = "return this;")
    public static native java.lang.Object staticThis();
    
    @JavaScriptBody(args = {}, body = "return this;")
    public native java.lang.Object getThis();
    @JavaScriptBody(args = {"x"}, body = "return x;")
    public native int plusInst(int x);
    
    @JavaScriptBody(args = {}, body = "return true;")
    public static boolean truth() {
        return false;
    }
    
    @JavaScriptBody(args = { "r" }, javacall=true, body = "r.@java.lang.Runnable::run()();")
    public static native void callback(Runnable r);
    
    @JavaScriptBody(args = { "at", "arr" }, javacall = true, body =
          """
          var a = 0;
          for (var i = 0; i < arr.length; i++) {
            a = at.@org.netbeans.html.boot.impl.Arithm::sumTwo(II)(a, arr[i]);
          }
          return a;"""
    )
    private static native int sumTwo(Arithm at, int... arr);

    public static int sumArr(int... arr) {
        return sumTwo(new Arithm(), arr);
    }

    @JavaScriptBody(args = {"r"}, javacall = true, body =
        """
        var array = new Array();
        array[0]=1; array[1]=2;
        return r.@org.netbeans.html.boot.impl.Arithm::sumArr([Ljava/lang/Object;)(array);
        """
    )
    private static native int sumArr(Arithm r);

    public static int sumArr() {
        return sumArr(new Arithm());
    }

    @JavaScriptBody(args = { "x", "y" }, body = "return mul(x, y);")
    public static native int useExternalMul(int x, int y);
    
    @JavaScriptBody(args = { "m" }, javacall = true, body = "return m.@org.netbeans.html.boot.impl.JsMethods::getThis()();")
    public static native JsMethods returnYourSelf(JsMethods m);
    
    @JavaScriptBody(args = { "x", "y" }, javacall = true, body = "return @org.netbeans.html.boot.impl.JsMethods::useExternalMul(II)(x, y);")
    public static native int staticCallback(int x, int y);

    @JavaScriptBody(args = { "v" }, javacall = true, body = "return @java.lang.Integer::parseInt(Ljava/lang/String;)(v);")
    public static native int parseInt(String v);
    
    @JavaScriptBody(args = { "v" }, body = "return v.toString();")
    public static native String fromEnum(Enm v);
    
    @JavaScriptBody(args = "arr", body = "return arr;")
    public static native java.lang.Object[] arr(java.lang.Object[] arr);
    
    @JavaScriptBody(args = { "useA", "useB", "a", "b" }, body = """
        var l = 0;if (useA) l += a;
        if (useB) l += b;
        return l;
        """
    )
    public static native long chooseLong(boolean useA, boolean useB, long a, long b);
    
    protected void onError(java.lang.Object o) throws Exception {
        value = o;
    }
    
    java.lang.Object getError() {
        return value;
    }
    
    @JavaScriptBody(args = { "err" }, javacall = true, body = 
        "this.@org.netbeans.html.boot.impl.JsMethods::onError(Ljava/lang/Object;)(err);"
      + "return this.@org.netbeans.html.boot.impl.JsMethods::getError()();"
    )
    public native java.lang.Object recordError(java.lang.Object err);
    
    @JavaScriptBody(args = { "x", "y" }, body = "return x + y;")
    public static int plusOrMul(int x, int y) {
        return x * y;
    }
    
    @JavaScriptBody(args = { "x" }, keepAlive = false, body = "throw 'Do not call me!'")
    public static native int checkAllowGC(java.lang.Object x);

    @JavaScriptBody(args = { "map", "value" }, javacall = true, body =
       "map.@java.util.Map::put(Ljava/lang/Object;Ljava/lang/Object;)('key',value);"
    )
    public static native void callParamTypes(Map<String,Integer> map, int value);

    @JavaScriptBody(args = { "a", "b" }, body = "return [ a, b ];")
    public static native Object both(double a, double b);
    
    enum Enm {
        A, B;
    }
}

