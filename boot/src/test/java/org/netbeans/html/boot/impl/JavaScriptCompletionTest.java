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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Completion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import net.java.html.js.JavaScriptBody;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class JavaScriptCompletionTest {

    @Test
    public void computeRunnableArgument() throws IOException {
        String code = """
            package x.y.z;
            import net.java.html.js.JavaScriptBody;
            class X {
              @JavaScriptBody(args={}, body="")
              private static native void callback(Runnable r);
            }
            """;
        List<Completion> completions = new ArrayList<>();
        Compile c = complete(completions, code, JavaScriptBody.class, "args", "");
        c.assertNoErrors();
        assertEquals(completions.size(), 1, "one suggestion found: " + completions);
        assertEquals(completions.get(0).getValue(), "{ \"r\" }");
    }

    @Test
    public void computeMultipleArguments() throws IOException {
        String code = """
            package x.y.z;
            import net.java.html.js.JavaScriptBody;
            class X {
              @JavaScriptBody(args={}, body="")
              private static native void substring(String s, int from, int length);
            }
            """;
        List<Completion> completions = new ArrayList<>();
        Compile c = complete(completions, code, JavaScriptBody.class, "args", "");
        c.assertNoErrors();
        assertEquals(completions.size(), 1, "one suggestion found: " + completions);
        assertEquals(completions.get(0).getValue(), "{ \"s\", \"from\", \"length\" }");
    }

    @Test
    public void completeCallToInstanceVariable() throws IOException {
        String code = """
            package x.y.z;
            import net.java.html.js.JavaScriptBody;
            import org.netbeans.html.boot.impl.Arithm;
            class X {
              @JavaScriptBody(args={}, body="")
              private static native void arithm(Arithm a, int from, int length);
            }
            """;
        List<Completion> completions = new ArrayList<>();
        complete(completions, code, JavaScriptBody.class, "body", "a.@").assertNoErrors();
        assertEquals(completions.size(), 1, "one suggestion found: " + completions);
        assertEquals(completions.get(0).getValue(), "a.@org.netbeans.html.boot.impl.Arithm::", "Type of instance variable found");

        complete(completions, code, JavaScriptBody.class, "body", "a.@org.netbeans.html.boot.impl.Arithm::").assertNoErrors();
        assertEquals(completions.size(), 2, "two suggestions found: " + completions);
        assertEquals(completions.get(0).getValue(), "a.@org.netbeans.html.boot.impl.Arithm::sumArr([Ljava/lang/Object;)(null)");
        assertEquals(completions.get(1).getValue(), "a.@org.netbeans.html.boot.impl.Arithm::sumTwo(II)(0, 0)");
    }

    @Test
    public void completeCallToStaticMethod() throws IOException {
        String code = """
            package x.y.z;
            import net.java.html.js.JavaScriptBody;
            import org.netbeans.html.boot.impl.Arithm;
            class X {
              @JavaScriptBody(args={}, body="")
              private static native void code();
            }
            """;
        List<Completion> completions = new ArrayList<>();
        complete(completions, code, JavaScriptBody.class, "body", "var c = @org.netbeans.html.boot.impl.Arithm::").assertNoErrors();
        assertEquals(completions.size(), 1, "one suggestion found: " + completions);
        assertEquals(completions.get(0).getValue(), "var c = @org.netbeans.html.boot.impl.Arithm::create()()");

        complete(completions, code, JavaScriptBody.class, "body", "var c = @org.netbeans.html.boot.impl.").assertNoErrors();
        assertNotEquals(completions.size(), 0, "some suggestions found: " + completions);
        OK: {
            for (Completion item : completions) {
                if (item.getValue().equals("var c = @org.netbeans.html.boot.impl.Arithm::")) {
                    break OK;
                }
            }
            fail("Should find class Arithm in the package: " + completions);
        }
    }

    private static Compile complete(List<Completion> completions, String code, Class<?> annotatedBy, String attribute, String userText) throws IOException {
        completions.clear();
        MockProcessor.registerConsumer(annotatedBy, (env, round) -> {
            JavaScriptProcesor jsp = new JavaScriptProcesor();
            jsp.init(env);

            for (Element e : round.getElementsAnnotatedWith(JavaScriptBody.class)) {
                if (e.getKind() != ElementKind.METHOD) {
                    continue;
                }
                ExecutableElement method = (ExecutableElement) e;
                for (AnnotationMirror a : method.getAnnotationMirrors() ) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> value : a.getElementValues().entrySet()) {
                        final ExecutableElement execElem = value.getKey();
                        if (execElem.getSimpleName().contentEquals(attribute)) {
                            assertTrue(completions.isEmpty(), "Already filled with something: " + completions);
                            for (Completion item : jsp.getCompletions(e, a, execElem, userText)) {
                                completions.add(item);
                            }
                        }
                    }
                }
            }
        });
        Compile c = Compile.create("", code);
        Collections.sort(completions, (a, b) -> a.getValue().compareTo(b.getValue()));
        return c;
    }
}
