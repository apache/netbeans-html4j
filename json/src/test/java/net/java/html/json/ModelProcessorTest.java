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
package net.java.html.json;

import java.io.IOException;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.Test;

/** Verify errors emitted by the processor.
 *
 * @author Jaroslav Tulach
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

    @Test public void verifyWrongTypeInInnerClass() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "class X {\n"
            + "  @Model(className=\"XModel\", properties={\n"
            + "    @Property(name=\"prop\", type=Runnable.class)\n"
            + "  })\n"
            + "  static class Inner {\n"
            + "  }\n"
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

    @Test public void warnOnDuplicated() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop1\", type=int.class),\n"
            + "  @Property(name=\"prop2\", type=int.class)\n"
            + "})\n"
            + "class X {\n"
            + "    @ComputedProperty static int y(int prop1) {\n"
            + "        return prop1;\n"
            + "    }\n"
            + "    @ComputedProperty static int y(int prop1, int prop2) {\n"
            + "        return prop2;\n"
            + "    }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("Cannot have the property y defined twice")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning duplicated property:" + msgs);
        }
    }

    @Test public void warnOnDuplicatedWithNormalProp() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", properties={\n"
            + "  @Property(name=\"prop1\", type=int.class),\n"
            + "  @Property(name=\"prop2\", type=int.class)\n"
            + "})\n"
            + "class X {\n"
            + "    @ComputedProperty static int prop2(int prop1) {\n"
            + "        return prop1;\n"
            + "    }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("Cannot have the property prop2 defined twice")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning duplicated property:" + msgs);
        }
    }

    @Test public void tooManyProperties() throws IOException {
        manyProperties(255, false, 0);
    }

    @Test public void tooManyArrayPropertiesIsOK() throws IOException {
        manyProperties(0, true, 300);
    }

    @Test public void justEnoughProperties() throws IOException {
        manyProperties(254, true, 0);
    }

    @Test public void justEnoughPropertiesWithArrayOne() throws IOException {
        manyProperties(253, true, 300);
    }

    @Test public void justEnoughPropertiesButOneArrayOne() throws IOException {
        manyProperties(254, false, 300);
    }

    private void manyProperties(
        int cnt, boolean constructorWithParams, int arrayCnt
    ) throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        StringBuilder code = new StringBuilder();
        code.append("package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "@Model(className=\"XModel\", properties={\n"
        );
        for (int i = 1; i <= cnt; i++) {
            code.append("  @Property(name=\"prop").append(i).append("\", ");
            code.append("type=int.class),\n");
        }
        for (int i = 1; i <= arrayCnt; i++) {
            code.append("  @Property(name=\"array").append(i).append("\", ");
            code.append("array=true, ");
            code.append("type=int.class),\n");
        }
        code.append(""
            + "})\n"
            + "class X {\n"
            + "    static {\n"
            + "      new XModel();\n"
            + "      new XModel("
        );
        if (constructorWithParams) {
            code.append("0");
            for (int i = 1; i < cnt; i++) {
                code.append(",\n").append(i);
            }
        }
        code.append(");\n"
            + "    }\n"
            + "}\n"
        );

        Compile c = Compile.create(html, code.toString());
        assertTrue(c.getErrors().isEmpty(), "Compiles OK: " + c.getErrors());
    }

    @Test public void writeableComputedPropertyMissingWrite() throws IOException {
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
            + "    static @ComputedProperty(write=\"setY\") int y(int prop) {\n"
            + "        return prop;\n"
            + "    }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("Cannot find setY")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about non-static method:" + msgs);
        }
    }

    @Test public void writeableComputedPropertyWrongWriteType() throws IOException {
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
            + "    static @ComputedProperty(write=\"setY\") int y(int prop) {\n"
            + "        return prop;\n"
            + "    }\n"
            + "    static void setY(XModel model, String prop) {\n"
            + "    }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("Write method first argument needs to be XModel and second int or Object")) {
                ok = true;
            }
            msgs.append("\n").append(msg);
        }
        if (!ok) {
            fail("Should contain warning about non-static method:" + msgs);
        }
    }

    @Test public void writeableComputedPropertyReturnsVoid() throws IOException {
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
            + "    static @ComputedProperty(write=\"setY\") int y(int prop) {\n"
            + "        return prop;\n"
            + "    }\n"
            + "    static Number setY(XModel model, int prop) {\n"
            + "    }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        boolean ok = false;
        StringBuilder msgs = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (msg.contains("Write method has to return void")) {
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
        try {
            Class.forName("java.lang.Module");
            throw new SkipException("Cannot use 1.5 source level on new JDKs");
        } catch (ClassNotFoundException ex) {
            // OK, go on
        }

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
    
    @Test public void instanceNeedsDefaultConstructor() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", instance=true, properties={\n"
            + "  @Property(name=\"prop\", type=long.class)\n"
            + "})\n"
            + "class X {\n"
            + "  X(int x) {}\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        c.assertError("Needs non-private default constructor when instance=true");
    }
    
    @Test public void instanceNeedsNonPrivateConstructor() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", instance=true, properties={\n"
            + "  @Property(name=\"prop\", type=long.class)\n"
            + "})\n"
            + "class X {\n"
            + "  private X() {}\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        c.assertError("Needs non-private default constructor when instance=true");
    }

    @Test public void instanceNoConstructorIsOK() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import net.java.html.json.Model;\n"
            + "import net.java.html.json.Property;\n"
            + "import net.java.html.json.ComputedProperty;\n"
            + "@Model(className=\"XModel\", instance=true, properties={\n"
            + "  @Property(name=\"prop\", type=long.class)\n"
            + "})\n"
            + "class X {\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        c.assertNoErrors();
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

    @Test public void noHeadersForWebSockets() throws Exception {
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
            + "  @OnReceive(method=\"WebSocket\", data = PQ.class, headers=\"SomeHeader: {some}\", url=\"whereever\")\n"
            + "  static void obtained(XModel m, PQ p) { }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        for (Diagnostic<? extends JavaFileObject> diagnostic : c.getErrors()) {
            String msg = diagnostic.getMessage(Locale.ENGLISH);
            if (msg.contains("WebSocket spec does not support headers")) {
                return;
            }
        }
        fail("Needs an error message about headers:\n" + c.getErrors());

    }

    @Test public void webSocketsWithoutDataIsError() throws Exception {
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
            + "  @OnReceive(method=\"WebSocket\", url=\"whereever\")\n"
            + "  static void obtained(XModel m, PQ p) { }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        for (Diagnostic<? extends JavaFileObject> diagnostic : c.getErrors()) {
            String msg = diagnostic.getMessage(Locale.ENGLISH);
            if (msg.contains("eeds to specify a data()")) {
                return;
            }
        }
        fail("Needs data attribute :\n" + c.getErrors());
    }

    @Test public void noNewLinesInHeaderLines() throws Exception {
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
            + "  @OnReceive(headers=\"SomeHeader\\n: {some}\", url=\"whereever\")\n"
            + "  static void obtained(XModel m, PQ p) { }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        for (Diagnostic<? extends JavaFileObject> diagnostic : c.getErrors()) {
            String msg = diagnostic.getMessage(Locale.ENGLISH);
            if (msg.contains("Header line cannot contain line separator")) {
                return;
            }
        }
        fail("Needs an error message about headers:\n" + c.getErrors());

    }

    @Test public void noReturnInHeaderLines() throws Exception {
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
            + "  @OnReceive(headers=\"Some\\rHeader: {some}\", url=\"whereever\")\n"
            + "  static void obtained(XModel m, PQ p) { }\n"
            + "}\n";

        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        for (Diagnostic<? extends JavaFileObject> diagnostic : c.getErrors()) {
            String msg = diagnostic.getMessage(Locale.ENGLISH);
            if (msg.contains("Header line cannot contain line separator")) {
                return;
            }
        }
        fail("Needs an error message about headers:\n" + c.getErrors());

    }

    @Test public void onErrorHasToExist() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class)\n"
            + "})\n"
            + "class UseOnReceive {\n"
            + "  @net.java.html.json.OnReceive(url=\"http://nowhere.com\", onError=\"doesNotExist\")\n"
            + "  static void onMessage(MyModel model, String value) {\n"
            + "  }\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("not find doesNotExist");
    }

    @Test public void usingListIsOK() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class)\n"
            + "})\n"
            + "class UseOnReceive {\n"
            + "  @net.java.html.json.OnReceive(url=\"http://nowhere.com\")\n"
            + "  static void onMessage(MyModel model, java.util.List<MyData> value) {\n"
            + "  }\n"
            + "\n"
            + "  @net.java.html.json.Model(className=\"MyData\", properties={\n"
            + "  })\n"
            + "  static class MyDataModel {\n"
            + "  }\n"
            + "}\n"
        );
        res.assertNoErrors();
    }

    @Test public void functionAndPropertyCollide() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class)\n"
            + "})\n"
            + "class Collision {\n"
            + "  @net.java.html.json.Function\n"
            + "  static void x(MyModel model, String value) {\n"
            + "  }\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("cannot have the name");
    }

    @Test public void twoPropertiesCollide() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class),\n"
            + "  @net.java.html.json.Property(name=\"x\", type=int.class)\n"
            + "})\n"
            + "class Collision {\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("Cannot have the property");
    }

    @Test public void propertyAndComputedOneCollide() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class),\n"
            + "})\n"
            + "class Collision {\n"
            + "  @net.java.html.json.ComputedProperty static int x(String x) {\n"
            + "    return x.length();\n"
            + "  }\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("Cannot have the property");
    }

    @Test public void onWebSocketJustTwoArgs() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class)\n"
            + "})\n"
            + "class UseOnReceive {\n"
            + "  @net.java.html.json.OnReceive(url=\"http://nowhere.com\", method=\"WebSocket\", data=String.class)\n"
            + "  static void onMessage(MyModel model, String value, int arg) {\n"
            + "  }\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("only have two arg");
    }

    @Test public void onErrorWouldHaveToBeStatic() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class)\n"
            + "})\n"
            + "class UseOnReceive {\n"
            + "  @net.java.html.json.OnReceive(url=\"http://nowhere.com\", onError=\"notStatic\")\n"
            + "  static void onMessage(MyModel model, String value) {\n"
            + "  }\n"
            + "  void notStatic(Exception e) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("have to be static");
    }

    @Test public void onErrorMustAcceptExceptionArgument() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "@net.java.html.json.Model(className=\"MyModel\", properties= {\n"
            + "  @net.java.html.json.Property(name=\"x\", type=String.class)\n"
            + "})\n"
            + "class UseOnReceive {\n"
            + "  @net.java.html.json.OnReceive(url=\"http://nowhere.com\", onError=\"subclass\")\n"
            + "  static void onMessage(MyModel model, String value) {\n"
            + "  }\n"
            + "  static void subclass(java.io.IOException e) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("Error method first argument needs to be MyModel and second Exception");
    }
}
