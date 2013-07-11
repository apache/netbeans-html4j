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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
                verify.parse(jsb.body());
            }
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
            if (processingEnv.getElementUtils().getTypeElement(fqn) == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, 
                    "Callback to non-existing class " + fqn, e
                );
            }
            return "";
        }
        
    }
}
