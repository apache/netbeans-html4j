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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
          "var a = 0;\n"
        + "for (var i = 0; i < arr.length; i++) {\n"
        + "  a = at.@org.netbeans.html.boot.impl.Arithm::sumTwo(II)(a, arr[i]);\n"
        + "}\n"
        + "return a;"
    )
    private static native int sumArr(Arithm at, int... arr);
    
    public static int sumArr(int... arr) {
        return sumArr(new Arithm(), arr);
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
    
    @JavaScriptBody(args = { "useA", "useB", "a", "b" }, body = "var l = 0;"
        + "if (useA) l += a;\n"
        + "if (useB) l += b;\n"
        + "return l;\n"
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
    public static native double[] both(double a, double b);
    
    enum Enm {
        A, B;
    }
}

