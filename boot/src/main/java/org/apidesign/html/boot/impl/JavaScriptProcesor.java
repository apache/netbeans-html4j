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
            source.append("    org.apidesign.html.boot.spi.Fn.Presenter now = org.apidesign.html.boot.impl.FnUtils.currentPresenter();\n");
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
                for (VariableElement ve : m.getParameters()) {
                    source.append(sep);
                    source.append(ve.asType());
                    source.append(" arg").append(++cnt);
                    sep = ", ";
                }
                source.append(")");
                sep = "\n throws ";
                for (TypeMirror thrwn : m.getThrownTypes()) {
                    source.append(sep).append(thrwn.toString());
                    sep = ",";
                }
                source.append(" {\n");
                source.append("    org.apidesign.html.boot.spi.Fn.Presenter $$prev = org.apidesign.html.boot.impl.FnUtils.currentPresenter(p); try { \n");
                source.append("    ");
                if (m.getReturnType().getKind() != TypeKind.VOID) {
                    source.append("return ");
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
                    source.append("arg").append(++cnt);
                    sep = ", ";
                }
                source.append(");\n");
                if (m.getReturnType().getKind() == TypeKind.VOID) {
                    source.append("    return null;\n");
                }
                source.append("    } finally { org.apidesign.html.boot.impl.FnUtils.currentPresenter($$prev); }\n");
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
