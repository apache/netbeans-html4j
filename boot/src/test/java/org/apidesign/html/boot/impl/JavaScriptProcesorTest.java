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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
    
    @Test public void generatesCallbacksThatReturnObject() throws Exception {
        Class<?> callbacksForTestPkg = Class.forName("org.apidesign.html.boot.impl.$JsCallbacks$");
        Method m = callbacksForTestPkg.getDeclaredMethod("java_lang_Runnable$run$", Runnable.class);
        assertEquals(m.getReturnType(), Object.class, "All methods always return object");
    }
    
    @Test public void hasInstanceField() throws Exception {
        Class<?> callbacksForTestPkg = Class.forName("org.apidesign.html.boot.impl.$JsCallbacks$");
        Field f = callbacksForTestPkg.getDeclaredField("VM");
        f.setAccessible(true);
        assertTrue(callbacksForTestPkg.isInstance(f.get(null)), "Singleton field VM");
    }
}
