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
package org.apidesign.html.json.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Function;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import org.openide.util.lookup.ServiceProvider;

/** Annotation processor to process an XHTML page and generate appropriate 
 * "id" file.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes({
    "net.java.html.json.Model",
    "net.java.html.json.Function",
    "net.java.html.json.OnReceive",
    "net.java.html.json.OnPropertyChange",
    "net.java.html.json.ComputedProperty",
    "net.java.html.json.Property"
})
public final class ModelProcessor extends AbstractProcessor {
    private final Map<Element,String> models = new WeakHashMap<>();
    private final Map<Element,Prprt[]> verify = new WeakHashMap<>();
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean ok = true;
        for (Element e : roundEnv.getElementsAnnotatedWith(Model.class)) {
            if (!processModel(e)) {
                ok = false;
            }
        }
        if (roundEnv.processingOver()) {
            models.clear();
            for (Map.Entry<Element, Prprt[]> entry : verify.entrySet()) {
                TypeElement te = (TypeElement)entry.getKey();
                String fqn = processingEnv.getElementUtils().getBinaryName(te).toString();
                Element finalElem = processingEnv.getElementUtils().getTypeElement(fqn);
                if (finalElem == null) {
                    continue;
                }
                Prprt[] props;
                Model m = finalElem.getAnnotation(Model.class);
                if (m == null) {
                    continue;
                }
                props = Prprt.wrap(processingEnv, finalElem, m.properties());
                for (Prprt p : props) {
                    boolean[] isModel = { false };
                    boolean[] isEnum = { false };
                    boolean[] isPrimitive = { false };
                    String t = checkType(p, isModel, isEnum, isPrimitive);
                    if (isEnum[0]) {
                        continue;
                    }
                    if (isPrimitive[0]) {
                        continue;
                    }
                    if (isModel[0]) {
                        continue;
                    }
                    if ("java.lang.String".equals(t)) {
                        continue;
                    }
                    error("The type " + t + " should be defined by @Model annotation", entry.getKey());
                }
            }
            verify.clear();
        }
        return ok;
    }

    private InputStream openStream(String pkg, String name) throws IOException {
        try {
            FileObject fo = processingEnv.getFiler().getResource(
                StandardLocation.SOURCE_PATH, pkg, name);
            return fo.openInputStream();
        } catch (IOException ex) {
            return processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, pkg, name).openInputStream();
        }
    }

    private void error(String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
    
    private boolean processModel(Element e) {
        boolean ok = true;
        Model m = e.getAnnotation(Model.class);
        if (m == null) {
            return true;
        }
        String pkg = findPkgName(e);
        Writer w;
        String className = m.className();
        models.put(e, className);
        try {
            StringWriter body = new StringWriter();
            List<String> propsGetSet = new ArrayList<>();
            List<String> functions = new ArrayList<>();
            Map<String, Collection<String>> propsDeps = new HashMap<>();
            Map<String, Collection<String>> functionDeps = new HashMap<>();
            Prprt[] props = createProps(e, m.properties());
            
            if (!generateComputedProperties(body, props, e.getEnclosedElements(), propsGetSet, propsDeps)) {
                ok = false;
            }
            if (!generateOnChange(e, propsDeps, props, className, functionDeps)) {
                ok = false;
            }
            if (!generateProperties(e, body, props, propsGetSet, propsDeps, functionDeps)) {
                ok = false;
            }
            if (!generateFunctions(e, body, className, e.getEnclosedElements(), functions)) {
                ok = false;
            }
            FileObject java = processingEnv.getFiler().createSourceFile(pkg + '.' + className, e);
            w = new OutputStreamWriter(java.openOutputStream());
            try {
                w.append("package " + pkg + ";\n");
                w.append("import net.java.html.json.*;\n");
                w.append("public final class ").append(className).append(" implements Cloneable {\n");
                w.append("  private boolean locked;\n");
                w.append("  private net.java.html.json.Context context;\n");
                w.append("  private org.apidesign.html.json.impl.Bindings ko;\n");
                w.append(body.toString());
                w.append("  private static Class<" + inPckName(e) + "> modelFor() { return null; }\n");
                w.append("  public ").append(className).append("(Context context) {\n");
                w.append("    this.context = context;\n");
                w.append("  };\n");
                w.append("  private org.apidesign.html.json.impl.Bindings intKnckt() {\n");
                w.append("    if (ko != null) return ko;\n");
                w.append("    return ko = org.apidesign.html.json.impl.Bindings.apply(context, this, ");
                writeStringArray(propsGetSet, w);
                w.append(", ");
                writeStringArray(functions, w);
                w.append("    );\n");
                w.append("  };\n");
                w.append("  ").append(className).append("(Object json) {\n");
                int values = 0;
                for (int i = 0; i < propsGetSet.size(); i += 4) {
                    Prprt p = findPrprt(props, propsGetSet.get(i));
                    if (p == null) {
                        continue;
                    }
                    values++;
                }
                w.append("    Object[] ret = new Object[" + values + "];\n");
                w.append("    org.apidesign.html.json.impl.JSON.extract(json, new String[] {\n");
                for (int i = 0; i < propsGetSet.size(); i += 4) {
                    Prprt p = findPrprt(props, propsGetSet.get(i));
                    if (p == null) {
                        continue;
                    }
                    w.append("      \"").append(propsGetSet.get(i)).append("\",\n");
                }
                w.append("    }, ret);\n");
                for (int i = 0, cnt = 0, prop = 0; i < propsGetSet.size(); i += 4) {
                    final String pn = propsGetSet.get(i);
                    Prprt p = findPrprt(props, pn);
                    if (p == null) {
                        continue;
                    }
                    boolean[] isModel = { false };
                    boolean[] isEnum = { false };
                    boolean isPrimitive[] = { false };
                    String type = checkType(props[prop++], isModel, isEnum, isPrimitive);
                    if (p.array()) {
                        w.append("if (ret[" + cnt + "] instanceof Object[]) {\n");
                        w.append("  for (Object e : ((Object[])ret[" + cnt + "])) {\n");
                        if (isModel[0]) {
                            w.append("    this.prop_").append(pn).append(".add(new ");
                            w.append(type).append("(e));\n");
                        } else if (isEnum[0]) {
                            w.append("    this.prop_").append(pn);
                            w.append(".add(e == null ? null : ");
                            w.append(type).append(".valueOf((String)e));\n");
                        } else {
                            if (isPrimitive(type)) {
                                w.append("    this.prop_").append(pn).append(".add(((Number)e).");
                                w.append(type).append("Value());\n");
                            } else {
                                w.append("    this.prop_").append(pn).append(".add((");
                                w.append(type).append(")e);\n");
                            }
                        }
                        w.append("  }\n");
                        w.append("}\n");
                    } else {
                        if (isEnum[0]) {
                            w.append("    this.prop_").append(pn);
                            w.append(" = ret[" + cnt + "] == null ? null : ");
                            w.append(type).append(".valueOf((String)ret[" + cnt + "]);\n");
                        } else if (isPrimitive(type)) {
                            w.append("    this.prop_").append(pn);
                            w.append(" = ((Number)").append("ret[" + cnt + "]).");
                            w.append(type).append("Value();\n");
                        } else {
                            w.append("    this.prop_").append(pn);
                            w.append(" = (").append(type).append(')');
                            w.append("ret[" + cnt + "];\n");
                        }
                    }
                    cnt++;
                }
                w.append("  };\n");
                writeToString(props, w);
                writeClone(className, props, w);
                w.append("  public Object koData() {\n");
                w.append("    return intKnckt().koData();\n");
                w.append("  }\n");
                w.append("}\n");
            } finally {
                w.close();
            }
        } catch (IOException ex) {
            error("Can't create " + className + ".java", e);
            return false;
        }
        return ok;
    }
    
    private boolean generateProperties(
        Element where,
        Writer w, Prprt[] properties,
        Collection<String> props, 
        Map<String,Collection<String>> deps,
        Map<String,Collection<String>> functionDeps
    ) throws IOException {
        boolean ok = true;
        for (Prprt p : properties) {
            final String tn;
            tn = typeName(where, p);
            String[] gs = toGetSet(p.name(), tn, p.array());

            if (p.array()) {
                w.write("private org.apidesign.html.json.impl.JSONList<" + tn + "> prop_" + p.name() + " = new org.apidesign.html.json.impl.JSONList<" + tn + ">(\""
                    + p.name() + "\"");
                Collection<String> dependants = deps.get(p.name());
                if (dependants != null) {
                    for (String depProp : dependants) {
                        w.write(", ");
                        w.write('\"');
                        w.write(depProp);
                        w.write('\"');
                    }
                }
                w.write(")");
                
                dependants = functionDeps.get(p.name());
                if (dependants != null) {
                    w.write(".onChange(new Runnable() { public void run() {\n");
                    for (String call : dependants) {
                        w.append(call);
                    }
                    w.write("}})");
                }
                w.write(";\n");
                
                w.write("public java.util.List<" + tn + "> " + gs[0] + "() {\n");
                w.write("  if (locked) throw new IllegalStateException();\n");
                w.write("  prop_" + p.name() + ".assign(this.intKnckt());\n");
                w.write("  return prop_" + p.name() + ";\n");
                w.write("}\n");
            } else {
                w.write("private " + tn + " prop_" + p.name() + ";\n");
                w.write("public " + tn + " " + gs[0] + "() {\n");
                w.write("  if (locked) throw new IllegalStateException();\n");
                w.write("  return prop_" + p.name() + ";\n");
                w.write("}\n");
                w.write("public void " + gs[1] + "(" + tn + " v) {\n");
                w.write("  if (locked) throw new IllegalStateException();\n");
                w.write("  prop_" + p.name() + " = v;\n");
                w.write("  org.apidesign.html.json.impl.Bindings b = intKnckt();\n");
                w.write("  if (b != null) {\n");
                w.write("    b.valueHasMutated(\"" + p.name() + "\");\n");
                Collection<String> dependants = deps.get(p.name());
                if (dependants != null) {
                    for (String depProp : dependants) {
                        w.write("    b.valueHasMutated(\"" + depProp + "\");\n");
                    }
                }
                w.write("  }\n");
                dependants = functionDeps.get(p.name());
                if (dependants != null) {
                    for (String call : dependants) {
                        w.append(call);
                    }
                }
                w.write("}\n");
            }
            
            props.add(p.name());
            props.add(gs[2]);
            props.add(gs[3]);
            props.add(gs[0]);
        }
        return ok;
    }

    private boolean generateComputedProperties(
        Writer w, Prprt[] fixedProps,
        Collection<? extends Element> arr, Collection<String> props,
        Map<String,Collection<String>> deps
    ) throws IOException {
        boolean ok = true;
        for (Element e : arr) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (e.getAnnotation(ComputedProperty.class) == null) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement)e;
            final TypeMirror rt = ee.getReturnType();
            final Types tu = processingEnv.getTypeUtils();
            TypeMirror ert = tu.erasure(rt);
            String tn = fqn(ert, ee);
            boolean array = false;
            if (tn.equals("java.util.List")) {
                array = true;
            }
            
            final String sn = ee.getSimpleName().toString();
            String[] gs = toGetSet(sn, tn, array);
            
            w.write("public " + tn + " " + gs[0] + "() {\n");
            w.write("  if (locked) throw new IllegalStateException();\n");
            int arg = 0;
            for (VariableElement pe : ee.getParameters()) {
                final String dn = pe.getSimpleName().toString();
                
                if (!verifyPropName(pe, dn, fixedProps)) {
                    ok = false;
                }
                
                final String dt = fqn(pe.asType(), ee);
                String[] call = toGetSet(dn, dt, false);
                w.write("  " + dt + " arg" + (++arg) + " = ");
                w.write(call[0] + "();\n");
                
                Collection<String> depends = deps.get(dn);
                if (depends == null) {
                    depends = new LinkedHashSet<>();
                    deps.put(dn, depends);
                }
                depends.add(sn);
            }
            w.write("  try {\n");
            w.write("    locked = true;\n");
            w.write("    return " + fqn(ee.getEnclosingElement().asType(), ee) + '.' + e.getSimpleName() + "(");
            String sep = "";
            for (int i = 1; i <= arg; i++) {
                w.write(sep);
                w.write("arg" + i);
                sep = ", ";
            }
            w.write(");\n");
            w.write("  } finally {\n");
            w.write("    locked = false;\n");
            w.write("  }\n");
            w.write("}\n");

            props.add(e.getSimpleName().toString());
            props.add(gs[2]);
            props.add(null);
            props.add(gs[0]);
        }
        
        return ok;
    }

    private static String[] toGetSet(String name, String type, boolean array) {
        String n = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String bck2brwsrType = "L" + type.replace('.', '_') + "_2";
        if ("int".equals(type)) {
            bck2brwsrType = "I";
        }
        if ("double".equals(type)) {
            bck2brwsrType = "D";
        }
        String pref = "get";
        if ("boolean".equals(type)) {
            pref = "is";
            bck2brwsrType = "Z";
        }
        final String nu = n.replace('.', '_');
        if (array) {
            return new String[] { 
                "get" + n,
                null,
                "get" + nu + "__Ljava_util_List_2",
                null
            };
        }
        return new String[]{
            pref + n, 
            "set" + n, 
            pref + nu + "__" + bck2brwsrType,
            "set" + nu + "__V" + bck2brwsrType
        };
    }

    private String typeName(Element where, Prprt p) {
        String ret;
        boolean[] isModel = { false };
        boolean[] isEnum = { false };
        boolean isPrimitive[] = { false };
        ret = checkType(p, isModel, isEnum, isPrimitive);
        if (p.array()) {
            String bt = findBoxedType(ret);
            if (bt != null) {
                return bt;
            }
        }
        return ret;
    }
    
    private static String findBoxedType(String ret) {
        if (ret.equals("boolean")) {
            return Boolean.class.getName();
        }
        if (ret.equals("byte")) {
            return Byte.class.getName();
        }
        if (ret.equals("short")) {
            return Short.class.getName();
        }
        if (ret.equals("char")) {
            return Character.class.getName();
        }
        if (ret.equals("int")) {
            return Integer.class.getName();
        }
        if (ret.equals("long")) {
            return Long.class.getName();
        }
        if (ret.equals("float")) {
            return Float.class.getName();
        }
        if (ret.equals("double")) {
            return Double.class.getName();
        }
        return null;
    }

    private boolean verifyPropName(Element e, String propName, Prprt[] existingProps) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Prprt Prprt : existingProps) {
            if (Prprt.name().equals(propName)) {
                return true;
            }
            sb.append(sep);
            sb.append('"');
            sb.append(Prprt.name());
            sb.append('"');
            sep = ", ";
        }
        error(
            propName + " is not one of known properties: " + sb
            , e
        );
        return false;
    }

    private static String findPkgName(Element e) {
        for (;;) {
            if (e.getKind() == ElementKind.PACKAGE) {
                return ((PackageElement)e).getQualifiedName().toString();
            }
            e = e.getEnclosingElement();
        }
    }

    private boolean generateFunctions(
        Element clazz, StringWriter body, String className, 
        List<? extends Element> enclosedElements, List<String> functions
    ) {
        for (Element m : enclosedElements) {
            if (m.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement e = (ExecutableElement)m;
            Function onF = e.getAnnotation(Function.class);
            if (onF == null) {
                continue;
            }
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("@OnFunction method needs to be static", e);
                return false;
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                error("@OnFunction method cannot be private", e);
                return false;
            }
            if (e.getReturnType().getKind() != TypeKind.VOID) {
                error("@OnFunction method should return void", e);
                return false;
            }
            String n = e.getSimpleName().toString();
            body.append("private void ").append(n).append("(Object data, Object ev) {\n");
            body.append("  ").append(clazz.getSimpleName()).append(".").append(n).append("(");
            body.append(wrapParams(e, null, className, "ev", "data"));
            body.append(");\n");
            body.append("}\n");
            
            functions.add(n);
            functions.add(n + "__VLjava_lang_Object_2Ljava_lang_Object_2");
        }
        return true;
    }

    private boolean generateOnChange(Element clazz, Map<String,Collection<String>> propDeps,
        Prprt[] properties, String className, 
        Map<String, Collection<String>> functionDeps
    ) {
        for (Element m : clazz.getEnclosedElements()) {
            if (m.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement e = (ExecutableElement) m;
            OnPropertyChange onPC = e.getAnnotation(OnPropertyChange.class);
            if (onPC == null) {
                continue;
            }
            for (String pn : onPC.value()) {
                if (findPrprt(properties, pn) == null && findDerivedFrom(propDeps, pn).isEmpty()) {
                    error("No Prprt named '" + pn + "' in the model", clazz);
                    return false;
                }
            }
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("@OnPrprtChange method needs to be static", e);
                return false;
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                error("@OnPrprtChange method cannot be private", e);
                return false;
            }
            if (e.getReturnType().getKind() != TypeKind.VOID) {
                error("@OnPrprtChange method should return void", e);
                return false;
            }
            String n = e.getSimpleName().toString();
            
            
            for (String pn : onPC.value()) {
                StringBuilder call = new StringBuilder();
                call.append("  ").append(clazz.getSimpleName()).append(".").append(n).append("(");
                call.append(wrapPropName(e, className, "name", pn));
                call.append(");\n");
                
                Collection<String> change = functionDeps.get(pn);
                if (change == null) {
                    change = new ArrayList<>();
                    functionDeps.put(pn, change);
                }
                change.add(call.toString());
                for (String dpn : findDerivedFrom(propDeps, pn)) {
                    change = functionDeps.get(dpn);
                    if (change == null) {
                        change = new ArrayList<>();
                        functionDeps.put(dpn, change);
                    }
                    change.add(call.toString());
                }
            }
        }
        return true;
    }
    
    private boolean generateReceive(
        Element clazz, StringWriter body, String className, 
        List<? extends Element> enclosedElements, List<String> functions
    ) {
        for (Element m : enclosedElements) {
            if (m.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement e = (ExecutableElement)m;
            OnReceive onR = e.getAnnotation(OnReceive.class);
            if (onR == null) {
                continue;
            }
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("@OnReceive method needs to be static", e);
                return false;
            }
            if (e.getModifiers().contains(Modifier.PRIVATE)) {
                error("@OnReceive method cannot be private", e);
                return false;
            }
            if (e.getReturnType().getKind() != TypeKind.VOID) {
                error("@OnReceive method should return void", e);
                return false;
            }
            String modelClass = null;
            boolean expectsList = false;
            List<String> args = new ArrayList<>();
            {
                for (VariableElement ve : e.getParameters()) {
                    TypeMirror modelType = null;
                    if (ve.asType().toString().equals(className)) {
                        args.add(className + ".this");
                    } else if (isModel(ve.asType())) {
                        modelType = ve.asType();
                    } else if (ve.asType().getKind() == TypeKind.ARRAY) {
                        modelType = ((ArrayType)ve.asType()).getComponentType();
                        expectsList = true;
                    }
                    if (modelType != null) {
                        if (modelClass != null) {
                            error("There can be only one model class among arguments", e);
                        } else {
                            modelClass = modelType.toString();
                            if (expectsList) {
                                args.add("arr");
                            } else {
                                args.add("arr[0]");
                            }
                        }
                    }
                }
            }
            if (modelClass == null) {
                error("The method needs to have one @Model class as parameter", e);
            }
            String n = e.getSimpleName().toString();
            body.append("public void ").append(n).append("(");
            StringBuilder assembleURL = new StringBuilder();
            String jsonpVarName = null;
            {
                String sep = "";
                boolean skipJSONP = onR.jsonp().isEmpty();
                for (String p : findParamNames(e, onR.url(), assembleURL)) {
                    if (!skipJSONP && p.equals(onR.jsonp())) {
                        skipJSONP = true;
                        jsonpVarName = p;
                        continue;
                    }
                    body.append(sep);
                    body.append("String ").append(p);
                    sep = ", ";
                }
                if (!skipJSONP) {
                    error(
                        "Name of jsonp attribute ('" + onR.jsonp() + 
                        "') is not used in url attribute '" + onR.url() + "'", e
                    );
                }
            }
            body.append(") {\n");
            body.append("  final Object[] result = { null };\n");
            body.append(
                "  class ProcessResult implements Runnable {\n" +
                "    @Override\n" +
                "    public void run() {\n" +
                "      Object value = result[0];\n");
            body.append(
                "      " + modelClass + "[] arr;\n");
            body.append(
                "      if (value instanceof Object[]) {\n" +
                "        Object[] data = ((Object[])value);\n" +
                "        arr = new " + modelClass + "[data.length];\n" +
                "        for (int i = 0; i < data.length; i++) {\n" +
                "          arr[i] = new " + modelClass + "(data[i]);\n" +
                "        }\n" +
                "      } else {\n" +
                "        arr = new " + modelClass + "[1];\n" +
                "        arr[0] = new " + modelClass + "(value);\n" +
                "      }\n"
            );
            {
                body.append(clazz.getSimpleName()).append(".").append(n).append("(");
                String sep = "";
                for (String arg : args) {
                    body.append(sep);
                    body.append(arg);
                    sep = ", ";
                }
                body.append(");\n");
            }
            body.append(
                "    }\n" +
                "  }\n"
            );
            body.append("  ProcessResult pr = new ProcessResult();\n");
            if (jsonpVarName != null) {
                body.append("  String ").append(jsonpVarName).
                    append(" = org.apidesign.html.json.impl.JSON.createJSONP(result, pr);\n");
            }
            body.append("  org.apidesign.html.json.impl.JSON.loadJSON(\n      ");
            body.append(assembleURL);
            body.append(", result, pr, ").append(jsonpVarName).append("\n  );\n");
//            body.append("  ").append(clazz.getSimpleName()).append(".").append(n).append("(");
//            body.append(wrapParams(e, null, className, "ev", "data"));
//            body.append(");\n");
            body.append("}\n");
        }
        return true;
    }

    private CharSequence wrapParams(
        ExecutableElement ee, String id, String className, String evName, String dataName
    ) {
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        StringBuilder params = new StringBuilder();
        boolean first = true;
        for (VariableElement ve : ee.getParameters()) {
            if (!first) {
                params.append(", ");
            }
            first = false;
            String toCall = null;
            if (ve.asType() == stringType) {
                if (ve.getSimpleName().contentEquals("id")) {
                    params.append('"').append(id).append('"');
                    continue;
                }
                toCall = "org.apidesign.html.json.impl.JSON.toString(";
            }
            if (ve.asType().getKind() == TypeKind.DOUBLE) {
                toCall = "org.apidesign.html.json.impl.JSON.toDouble(";
            }
            if (ve.asType().getKind() == TypeKind.INT) {
                toCall = "org.apidesign.html.json.impl.JSON.toInt(";
            }
            if (dataName != null && ve.getSimpleName().contentEquals(dataName) && isModel(ve.asType())) {
                toCall = "org.apidesign.html.json.impl.JSON.toModel(" + ve.asType() + ".class, ";
            }

            if (toCall != null) {
                params.append(toCall);
                if (dataName != null && ve.getSimpleName().contentEquals(dataName)) {
                    params.append(dataName);
                    params.append(", null");
                } else {
                    if (evName == null) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("Unexpected string parameter name.");
                        if (dataName != null) {
                            sb.append(" Try \"").append(dataName).append("\"");
                        }
                        error(sb.toString(), ee);
                    }
                    params.append(evName);
                    params.append(", \"");
                    params.append(ve.getSimpleName().toString());
                    params.append("\"");
                }
                params.append(")");
                continue;
            }
            String rn = fqn(ve.asType(), ee);
            int last = rn.lastIndexOf('.');
            if (last >= 0) {
                rn = rn.substring(last + 1);
            }
            if (rn.equals(className)) {
                params.append(className).append(".this");
                continue;
            }
            error(
                "@On method can only accept String named 'id' or " + className + " arguments",
                ee
            );
        }
        return params;
    }
    
    
    private CharSequence wrapPropName(
        ExecutableElement ee, String className, String propName, String propValue
    ) {
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        StringBuilder params = new StringBuilder();
        boolean first = true;
        for (VariableElement ve : ee.getParameters()) {
            if (!first) {
                params.append(", ");
            }
            first = false;
            if (ve.asType() == stringType) {
                if (propName != null && ve.getSimpleName().contentEquals(propName)) {
                    params.append('"').append(propValue).append('"');
                } else {
                    error("Unexpected string parameter name. Try \"" + propName + "\".", ee);
                }
                continue;
            }
            String rn = fqn(ve.asType(), ee);
            int last = rn.lastIndexOf('.');
            if (last >= 0) {
                rn = rn.substring(last + 1);
            }
            if (rn.equals(className)) {
                params.append(className).append(".this");
                continue;
            }
            error(
                "@OnPrprtChange method can only accept String or " + className + " arguments",
                ee);
        }
        return params;
    }
    
    private boolean isModel(TypeMirror tm) {
        final Element e = processingEnv.getTypeUtils().asElement(tm);
        if (e == null) {
            return false;
        }
        for (Element ch : e.getEnclosedElements()) {
            if (ch.getKind() == ElementKind.METHOD) {
                ExecutableElement ee = (ExecutableElement)ch;
                if (ee.getParameters().isEmpty() && ee.getSimpleName().contentEquals("modelFor")) {
                    return true;
                }
            }
        }
        return models.values().contains(e.getSimpleName().toString());
    }

    private void writeStringArray(List<String> strings, Writer w) throws IOException {
        w.write("new String[] {\n");
        String sep = "";
        for (String n : strings) {
            w.write(sep);
            if (n == null) {
                w.write("    null");
            } else {
                w.write("    \"" + n + "\"");
            }
            sep = ",\n";
        }
        w.write("\n  }");
    }
    
    private void writeToString(Prprt[] props, Writer w) throws IOException {
        w.write("  public String toString() {\n");
        w.write("    StringBuilder sb = new StringBuilder();\n");
        w.write("    sb.append('{');\n");
        String sep = "";
        for (Prprt p : props) {
            w.write(sep);
            w.append("    sb.append(\"" + p.name() + ": \");\n");
            w.append("    sb.append(org.apidesign.html.json.impl.JSON.toJSON(prop_");
            w.append(p.name()).append("));\n");
            sep =    "    sb.append(',');\n";
        }
        w.write("    sb.append('}');\n");
        w.write("    return sb.toString();\n");
        w.write("  }\n");
    }
    private void writeClone(String className, Prprt[] props, Writer w) throws IOException {
        w.write("  public " + className + " clone() {\n");
        w.write("    " + className + " ret = new " + className + "(context);\n");
        for (Prprt p : props) {
            if (!p.array()) {
                boolean isModel[] = { false };
                boolean isEnum[] = { false };
                boolean isPrimitive[] = { false };
                checkType(p, isModel, isEnum, isPrimitive);
                if (!isModel[0]) {
                    w.write("    ret.prop_" + p.name() + " = prop_" + p.name() + ";\n");
                    continue;
                }
                w.write("    ret.prop_" + p.name() + " = prop_" + p.name() + ".clone();\n");
            } else {
                w.write("    ret.prop_" + p.name() + " = prop_" + p.name() + ".clone();\n");
            }
        }
        
        w.write("    return ret;\n");
        w.write("  }\n");
    }

    private String inPckName(Element e) {
        StringBuilder sb = new StringBuilder();
        while (e.getKind() != ElementKind.PACKAGE) {
            if (sb.length() == 0) {
                sb.append(e.getSimpleName());
            } else {
                sb.insert(0, '.');
                sb.insert(0, e.getSimpleName());
            }
            e = e.getEnclosingElement();
        }
        return sb.toString();
    }

    private String fqn(TypeMirror pt, Element relative) {
        if (pt.getKind() == TypeKind.ERROR) {
            final Elements eu = processingEnv.getElementUtils();
            PackageElement pckg = eu.getPackageOf(relative);
            return pckg.getQualifiedName() + "." + pt.toString();
        }
        return pt.toString();
    }

    private String checkType(Prprt p, boolean[] isModel, boolean[] isEnum, boolean[] isPrimitive) {
        TypeMirror tm;
        try {
            String ret = p.typeName(processingEnv);
            TypeElement e = processingEnv.getElementUtils().getTypeElement(ret);
            if (e == null) {
                isModel[0] = true;
                isEnum[0] = false;
                isPrimitive[0] = false;
                return ret;
            }
            tm = e.asType();
        } catch (MirroredTypeException ex) {
            tm = ex.getTypeMirror();
        }
        tm = processingEnv.getTypeUtils().erasure(tm);
        isPrimitive[0] = tm.getKind().isPrimitive();
        final Element e = processingEnv.getTypeUtils().asElement(tm);
        final Model m = e == null ? null : e.getAnnotation(Model.class);
        
        String ret;
        if (m != null) {
            ret = findPkgName(e) + '.' + m.className();
            isModel[0] = true;
            models.put(e, m.className());
        } else if (findModelForMthd(e)) {
            ret = ((TypeElement)e).getQualifiedName().toString();
            isModel[0] = true;
        } else {
            ret = tm.toString();
        }
        TypeMirror enm = processingEnv.getElementUtils().getTypeElement("java.lang.Enum").asType();
        enm = processingEnv.getTypeUtils().erasure(enm);
        isEnum[0] = processingEnv.getTypeUtils().isSubtype(tm, enm);
        return ret;
    }
    
    private static boolean findModelForMthd(Element clazz) {
        if (clazz == null) {
            return false;
        }
        for (Element e : clazz.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD) {
                ExecutableElement ee = (ExecutableElement)e;
                if (
                    ee.getSimpleName().contentEquals("modelFor") &&
                    ee.getParameters().isEmpty()
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    private Iterable<String> findParamNames(Element e, String url, StringBuilder assembleURL) {
        List<String> params = new ArrayList<>();

        for (int pos = 0; ;) {
            int next = url.indexOf('{', pos);
            if (next == -1) {
                assembleURL.append('"')
                    .append(url.substring(pos))
                    .append('"');
                return params;
            }
            int close = url.indexOf('}', next);
            if (close == -1) {
                error("Unbalanced '{' and '}' in " + url, e);
                return params;
            }
            final String paramName = url.substring(next + 1, close);
            params.add(paramName);
            assembleURL.append('"')
                .append(url.substring(pos, next))
                .append("\" + ").append(paramName).append(" + ");
            pos = close + 1;
        }
    }

    private static Prprt findPrprt(Prprt[] properties, String propName) {
        for (Prprt p : properties) {
            if (propName.equals(p.name())) {
                return p;
            }
        }
        return null;
    }

    private boolean isPrimitive(String type) {
        return 
            "int".equals(type) ||
            "double".equals(type) ||
            "long".equals(type) ||
            "short".equals(type) ||
            "byte".equals(type) ||
            "float".equals(type);
    }

    private static Collection<String> findDerivedFrom(Map<String, Collection<String>> propsDeps, String derivedProp) {
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, Collection<String>> e : propsDeps.entrySet()) {
            if (e.getValue().contains(derivedProp)) {
                names.add(e.getKey());
            }
        }
        return names;
    }
    
    private Prprt[] createProps(Element e, Property[] arr) {
        Prprt[] ret = Prprt.wrap(processingEnv, e, arr);
        Prprt[] prev = verify.put(e, ret);
        if (prev != null) {
            error("Two sets of properties for ", e);
        }
        return ret;
    }
    
    private static class Prprt {
        private final Element e;
        private final AnnotationMirror tm;
        private final Property p;

        public Prprt(Element e, AnnotationMirror tm, Property p) {
            this.e = e;
            this.tm = tm;
            this.p = p;
        }
        
        String name() {
            return p.name();
        }
        
        boolean array() {
            return p.array();
        }

        String typeName(ProcessingEnvironment env) {
            try {
                return p.type().getName();
            } catch (IncompleteAnnotationException | AnnotationTypeMismatchException ex) {
                for (Object v : getAnnoValues(env)) {
                    String s = v.toString().replace(" ", "");
                    if (s.startsWith("type=") && s.endsWith(".class")) {
                        return s.substring(5, s.length() - 6);
                    }
                }
                throw ex;
            }
        }
        
        
        static Prprt[] wrap(ProcessingEnvironment pe, Element e, Property[] arr) {
            if (arr.length == 0) {
                return new Prprt[0];
            }
            
            if (e.getKind() != ElementKind.CLASS) {
                throw new IllegalStateException("" + e.getKind());
            }
            TypeElement te = (TypeElement)e;
            List<? extends AnnotationValue> val = null;
            for (AnnotationMirror an : te.getAnnotationMirrors()) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : an.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().contentEquals("properties")) {
                        val = (List)entry.getValue().getValue();
                        break;
                    }
                }
            }
            if (val == null || val.size() != arr.length) {
                pe.getMessager().printMessage(Diagnostic.Kind.ERROR, "" + val, e);
                return new Prprt[0];
            }
            Prprt[] ret = new Prprt[arr.length];
            BIG: for (int i = 0; i < ret.length; i++) {
                AnnotationMirror am = (AnnotationMirror)val.get(i).getValue();
                ret[i] = new Prprt(e, am, arr[i]);
                
            }
            return ret;
        }
        
        private List<? extends Object> getAnnoValues(ProcessingEnvironment pe) {
            try {
                Class<?> trees = Class.forName("com.sun.tools.javac.api.JavacTrees");
                Method m = trees.getMethod("instance", ProcessingEnvironment.class);
                Object instance = m.invoke(null, pe);
                m = instance.getClass().getMethod("getPath", Element.class, AnnotationMirror.class);
                Object path = m.invoke(instance, e, tm);
                m = path.getClass().getMethod("getLeaf");
                Object leaf = m.invoke(path);
                m = leaf.getClass().getMethod("getArguments");
                return (List)m.invoke(leaf);
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        }
    }
    
}