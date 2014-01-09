/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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

import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = Processor.class)
public final class JavaScriptProcesor extends AbstractProcessor {
    private final Map<String,Map<String,ExecutableElement>> javacalls = 
        new HashMap<String,Map<String,ExecutableElement>>();
    
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<String>();
        set.add(JavaScriptBody.class.getName());
        set.add(JavaScriptResource.class.getName());
        return set;
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
            }
            String[] arr = jsb.args();
            if (params.size() != arr.length) {
                msg.printMessage(Diagnostic.Kind.ERROR, "Number of args arguments does not match real arguments!", e);
            }
            if (!jsb.javacall() && jsb.body().contains(".@")) {
                msg.printMessage(Diagnostic.Kind.WARNING, "Usage of .@ usually requires javacall=true", e);
            }
            if (jsb.javacall()) {
                JsCallback verify = new VerifyCallback(e);
                try {
                    verify.parse(jsb.body());
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
            final String res;
            if (r.value().startsWith("/")) {
                res = r.value();
            } else {
                res = findPkg(e).replace('.', '/') + "/" + r.value();
            }
            
            try {
                FileObject os = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", res);
                os.openInputStream().close();
            } catch (IOException ex1) {
                try {
                    FileObject os2 = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", res);
                    os2.openInputStream().close();
                } catch (IOException ex2) {
                    msg.printMessage(Diagnostic.Kind.ERROR, "Cannot find " + res + " in " + res + " package", e);
                }
            }
        }

        if (roundEnv.processingOver()) {
            generateCallbackClass(javacalls);
            javacalls.clear();
        }
        return true;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element e, 
        AnnotationMirror annotation, ExecutableElement member, String userText
    ) {
        StringBuilder sb = new StringBuilder();
        if (e.getKind() == ElementKind.METHOD && member.getSimpleName().contentEquals("args")) {
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
        return null;
    }

    private class VerifyCallback extends JsCallback {
        private final Element e;
        public VerifyCallback(Element e) {
            this.e = e;
        }

        @Override
        protected CharSequence callMethod(String ident, String fqn, String method, String params) {
            final TypeElement type = processingEnv.getElementUtils().getTypeElement(fqn);
            if (type == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                    "Callback to non-existing class " + fqn, e
                );
                return "";
            }
            ExecutableElement found = null;
            StringBuilder foundParams = new StringBuilder();
            for (Element m : type.getEnclosedElements()) {
                if (m.getKind() != ElementKind.METHOD) {
                    continue;
                }
                if (m.getSimpleName().contentEquals(method)) {
                    String paramTypes = findParamTypes((ExecutableElement)m);
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
                    mangledOnes = new TreeMap<String, ExecutableElement>();
                    javacalls.put(findPkg(e), mangledOnes);
                }
                String mangled = JsCallback.mangle(fqn, method, findParamTypes(found));
                mangledOnes.put(mangled, found);
            }
            return "";
        }

        private String findParamTypes(ExecutableElement method) {
            ExecutableType t = (ExecutableType) method.asType();
            StringBuilder sb = new StringBuilder();
            sb.append('(');
            for (TypeMirror tm : t.getParameterTypes()) {
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
                            throw new IllegalStateException("Uknown " + tm.getKind());
                    }
                } else {
                    while (tm.getKind() == TypeKind.ARRAY) {
                        sb.append('[');
                        tm = ((ArrayType)tm).getComponentType();
                    }
                    sb.append('L');
                    sb.append(tm.toString().replace('.', '/'));
                    sb.append(';');
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }
    
    private void generateCallbackClass(Map<String,Map<String, ExecutableElement>> process) {
        for (Map.Entry<String, Map<String, ExecutableElement>> pkgEn : process.entrySet()) {
            String pkgName = pkgEn.getKey();
            Map<String, ExecutableElement> map = pkgEn.getValue();
            StringBuilder source = new StringBuilder();
            source.append("package ").append(pkgName).append(";\n");
            source.append("public final class $JsCallbacks$ {\n");
            source.append("  static final $JsCallbacks$ VM = new $JsCallbacks$(null);\n");
            source.append("  private final org.apidesign.html.boot.spi.Fn.Presenter p;\n");
            source.append("  private $JsCallbacks$ last;\n");
            source.append("  private $JsCallbacks$(org.apidesign.html.boot.spi.Fn.Presenter p) {\n");
            source.append("    this.p = p;\n");
            source.append("  }\n");
            source.append("  final $JsCallbacks$ current() {\n");
            source.append("    org.apidesign.html.boot.spi.Fn.Presenter now = org.apidesign.html.boot.spi.Fn.activePresenter();\n");
            source.append("    if (now == p) return this;\n");
            source.append("    if (last != null && now == last.p) return last;\n");
            source.append("    return last = new $JsCallbacks$(now);\n");
            source.append("  }\n");
            for (Map.Entry<String, ExecutableElement> entry : map.entrySet()) {
                final String mangled = entry.getKey();
                final ExecutableElement m = entry.getValue();
                final boolean isStatic = m.getModifiers().contains(Modifier.STATIC);
                
                source.append("\n  public java.lang.Object ")
                    .append(mangled)
                    .append("(");
                
                String sep = "";
                if (!isStatic) {
                    source.append(((TypeElement)m.getEnclosingElement()).getQualifiedName());
                    source.append(" self");
                    sep = ", ";
                }
                
                int cnt = 0;
                StringBuilder convert = new StringBuilder();
                for (VariableElement ve : m.getParameters()) {
                    source.append(sep);
                    ++cnt;
                    final TypeMirror t = ve.asType();
                    if (!t.getKind().isPrimitive()) {
                        source.append("Object");
                        convert.append("    if (p instanceof org.apidesign.html.boot.spi.Fn.FromJavaScript) {\n");
                        convert.append("      arg").append(cnt).
                            append(" = ((org.apidesign.html.boot.spi.Fn.FromJavaScript)p).toJava(arg").append(cnt).
                            append(");\n");
                        convert.append("    }\n");
                    } else {
                        source.append(t);
                    }
                    source.append(" arg").append(cnt);
                    sep = ", ";
                }
                source.append(") throws Throwable {\n");
                source.append(convert);
                if (processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0) {
                    source.append("    try (java.io.Closeable a = org.apidesign.html.boot.spi.Fn.activate(p)) { \n");
                } else {
                    source.append("    java.io.Closeable a = org.apidesign.html.boot.spi.Fn.activate(p); try {\n");
                }
                source.append("    ");
                if (m.getReturnType().getKind() != TypeKind.VOID) {
                    source.append("Object $ret = ");
                }
                if (isStatic) {
                    source.append(((TypeElement)m.getEnclosingElement()).getQualifiedName());
                    source.append('.');
                } else {
                    source.append("self.");
                }
                source.append(m.getSimpleName());
                source.append("(");
                cnt = 0;
                sep = "";
                for (VariableElement ve : m.getParameters()) {
                    source.append(sep);
                    source.append("(").append(ve.asType());
                    source.append(")arg").append(++cnt);
                    sep = ", ";
                }
                source.append(");\n");
                if (m.getReturnType().getKind() == TypeKind.VOID) {
                    source.append("    return null;\n");
                } else {
                    source.append("    if (p instanceof org.apidesign.html.boot.spi.Fn.ToJavaScript) {\n");
                    source.append("      $ret = ((org.apidesign.html.boot.spi.Fn.ToJavaScript)p).toJavaScript($ret);\n");
                    source.append("    }\n");
                    source.append("    return $ret;\n");
                }
                if (processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_7) >= 0) {
                    source.append("    }\n");
                } else {
                    
                    source.append("    } finally {\n");
                    source.append("      a.close();\n");
                    source.append("    }\n");
                }
                source.append("  }\n");
            }
            source.append("}\n");
            final String srcName = pkgName + ".$JsCallbacks$";
            try {
                Writer w = processingEnv.getFiler().createSourceFile(srcName,
                    map.values().toArray(new Element[map.size()])
                ).openWriter();
                w.write(source.toString());
                w.close();
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Can't write " + srcName + ": " + ex.getMessage()
                );
            }
        }
    }
    
    private static String findPkg(Element e) {
        while (e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return ((PackageElement)e).getQualifiedName().toString();
    }
    
}
