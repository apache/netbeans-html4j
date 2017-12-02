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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class JavaScriptProcesorTest {
    
    @Test public void detectCallbackToNonExistingClass() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Runable::run()();\"\n" // typo
            + "  )\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("java.lang.Runable"); // typo
    }

    @Test public void detectCallbackToNonExistingMethod() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Runnable::cancel()();\"\n"
            + "  )\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("method cancel");
    }

    @Test public void detectCallbackToNonExistingParams() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Runnable::run(I)(10);\"\n"
            + "  )\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("wrong parameters: (I)");
    }

    @Test public void objectTypeParamsAreOK() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Object::equals(Ljava/lang/Object;)(null);\"\n"
            + "  )\n"
            + "  private static native void testEqual(Object r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertNoErrors();
    }
    
    @Test public void primitiveArrayGeneratesAnError() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, body =\n"
            + "    \"return [ 1, 2 ];\"\n"
            + "  )\n"
            + "  private static native double[] returnPrimitive(Object r);\n"
            + "}\n";

        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("Use Object[]");
    }

    @Test public void nonObjectArrayGeneratesAnError() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, body =\n"
            + "    \"return [ 1, 2 ];\"\n"
            + "  )\n"
            + "  private static native Double[] returnPrimitive(Object r);\n"
            + "}\n";

        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("Use Object[]");
    }

    @Test public void primitiveArrayCallbackGeneratesAnError() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall = true, body =\n"
            + "    \"return @x.y.z.X::acceptDouble([D)([ 1, 2 ]);\"\n"
            + "  )\n"
            + "  private static native Object[] returnPrimitive(Object r);\n"
            + "  static double[] acceptDouble(double[] arr) {\n"
            + "    return arr;\n"
            + "  }\n"
            + "}\n";

        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("Use Object[]");
    }

    @Test public void nonObjectArrayCallbackGeneratesAnError() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall = true, body =\n"
            + "    \"return @x.y.z.X::acceptDouble([Ljava/lang/Double;)([ 1, 2 ]);\"\n"
            + "  )\n"
            + "  private static native Object[] returnPrimitive(Object r);\n"
            + "  static Double[] acceptDouble(Double[] arr) {\n"
            + "    return arr;\n"
            + "  }\n"
            + "}\n";

        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("Use Object[]");
    }

    @Test public void misorderNotified() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\", \"a\", \"b\"}, body =\"\"\n"
            + "  )\n"
            + "  private static native void testEqual(Object p, String q, int r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        List<Diagnostic<? extends JavaFileObject>> warnings = c.getDiagnostics(Diagnostic.Kind.WARNING);
        assertTrue(warnings.size() >= 1, "There are warnings: " + warnings);
        for (Diagnostic<? extends JavaFileObject> w : warnings) {
            if (w.getMessage(Locale.US).contains("Actual method parameter names and args")) {
                return;
            }
        }
        fail("Expecting order warning: " + warnings);
    }

    @Test public void needJavaScriptBodyToUseResource() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptResource;\n"
            + "@JavaScriptResource(\"x.html\")\n"
            + "class X {\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("needs @JavaScriptBody");
    }
    
    @Test public void generatesCallbacksThatReturnObject() throws Exception {
        Class<?> callbacksForTestPkg = Class.forName("org.netbeans.html.boot.impl.$JsCallbacks$");
        Method m = callbacksForTestPkg.getDeclaredMethod("java_lang_Runnable$run$", Runnable.class);
        assertEquals(m.getReturnType(), java.lang.Object.class, "All methods always return object");
    }
    
    @Test public void hasInstanceField() throws Exception {
        Class<?> callbacksForTestPkg = Class.forName("org.netbeans.html.boot.impl.$JsCallbacks$");
        Field f = callbacksForTestPkg.getDeclaredField("VM");
        f.setAccessible(true);
        assertTrue(callbacksForTestPkg.isInstance(f.get(null)), "Singleton field VM");
    }
}
