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
package net.java.html.json;

import java.io.IOException;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/** Verify errors emitted by the processor.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ModelProcessorTest {
    @Test public void verifyWrongType() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop\", type=Runnable.class)\n"
            + "})\n"
            + "class X {\n"
            + "}\n";
        
        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("Runnable")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about Runnable:" + msgs);
        }
    }
    
    @Test public void warnOnNonStatic() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop\", type=int.class)\n"
            + "})\n"
            + "class X {\n"
            + "    @ComputedProperty int y(int prop) {\n"
            + "        return prop;\n"
            + "    }\n"
            + "}\n";
        
        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("y has to be static")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about non-static method:" + msgs);
        }
    }
    
    @Test public void computedCantReturnVoid() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop\", type=int.class)\n"
            + "})\n"
            + "class X {\n"
            + "    @ComputedProperty static void y(int prop) {\n"
            + "    }\n"
            + "}\n";
        
        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("y cannot return void")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about non-static method:" + msgs);
        }
    }
    
    @Test public void computedCantReturnRunnable() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop\", type=int.class)\n"
            + "})\n"
            + "class X {\n"
            + "    @ComputedProperty static Runnable y(int prop) {\n"
            + "       return null;\n"
            + "    }\n"
            + "}\n";
        
        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("y cannot return java.lang.Runnable")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about non-static method:" + msgs);
        }
    }
    
    @Test public void canWeCompileWithJDK1_5SourceLevel() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop\", type=long.class)\n"
            + "})\n"
            + "class X {\n"
            + "  @ComputedProperty static double derived(long prop) { return prop; }"
            + "}\n";
        
        Compile c = Compile.create(html, code, "1.5");
        assertTrue(c.getErrors().isEmpty(), "No errors: " + c.getErrors());
    }
    
    @Test public void putNeedsDataArgument() throws Exception {
        needsAnArg("PUT");
    }

    @Test public void postNeedsDataArgument() throws Exception {
        needsAnArg("POST");
    }
    
    private void needsAnArg(String method) throws Exception {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.OnReceive;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop\", type=long.class)\n"
            + "})\n"
            + "class X {\n"
            + "  @Model(className=\"PQ\", properties={})\n"
            + "  class PImpl {\n"
            + "  }\n"
            + "  @OnReceive(method=\"" + method + "\", url=\"whereever\")\n"
            + "  static void obtained(XModel m, PQ p) { }\n"
            + "}\n";
        
        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        for (Diagnostic<? extends JavaFileObject> diagnostic : c.getErrors()) {
            String msg = diagnostic.getMessage(Locale.ENGLISH);
            if (msg.contains("specify a data()")) {
                return;
            }
        }
        fail("Needs an error message about missing data():\n" + c.getErrors());
        
    }
    
    
    @Test public void jsonNeedsToUseGet () throws Exception {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.OnReceive;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop\", type=long.class)\n"
            + "})\n"
            + "class X {\n"
            + "  @Model(className=\"PQ\", properties={})\n"
            + "  class PImpl {\n"
            + "  }\n"
            + "  @OnReceive(method=\"POST\", jsonp=\"callback\", url=\"whereever\")\n"
            + "  static void obtained(XModel m, PQ p) { }\n"
            + "}\n";
        
        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        for (Diagnostic<? extends JavaFileObject> diagnostic : c.getErrors()) {
            String msg = diagnostic.getMessage(Locale.ENGLISH);
            if (msg.contains("JSONP works only with GET")) {
                return;
            }
        }
        fail("Needs an error message about wrong method:\n" + c.getErrors());
        
    }
}
