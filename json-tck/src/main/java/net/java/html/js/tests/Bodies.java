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
package net.java.html.js.tests;

import java.util.concurrent.Callable;
import net.java.html.js.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach
 */
final class Bodies {
    @JavaScriptBody(args = { "a", "b" }, body = "return a + b;")
    public static native int sum(int a, int b);

    @JavaScriptBody(args = { "a", "b" }, javacall = true, body = 
        "return @net.java.html.js.tests.Bodies::sum(II)(a, b);"
    )
    public static native int sumJS(int a, int b);
    
    @JavaScriptBody(args = {"r"}, javacall = true, body = "r.@java.lang.Runnable::run()();")
    static native void callback(Runnable r);

    @JavaScriptBody(args = {"r"}, wait4js = false, keepAlive = false, javacall = true, body = "r.@java.lang.Runnable::run()();")
    static native void asyncCallback(Runnable r);
    
    @JavaScriptBody(args = {"c", "v"}, javacall = true, body = "var arr = c.@java.util.concurrent.Callable::call()(); arr.push(v); return arr;")
    static native Object callbackAndPush(Callable<String[]> c, String v);
    
    @JavaScriptBody(args = { "v" }, body = "return v;")
    public static native Object id(Object v);
    
    @JavaScriptBody(args = { "v" }, body = "return { 'x' : v };")
    public static native Object instance(int v);
    
    @JavaScriptBody(args = "o", body = "o.x++;")
    public static native void incrementX(Object o);

    @JavaScriptBody(args = "o", wait4js = true, body = "o.x++;")
    static native void incrementXAsync(Object o);

    @JavaScriptBody(args = "o", body = "return o.x;")
    public static native int readIntX(Object o);
    
    @JavaScriptBody(args = "o", body = "return o.x;")
    public static native Object readX(Object o);
    
    @JavaScriptBody(args = { "o", "x" }, keepAlive = false, body = "o.x = x;")
    public static native Object setX(Object o, Object x);

    @JavaScriptBody(args = { "c", "a", "b" }, keepAlive = false, javacall = true, body = """
        return c.@net.java.html.js.tests.Sum::sum(II)(a, b);
    """)
    public static native int sumIndirect(Sum c, int a, int b);

    @JavaScriptBody(args = { "c" }, javacall = true, body =
        """
        return {
          'sum' : function(a,b) {
             return c.@net.java.html.js.tests.Sum::sum(II)(a, b);
          }
        };
        """
    )
    public static native Object sumDelayed(Sum c);

    @JavaScriptBody(args = { "sum", "a", "b" }, body = "return sum.sum(a, b);")
    public static native int sumNow(Object sum, int a, int b);
    
    @JavaScriptBody(args = { "arr", "index" }, body = "return arr[index];")
    public static native Object select(Object[] arr, int index);

    @JavaScriptBody(args = { "arr" }, body = "return arr.length;")
    public static native int length(Object[] arr);
    
    @JavaScriptBody(args = { "o", "vo" }, body = "if (vo) o = o.valueOf(); return typeof o;")
    public static native String typeof(Object o, boolean useValueOf);

    @JavaScriptBody(args = { "b" }, body = "return typeof b;")
    public static native String typeof(boolean b);

    @JavaScriptBody(args = { "o" }, body = "return o.toString();")
    public static native String toString(Object o);

    @JavaScriptBody(args = { "o" }, body = "return Array.isArray(o);")
    public static native boolean isArray(Object o);

    @JavaScriptBody(args = { "arr", "i", "value" }, body = "arr[i] = value; return arr[i];")
    public static native String modify(String[] arr, int i, String value);
    
    @JavaScriptBody(args = {}, body = "return true;")
    public static native boolean truth();
    
    @JavaScriptBody(args = { "s" }, javacall = true, body = 
        "return s.@net.java.html.js.tests.Sum::sum([Ljava/lang/Object;)([1, 2, 3]);"
    )
    public static native int sumArr(Sum s);
    
    @JavaScriptBody(args = {}, javacall = true, body = 
        "return @net.java.html.js.tests.Bodies::fourtyTwo()();"
    )
    public static native int staticCallback();

    @JavaScriptBody(args = {}, javacall = true, body = 
        "return function() { return @net.java.html.js.tests.Bodies::fourtyTwo()(); }"
    )
    public static native Object delayCallback();
    
    @JavaScriptBody(args = { "fn" }, body = "return fn();")
    public static native Object invokeFn(Object fn);
    
    static int fourtyTwo() {
        return 42;
    }
    
    @JavaScriptBody(args = { "arr" }, body = 
        """
        var sum = 0;
        for (var i = 0; i < arr.length; i++) {
          sum += arr[i];
        }
        return sum;
        """
    )
    public static native double sumVector(double[] arr);
    
    @JavaScriptBody(args = { "arr" }, body = 
        """
        var sum = 0;
        for (var i = 0; i < arr.length; i++) {
          for (var j = 0; j < arr[i].length; j++) {
            sum += arr[i][j];
          }
        }
        return sum;
        """
    )
    public static native double sumMatrix(double[][] arr);

    static void incCounter(int howMuch, final Object js) {
        for (int i = 0; i < howMuch; i++) {
            asyncCallback(new Runnable() {
                @Override
                public void run() {
                    incrementXAsync(js);
                }
            });
        }
    }
    
    @JavaScriptBody(args = {}, javacall = true, body = 
        """
        var v = { x : 0 };
        @net.java.html.js.tests.Bodies::incCounter(ILjava/lang/Object;)(42, v);
        return v.x;
        """
    )
    static native int incAsync();
    
    @JavaScriptBody(args = { "obj" }, body = 
        """
        var ret = [];
        for (var i in obj) {
          ret.push(i);
          ret.push(obj[i]);
        }
        return ret;
        """
    )
    static native Object[] forIn(Object obj);

    @JavaScriptBody(args = { "max" }, body = 
        """
        var arr = [];
        for (var i = 0; i < max; i++) {
          arr.push(i);
        }
        return arr.length;"""
    )
    static native int gc(double max);

    @JavaScriptBody(args = {}, body = """
                                      var o = {};
                                      return o.x;
                                      """
    )
    static native Object unknown();

    @JavaScriptBody(args = {}, body = """
                                      return new Array(2);
                                      """
    )
    static native Object[] unknownArray();

    @JavaScriptBody(args = { "sum" }, javacall = true, body = """
                                                              var arr = [];
                                                              arr[1] = null;
                                                              arr[2] = 1;
                                                              return sum.@net.java.html.js.tests.Sum::sumNonNull([Ljava/lang/Object;)(arr);
                                                              """
    )
    static native int sumNonNull(Sum sum);

    @JavaScriptBody(args = { "sum", "p" }, javacall = true, body = """
                                                                   var obj = {};
                                                                   obj.x = 1;
                                                                   return sum.@net.java.html.js.tests.Sum::checkNonNull(Ljava/lang/Object;)(obj[p]);
                                                                   """
    )
    static native boolean nonNull(Sum sum, String p);

    @JavaScriptBody(args = {}, javacall = true, body = 
        "return @net.java.html.js.tests.Bodies::problematicString()();"
    )
    public static native String problematicCallback();

    @JavaScriptBody(args = { "sum" }, javacall = true, body = 
        "return sum.@net.java.html.js.tests.Sum::all(ZBSIJFDCLjava/lang/String;)(false, 1, 2, 3, 5, 6, 7, 32, 'TheEND');\n"
    )
    static native String primitiveTypes(Sum sum);

    @JavaScriptBody(args = { "call" }, javacall = true, body = """
        var b = call.@java.util.concurrent.Callable::call()();
        return b ? 'yes' : 'no';
        """
    )
    static native String yesNo(Callable<Boolean> call);

    @JavaScriptBody(args = {"arr", "val"}, body = "return arr[0] === val;")
    public static native boolean isInArray(Object[] arr, Object val);
    
    @JavaScriptBody(args = {}, body = "return globalString;")
    static native String readGlobalString();

    @JavaScriptBody(args = {}, body = "return global2String;")
    static native String readGlobal2String();
    
    static String problematicString() {
        return """
            {
                MyViewModel: {
            //      ViewModel: JavaViewModel,
                }          
            }""";
    }

    @JavaScriptBody(args = { "o", "n" }, body = "return o[n];")
    static native Object get(Object o, String n);

    @JavaScriptBody(args = { "o", "n", "arg" }, body = """
        try {
          if (arg == null) {
            return o[n]();
          } else {
            return o[n](arg);
          }
        } catch (e) {
          return n;
        }
        """
    )
    static native Object invoke(Object o, String n, Object arg);
}
