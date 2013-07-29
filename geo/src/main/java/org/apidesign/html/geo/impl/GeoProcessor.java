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
package org.apidesign.html.geo.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import net.java.html.geo.OnLocation;
import net.java.html.geo.Position;
import org.openide.util.lookup.ServiceProvider;

/** Annotation processor to generate callbacks from {@link GeoHandle} class.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({
    "net.java.html.geo.OnLocation"
})
public final class GeoProcessor extends AbstractProcessor {
    private static final Logger LOG = Logger.getLogger(GeoProcessor.class.getName());
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean ok = true;
        for (Element e : roundEnv.getElementsAnnotatedWith(OnLocation.class)) {
            if (!processLocation(e)) {
                ok = false;
            }
        }
        return ok;
    }

    private void error(String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
    
    private boolean processLocation(Element e) {
        if (e.getKind() != ElementKind.METHOD) {
            return false;
        }
        ExecutableElement me = (ExecutableElement) e;
        OnLocation ol = e.getAnnotation(OnLocation.class);
        if (ol == null) {
            return true;
        }
        if (me.getModifiers().contains(Modifier.PRIVATE)) {
            error("Method annotated by @OnLocation cannot be private", e);
            return false;
        }
        TypeMirror positionClass = processingEnv.getElementUtils().getTypeElement(Position.class.getName()).asType();
        final List<? extends VariableElement> params = me.getParameters();
        if (params.size() < 1 || !params.get(0).asType().equals(positionClass)) {
            error("Method annotated by @OnLocation first argument must be net.java.html.geo.Position!", e);
            return false;
        }
        String className = ol.className();
        if (className.isEmpty()) {
            String n = e.getSimpleName().toString();
            if (n.isEmpty()) {
                error("Empty method name", e);
                return false;
            }
            final String firstLetter = n.substring(0, 1).toUpperCase(Locale.ENGLISH);
            className = firstLetter + n.substring(1) + "Handle";
        }
        TypeElement te = (TypeElement)e.getEnclosingElement();
        PackageElement pe = (PackageElement) te.getEnclosingElement();
        final String pkg = pe.getQualifiedName().toString();
        final String fqn = pkg + "." + className;
        final boolean isStatic = me.getModifiers().contains(Modifier.STATIC);
        String sep;
        try {
            JavaFileObject fo = processingEnv.getFiler().createSourceFile(fqn, e);
            Writer w = fo.openWriter();
            w.append("package ").append(pkg).append(";\n");
            w.append("class ").append(className).append(" extends net.java.html.geo.Position.Handle {\n");
            if (!isStatic) {
                w.append("  private final ").append(te.getSimpleName()).append(" $i;\n");
            }
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append("  private final ").append(p.asType().toString()).append(" ").append(p.getSimpleName()).append(";\n");
            }
            w.append("  private ").append(className).append("(boolean oneTime");
            w.append(", ").append(te.getSimpleName()).append(" i");
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append(", ").append(p.asType().toString()).append(" ").append(p.getSimpleName());
            }
            w.append(") {\n    super(oneTime);\n");
            if (!isStatic) {
                w.append("    this.$i = i;\n");
            }
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append("  this.").append(p.getSimpleName()).append(" = ").append(p.getSimpleName()).append(";\n");
            }
            w.append("}\n");
            w.append("  static net.java.html.geo.Position.Handle createQuery(");
            String inst;
            if (!isStatic) {
                w.append(te.getSimpleName()).append(" instance");
                inst = "instance";
                sep = ", ";
            } else {
                inst = "null";
                sep = "";
            }
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append(sep).append(p.asType().toString()).append(" ").append(p.getSimpleName());
                sep = ", ";
            }
            w.append(") { return new ").append(className).append("(true, ").append(inst);
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append(", ").append(p.getSimpleName());
            }
            w.append("); }\n");
            w.append("  static net.java.html.geo.Position.Handle createWatch(");
            if (!isStatic) {
                w.append(te.getSimpleName()).append(" instance");
                sep = ", ";
            } else {
                sep = "";
            }
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append(sep).append(p.asType().toString()).append(" ").append(p.getSimpleName());
            }
            w.append(") { return new ").append(className).append("(false, ").append(inst);
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append(", ").append(p.getSimpleName());
            }
            w.append("); }\n");
            w.append("  @Override protected void onError(Exception t) throws Throwable {\n");
            if (ol.onError().isEmpty()) {
                w.append("    t.printStackTrace();");
            } else {
                if (!findOnError(me, te, ol.onError(), isStatic)) {
                    return false;
                }
                if (isStatic) {
                    w.append("    ").append(te.getSimpleName()).append(".");
                } else {
                    w.append("    $i.");
                }
                w.append(ol.onError()).append("(t");
                for (int i = 1; i < params.size(); i++) {
                    final VariableElement p = params.get(i);
                    w.append(", ").append(p.getSimpleName());
                }
                w.append(");\n");
            }
            w.append("  }\n");
            w.append("  @Override protected void onLocation(net.java.html.geo.Position p) throws Throwable {\n");
            if (isStatic) {
                w.append("    ").append(te.getSimpleName()).append(".");
            } else {
                w.append("    $i.");
            }
            w.append(me.getSimpleName()).append("(p");
            for (int i = 1; i < params.size(); i++) {
                final VariableElement p = params.get(i);
                w.append(", ").append(p.getSimpleName());
            }
            w.append(");\n");
            w.append("  }\n");
            w.append("}\n");
            w.close();
        } catch (IOException ex) {
            Logger.getLogger(GeoProcessor.class.getName()).log(Level.SEVERE, null, ex);
            error("Can't write handler class: " + ex.getMessage(), e);
            return false;
        }
        
        return true;
    }

    private boolean findOnError(ExecutableElement errElem, TypeElement te, String name, boolean onlyStatic) {
        String err = null;
        METHODS: for (Element e : te.getEnclosedElements()) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (!e.getSimpleName().contentEquals(name)) {
                continue;
            }
            if (onlyStatic && !e.getModifiers().contains(Modifier.STATIC)) {
                errElem = (ExecutableElement) e;
                err = "Would have to be static";
                continue;
            }
            ExecutableElement ee = (ExecutableElement) e;
            TypeMirror excType = processingEnv.getElementUtils().getTypeElement(Exception.class.getName()).asType();
            final List<? extends VariableElement> params = ee.getParameters(); 
            if (params.size() < 1 || 
                !processingEnv.getTypeUtils().isAssignable(excType, ee.getParameters().get(0).asType())
            ) {
                errElem = (ExecutableElement) e;
                err = "Error method first argument needs to be Exception";
                continue;
            }
            final List<? extends Element> origParams = errElem.getParameters();
            if (params.size() != origParams.size()) {
                errElem = (ExecutableElement) e;
                err = "Error method must have the same parameters as @OnLocation one";
                continue;
            }
            for (int i = 1; i < origParams.size(); i++) {
                final TypeMirror t1 = params.get(i).asType();
                final TypeMirror t2 = origParams.get(i).asType();
                if (!processingEnv.getTypeUtils().isSameType(t1, t2)) {
                    errElem = (ExecutableElement) e;
                    err = "Error method must have the same parameters as @OnLocation one";
                    continue METHODS;
                }
            }
            return true;
        }
        if (err == null) {
            err = "Cannot find " + name + "(Exception) method in this class";
        }
        error(err, errElem);
        return false;
    }
}
