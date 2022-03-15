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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = Processor.class)
public final class JavaScriptProcesor extends AbstractProcessor {
    private final Map<String,Map<String,ExecutableElement>> javacalls = new HashMap<>();
    private final Map<String,Set<TypeElement>> bodies = new HashMap<>();

    public JavaScriptProcesor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(JavaScriptBody.class.getName());
        set.add(JavaScriptResource.class.getName());
        set.add(JavaScriptResource.Group.class.getName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Messager msg = processingEnv.getMessager();
        for (Element e : roundEnv.getElementsAnnotatedWith(JavaScriptBody.class)) {
            if (e.getKind() != ElementKind.METHOD && e.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement)e;
            List<? extends VariableElement> params = ee.getParameters();

            JavaScriptBody jsb = e.getAnnotation(JavaScriptBody.class);
            if (jsb == null) {
                continue;
            } else {
                Set<TypeElement> classes = this.bodies.get(findPkg(e));
                if (classes == null) {
                    classes = new HashSet<>();
                    bodies.put(findPkg(e), classes);
                }
                Element t = e.getEnclosingElement();
                while (!t.getKind().isClass() && !t.getKind().isInterface()) {
                    t = t.getEnclosingElement();
                }
                classes.add((TypeElement)t);
            }
            String[] arr = jsb.args();
            if (params.size() != arr.length) {
                msg.printMessage(Diagnostic.Kind.ERROR, "Number of args arguments does not match real arguments!", e);
            }
            for (int i = 0; i < arr.length; i++) {
                if (!params.get(i).getSimpleName().toString().equals(arr[i])) {
                    msg.printMessage(Diagnostic.Kind.WARNING, "Actual method parameter names and args ones " + Arrays.toString(arr) + " differ", e);
                }
            }
            if (!jsb.wait4js() && ee.getReturnType().getKind() != TypeKind.VOID) {
                msg.printMessage(Diagnostic.Kind.ERROR, "Methods that don't wait for JavaScript to finish must return void!", e);
            }
            if (!jsb.javacall() && jsb.body().contains(".@")) {
                msg.printMessage(Diagnostic.Kind.WARNING, "Usage of .@ usually requires javacall=true", e);
            }
            if (ee.getReturnType().getKind() == TypeKind.ARRAY) {
                ArrayType at = (ArrayType) ee.getReturnType();
                TypeMirror objectType = processingEnv.getElementUtils().getTypeElement("java.lang.Object").asType();
                    final TypeMirror componentType = at.getComponentType();
                if (!processingEnv.getTypeUtils().isSameType(objectType, componentType)) {
                    wrongArrayError(componentType, e);
                }
            }
            if (jsb.javacall()) {
                JsCallback verify = new VerifyCallback(e);
                try {
                    verify.parse(jsb.body(), !jsb.wait4java());
                } catch (IllegalStateException ex) {
                    msg.printMessage(Diagnostic.Kind.ERROR, ex.getLocalizedMessage(), e);
                }
            }
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(JavaScriptResource.class)) {
            JavaScriptResource r = e.getAnnotation(JavaScriptResource.class);
            if (r == null) {
                continue;
            }
            checkJavaScriptBody(r, e, msg);
        }

        for (Element e : roundEnv.getElementsAnnotatedWith(JavaScriptResource.Group.class)) {
            JavaScriptResource.Group g = e.getAnnotation(JavaScriptResource.Group.class);
            if (g == null) {
                continue;
            }
            for (JavaScriptResource r : g.value()) {
                checkJavaScriptBody(r, e, msg);
            }
        }

        if (roundEnv.processingOver()) {
            generateCallbackClass(javacalls);
            generateJavaScriptBodyList(bodies);
            javacalls.clear();
        }
        return true;
    }

    private void checkJavaScriptBody(JavaScriptResource r, Element e, final Messager msg) {
        final String res;
        if (r.value().startsWith("/")) {
            res = r.value().substring(1);
        } else {
            res = findPkg(e).replace('.', '/') + "/" + r.value();
        }

        verifyResource(res, msg, e);

        boolean found = false;
        for (Element mthod : e.getEnclosedElements()) {
            if (mthod.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (mthod.getAnnotation(JavaScriptBody.class) != null) {
                found = true;
                break;
            }
        }
        if (!found) {
            msg.printMessage(Diagnostic.Kind.ERROR, "At least one method needs @JavaScriptBody annotation. "
                    + "Otherwise it is not guaranteed the resource will ever be loaded,", e
            );
        }
    }

    private void verifyResource(final String res, final Messager msg, Element e) {
        Diagnostic.Kind[] missingReferenceError = { Diagnostic.Kind.ERROR };
        IOException[] ex = { null };
        if (verifyResourceAtPath(res, StandardLocation.SOURCE_PATH, ex, missingReferenceError)) {
            return;
        }
        if (verifyResourceAtPath(res, StandardLocation.CLASS_OUTPUT, ex, missingReferenceError)) {
            return;
        }
        if (verifyResourceAtPath(res, StandardLocation.CLASS_PATH, ex, missingReferenceError)) {
            return;
        }
        if (ex[0] != null) {
            ex[0].printStackTrace();
        }
        msg.printMessage(missingReferenceError[0], "Cannot find resource " + res, e);
    }

    private boolean verifyResourceAtPath(final String res, StandardLocation location, IOException[] exArr, Diagnostic.Kind[] missingReferenceError)  {
        try {
            FileObject os = processingEnv.getFiler().getResource(location, "", res);
            os.openInputStream().close();
            return true;
        } catch (IOException ex) {
            exArr[0] = ex;
            return false;
        } catch (Exception | Error err) {
            missingReferenceError[0] = Diagnostic.Kind.MANDATORY_WARNING;
            return false;
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element e,
        AnnotationMirror annotation, ExecutableElement member, String userText
    ) {
        if (e.getKind() == ElementKind.METHOD && member.getSimpleName().contentEquals("args")) { // NOI18N
            return argsCompletion(e);
        }
        if (e.getKind() == ElementKind.METHOD && member.getSimpleName().contentEquals("body")) { // NOI18N
            return bodyCompletion(e, userText);
        }
        return Collections.emptyList();
    }

    private Iterable<? extends Completion> argsCompletion(Element e) {
        StringBuilder sb = new StringBuilder();
        ExecutableElement ee = (ExecutableElement) e;
        String sep = "";
        sb.append("{ ");
        for (VariableElement ve : ee.getParameters()) {
            sb.append(sep).append('"').append(ve.getSimpleName())
                    .append('"');
            sep = ", ";
        }
        sb.append(" }");
        return Collections.nCopies(1, Completions.of(sb.toString()));
    }

    private Iterable<? extends Completion> bodyCompletion(Element e, String userText) {
        ExecutableElement ee = (ExecutableElement) e;
        String identifier = endsWithIdentifier(userText);
        int preIdentifierAt = userText.length() - identifier.length() - 1;
        if (preIdentifierAt >= 0) {
            char preId = userText.charAt(preIdentifierAt);
            if (preId == '.' || preId == '@') {
                int lastAt = userText.lastIndexOf('@', preIdentifierAt + 1);
                String maybePackageName = preIdentifierAt > lastAt ? userText.substring(lastAt + 1, preIdentifierAt) : "";
                if (!maybePackageName.isEmpty()) {
                    PackageElement maybePackage = processingEnv.getElementUtils().getPackageElement(maybePackageName);
                    if (maybePackage != null) {
                        List<Completion> offer = new ArrayList<>();
                        for (Element ch : maybePackage.getEnclosedElements()) {
                            final String elemName = ch.getSimpleName().toString();
                            if (elemName.startsWith(identifier)) {
                                final String userCompl = userText.substring(0, lastAt + 1) + maybePackageName + "." + elemName + "::"; // NOI18N
                                offer.add(Completions.of(userCompl));
                            }
                        }
                        return offer;
                    }
                } else {
                    String instanceName = null;
                    if (lastAt > 0 && userText.charAt(lastAt - 1) == '.') {
                        instanceName = endsWithIdentifier(userText, lastAt - 1);
                        if (instanceName.isEmpty()) {
                            instanceName = null;
                        }
                    }
                    if (instanceName != null) {
                        for (VariableElement p : ee.getParameters()) {
                            if (p.getSimpleName().contentEquals(instanceName)) {
                                Element et = processingEnv.getTypeUtils().asElement(p.asType());
                                if (et.getKind().isClass() || et.getKind().isInterface()) {
                                    String fqn = processingEnv.getElementUtils().getBinaryName((TypeElement) et).toString();
                                    String type = userText.substring(0, lastAt + 1) + fqn + "::";
                                    return Collections.nCopies(1, Completions.of(type));
                                }
                            }
                        }
                    }
                }
            } else if (preId == ':') {
                int lastAt = userText.lastIndexOf('@', preIdentifierAt + 1);
                String maybeClassName = userText.substring(lastAt + 1, preIdentifierAt - 1);
                TypeElement maybeClass = processingEnv.getElementUtils().getTypeElement(maybeClassName);
                String instanceName = null;
                if (lastAt > 0 && userText.charAt(lastAt - 1) == '.') {
                    instanceName = endsWithIdentifier(userText, lastAt - 1);
                    if (instanceName.isEmpty()) {
                        instanceName = null;
                    }
                }
                if (maybeClass != null) {
                    List<Completion> offer = new ArrayList<>();
                    for (Element ch : maybeClass.getEnclosedElements()) {
                        if (ch.getKind() != ElementKind.METHOD) {
                            continue;
                        }
                        final String elemName = ch.getSimpleName().toString();
                        if (elemName.startsWith(identifier)) {
                            ExecutableElement m = (ExecutableElement) ch;
                            boolean isStatic = m.getModifiers().contains(Modifier.STATIC);
                            if (instanceName != null) {
                                if (isStatic) {
                                    continue;
                                }
                            } else {
                                if (!isStatic) {
                                    continue;
                                }
                            }

                            StringBuilder invokeMethod = new StringBuilder().
                                    append(userText.substring(0, lastAt + 1)).
                                    append(maybeClassName).
                                    append("::").
                                    append(elemName).
                                    append(findParamTypes(m)).
                                    append("(");
                            String sep = "";
                            for (VariableElement p : m.getParameters()) {
                                invokeMethod.append(sep);
                                if (p.asType().getKind().isPrimitive()) {
                                    invokeMethod.append("0");
                                } else {
                                    invokeMethod.append("null");
                                }
                                sep = ", ";
                            }
                            invokeMethod.append(")");
                            offer.add(Completions.of(invokeMethod.toString()));
                        }
                    }
                    return offer;
                }
            }
        }
        return Collections.emptyList();
    }

    final void wrongArrayError(TypeMirror paramType, Element method) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Don't use " + paramType + " array. Use Object[].", method);
    }

    private static String endsWithIdentifier(String text) {
        return endsWithIdentifier(text, text.length());
    }

    private static String endsWithIdentifier(String text, int max) {
        int at = max;
        while (--at >= 0) {
            char ch = text.charAt(at);
            if (!Character.isJavaIdentifierPart(ch)) {
                break;
            }
        }
        return text.substring(at + 1, max);
    }

    private class VerifyCallback extends JsCallback {
        private final Element e;
        public VerifyCallback(Element e) {
            this.e = e;
        }

        @Override
        protected CharSequence callMethod(String ident, boolean promise, String fqn, String method, String params) {
            final TypeElement type = processingEnv.getElementUtils().getTypeElement(fqn);
            if (type == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Callback to non-existing class " + fqn, e
                );
                return "";
            }
            ExecutableElement found = null;
            String paramTypes = null;
            StringBuilder foundParams = new StringBuilder();
            for (Element m : type.getEnclosedElements()) {
                if (m.getKind() != ElementKind.METHOD) {
                    continue;
                }
                if (m.getSimpleName().contentEquals(method)) {
                    paramTypes = findParamTypes((ExecutableElement)m);
                    if (paramTypes.equals(params)) {
                        found = (ExecutableElement) m;
                        break;
                    }
                    foundParams.append(paramTypes).append("\n");
                }
            }
            if (found == null) {
                if (foundParams.length() == 0) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Callback to class " + fqn + " with unknown method " + method, e
                    );
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Callback to " + fqn + "." + method + " with wrong parameters: " +
                        params + ". Only known parameters are " + foundParams, e
                    );
                }
            } else {
                Map<String,ExecutableElement> mangledOnes = javacalls.get(findPkg(e));
                if (mangledOnes == null) {
                    mangledOnes = new TreeMap<>();
                    javacalls.put(findPkg(e), mangledOnes);
                }
                String mangled = JsCallback.mangle(fqn, method, paramTypes);
                mangledOnes.put(mangled, found);
            }
            return "";
        }
    }

    private static void dumpElems(StringBuilder sb, Element e, char after) {
        if (e == null) {
            return;
        }
        if (e.getKind() == ElementKind.PACKAGE) {
            PackageElement pe = (PackageElement) e;
            sb.append(pe.getQualifiedName().toString().replace('.', '/')).append('/');
            return;
        }
        Element p = e.getEnclosingElement();
        dumpElems(sb, p, '$');
        sb.append(e.getSimpleName());
        sb.append(after);
    }

    private void generateJavaScriptBodyList(Map<String,Set<TypeElement>> bodies) {
        if (bodies.isEmpty()) {
            return;
        }
        try {
            FileObject all = processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT, "", "META-INF/net.java.html.js.classes"
            );
            try (PrintWriter wAll = new PrintWriter(new OutputStreamWriter(
                all.openOutputStream(), "UTF-8"
            ))) {
                for (Map.Entry<String, Set<TypeElement>> entry : bodies.entrySet()) {
                    String pkg = entry.getKey();
                    Set<TypeElement> classes = entry.getValue();

                    FileObject out = processingEnv.getFiler().createResource(
                            StandardLocation.CLASS_OUTPUT, pkg, "net.java.html.js.classes",
                            classes.iterator().next()
                    );
                    try (OutputStream os = out.openOutputStream()) {
                        PrintWriter w = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
                        for (TypeElement type : classes) {
                            final Name bn = processingEnv.getElementUtils().getBinaryName(type);
                            w.println(bn);
                            wAll.println(bn);
                        }
                        w.flush();
                        w.close();
                    } catch (IOException x) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write to " + entry.getKey() + ": " + x.toString());
                    }
                }
            }
        } catch (IOException x) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write to " + "META-INF/net.java.html.js.classes: " + x.toString());
        }
    }

    private void generateCallbackClass(Map<String,Map<String, ExecutableElement>> process) {
        for (Map.Entry<String, Map<String, ExecutableElement>> pkgEn : process.entrySet()) {
            String pkgName = pkgEn.getKey();
            Map<String, ExecutableElement> map = pkgEn.getValue();
            StringBuilder source = new StringBuilder();
            source.append("package ").append(pkgName).append(";\n");
            source.append("@java.lang.SuppressWarnings(\"all\")\n");
            source.append("public final class $JsCallbacks$ {\n");
            source.append("  static final $JsCallbacks$ VM = new $JsCallbacks$(null);\n");
            source.append("  private final org.netbeans.html.boot.spi.Fn.Ref<?> ref;\n");
            source.append("  private $JsCallbacks$ next;\n");
            source.append("  private $JsCallbacks$(org.netbeans.html.boot.spi.Fn.Presenter p) {\n");
            source.append("    this.ref = org.netbeans.html.boot.spi.Fn.ref(p);\n");
            source.append("  }\n");
            source.append("  synchronized final $JsCallbacks$ current() {\n");
            source.append("    org.netbeans.html.boot.spi.Fn.Presenter now = org.netbeans.html.boot.spi.Fn.activePresenter();\n");
            source.append("    $JsCallbacks$ thiz = this;\n");
            source.append("    $JsCallbacks$ prev = null;\n");
            source.append("    for (;;) {\n");
            source.append("      if (thiz.ref != null) {\n");
            source.append("        org.netbeans.html.boot.spi.Fn.Presenter thizPresenter = thiz.ref.presenter();\n");
            source.append("        if (thizPresenter == null) {\n");
            source.append("          if (prev != null) {\n");
            source.append("            prev.next = thiz.next;\n");
            source.append("          }\n");
            source.append("        } else {\n");
            source.append("          if (thizPresenter == now) return thiz;\n");
            source.append("        }\n");
            source.append("      }\n");
            source.append("      if (thiz.next == null) {\n");
            source.append("        return thiz.next = new $JsCallbacks$(now);\n");
            source.append("      }\n");
            source.append("      prev = thiz;\n");
            source.append("      thiz = thiz.next;\n");
            source.append("    }\n");
            source.append("  }\n");
            source.append("  private static <T> T cast(org.netbeans.html.boot.spi.Fn.Presenter p, java.lang.Class<T> clazz, java.lang.Object obj) {\n");
            source.append("    if (p instanceof org.netbeans.html.boot.spi.Fn.FromJavaScript) {\n");
            source.append("      obj = ((org.netbeans.html.boot.spi.Fn.FromJavaScript)p).toJava(obj);\n");
            source.append("    }\n");
            source.append("    return clazz.cast(obj);\n");
            source.append("  }\n");
            source.append("  private static java.lang.Object toJs(org.netbeans.html.boot.spi.Fn.Presenter p, java.lang.Object obj) {\n");
            source.append("    if (p instanceof org.netbeans.html.boot.spi.Fn.ToJavaScript) {\n");
            source.append("      obj = ((org.netbeans.html.boot.spi.Fn.ToJavaScript)p).toJavaScript(obj);\n");
            source.append("    }\n");
            source.append("    return obj;\n");
            source.append("  }\n");
            for (Map.Entry<String, ExecutableElement> entry : map.entrySet()) {
                final String mangled = entry.getKey();
                final ExecutableElement m = entry.getValue();
                generateMethod(false, false, m, source, mangled);
                generateMethod(true, false, m, source, "raw$" + mangled);
                generateMethod(true, true, m, source, "promise$" + mangled);
            }
            source.append("}\n");
            final String srcName = pkgName + ".$JsCallbacks$";
            try {
                try (Writer w = processingEnv.getFiler().createSourceFile(srcName,
                    map.values().toArray(new Element[map.size()])
                ).openWriter()) {
                    w.write(source.toString());
                }
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Can't write " + srcName + ": " + ex.getMessage()
                );
            }
        }
    }

    private void generateMethod(boolean selfObj, boolean promise, final ExecutableElement m, StringBuilder source, final String mangled) {
        final boolean isStatic = m.getModifiers().contains(Modifier.STATIC);
        if (isStatic && selfObj) {
            return;
        }
        final TypeElement selfType = (TypeElement)m.getEnclosingElement();
        Types tu = processingEnv.getTypeUtils();

        source.append("\n  public java.lang.Object ")
                .append(mangled)
                .append("(");

        {
            String sep = "";
            if (!isStatic) {
                source.append("final ");
                if (selfObj) {
                    source.append("java.lang.Object self");
                } else {
                    source.append(selfType.getQualifiedName());
                    source.append(" self");
                }
                sep = ", ";
            }

            int cnt = 0;
            for (VariableElement ve : m.getParameters()) {
                source.append(sep).append("final ");
                ++cnt;
                TypeMirror t = ve.asType();
                if (!t.getKind().isPrimitive() && !"java.lang.String".equals(t.toString())) { // NOI18N
                    source.append("java.lang.Object");
                } else {
                    source.append(t);
                }
                source.append(" arg").append(cnt);
                sep = ", ";
            }
        }
        source.append(") throws Throwable {\n");
        source.append("    final org.netbeans.html.boot.spi.Fn.Presenter p = ref.presenter(); \n");
        String newLine = "";
        if (promise) {
            source.append("    return new org.netbeans.html.boot.spi.Fn.Promise() {\n");
            source.append("      @Override\n");
            source.append("      protected java.lang.Object compute() throws java.lang.Throwable {\n");
            newLine = "    ";
        }
        if (useTryResources()) {
            newLine += "      ";
            source.append("    try (java.io.Closeable a = org.netbeans.html.boot.spi.Fn.activate(p)) {");
        } else {
            newLine += "    ";
            source.append("    java.io.Closeable a = org.netbeans.html.boot.spi.Fn.activate(p); try {");
        }
        newLine = "\n" + newLine;
        source.append(newLine);
        if (m.getReturnType().getKind() != TypeKind.VOID) {
            source.append("java.lang.Object $ret = ");
        }
        if (isStatic) {
            source.append(((TypeElement)m.getEnclosingElement()).getQualifiedName());
            source.append('.');
        } else {
            if (selfObj) {
                source.append("cast(p, ");
                source.append(selfType.getQualifiedName());
                source.append(".class, self).");
            } else {
                source.append("self.");
            }
        }
        source.append(m.getSimpleName());
        source.append("(");
        {
            int cnt = 0;
            String sep = newLine + "  ";
            for (VariableElement ve : m.getParameters()) {
                source.append(sep);
                TypeMirror targetType = tu.erasure(ve.asType());
                if (targetType.getKind().isPrimitive()) {
                    targetType = tu.boxedClass((PrimitiveType) targetType).asType();
                }
                source.append("cast(p, ").append(targetType);
                source.append(".class, arg").append(++cnt).append(")");
                sep = "," + newLine + "  ";
            }
        }
        source.append(");").append(newLine);
        if (m.getReturnType().getKind() == TypeKind.VOID) {
            source.append("return null;");
        } else {
            source.append("return toJs(p, $ret);");
        }
        source.append("\n");
        if (useTryResources()) {
            source.append("    }\n");
        } else {

            source.append("    } finally {\n");
            source.append("      a.close();\n");
            source.append("    }\n");
        }
        if (promise) {
            source.append("    }}.toJsPromise();\n");
        }
        source.append("  }\n");
    }

    private boolean useTryResources() {
        try {
            return processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0;
        } catch (LinkageError err) {
            // can happen when running on JDK6
            return false;
        }
    }

    private static String findPkg(Element e) {
        while (e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return ((PackageElement)e).getQualifiedName().toString();
    }

    private String findParamTypes(ExecutableElement method) {
        ExecutableType t = (ExecutableType) method.asType();
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (TypeMirror paramType : t.getParameterTypes()) {
            TypeMirror tm = paramType;
            boolean isArray = false;
            while (tm.getKind() == TypeKind.ARRAY) {
                sb.append('[');
                tm = ((ArrayType) tm).getComponentType();
                isArray = true;
            }
            if (tm.getKind().isPrimitive()) {
                switch (tm.getKind()) {
                    case INT: sb.append('I'); break;
                    case BOOLEAN: sb.append('Z'); break;
                    case BYTE: sb.append('B'); break;
                    case CHAR: sb.append('C'); break;
                    case SHORT: sb.append('S'); break;
                    case DOUBLE: sb.append('D'); break;
                    case FLOAT: sb.append('F'); break;
                    case LONG: sb.append('J'); break;
                    default:
                        throw new IllegalStateException("Unknown " + tm.getKind());
                }
                if (isArray) {
                    wrongArrayError(paramType, method);
                }
            } else {
                sb.append('L');
                Types tu = processingEnv.getTypeUtils();
                final TypeMirror erasedType = tu.erasure(tm);
                TypeMirror objectType = processingEnv.getElementUtils().getTypeElement("java.lang.Object").asType();
                if (isArray && !processingEnv.getTypeUtils().isSameType(objectType, erasedType)) {
                    wrongArrayError(paramType, method);
                }
                Element elm = tu.asElement(erasedType);
                dumpElems(sb, elm, ';');
            }
        }
        sb.append(')');
        return sb.toString();
    }

}
