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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.apidesign.html.boot.spi.Fn;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class FnUtils {
    
    private FnUtils() {
    }
    
    /** Seeks for {@link JavaScriptBody} and {@link JavaScriptResource} annotations
     * in the bytecode and converts them into real code. Used by Maven plugin
     * postprocessing classes.
     * 
     * @param bytecode the original bytecode with javascript specific annotations
     * @param loader the loader to load resources (scripts and classes) when needed
     * @return the transformed bytecode
     * @since 0.7
     */
    public static byte[] transform(byte[] bytecode, ClassLoader loader) {
        ClassReader cr = new ClassReader(bytecode) {
            // to allow us to compile with -profile compact1 on 
            // JDK8 while processing the class as JDK7, the highest
            // class format asm 4.1 understands to
            @Override
            public short readShort(int index) {
                short s = super.readShort(index);
                if (index == 6 && s > Opcodes.V1_7) {
                    return Opcodes.V1_7;
                }
                return s;
            }
        };
        FindInClass tst = new FindInClass(loader, null);
        cr.accept(tst, 0);
        if (tst.found > 0) {
            ClassWriter w = new ClassWriterEx(loader, cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            FindInClass fic = new FindInClass(loader, w);
            cr.accept(fic, 0);
            bytecode = w.toByteArray();
        }
        return bytecode;
    }
    
    public static boolean isJavaScriptCapable(ClassLoader l) {
        if (l instanceof JsClassLoader) {
            return true;
        }
        if (l.getResource("META-INF/net.java.html.js.classes") != null) {
            return false;
        }
        return true;
    }
    
    public static boolean isValid(Fn fn) {
        return fn != null && fn.isValid();
    }

    public static ClassLoader newLoader(final FindResources f, final Fn.Presenter d, ClassLoader parent) {
        return new JsClassLoader(parent) {
            @Override
            protected URL findResource(String name) {
                List<URL> l = res(name, true);
                return l.isEmpty() ? null : l.get(0);
            }
            
            @Override
            protected Enumeration<URL> findResources(String name) {
                return Collections.enumeration(res(name, false));
            }
            
            private List<URL> res(String name, boolean oneIsEnough) {
                List<URL> l = new ArrayList<URL>();
                f.findResources(name, l, oneIsEnough);
                return l;
            }
            
            @Override
            protected Fn defineFn(String code, String... names) {
                return d.defineFn(code, names);
            }

            @Override
            protected void loadScript(Reader code) throws Exception {
                d.loadScript(code);
            }
        };
    }

    static String callback(final String body) {
        return new JsCallback() {
            @Override
            protected CharSequence callMethod(
                String ident, String fqn, String method, String params
            ) {
                StringBuilder sb = new StringBuilder();
                sb.append("vm.").append(mangle(fqn, method, params));
                sb.append("(");
                if (ident != null) {
                    sb.append(ident);
                }
                return sb;
            }

        }.parse(body);
    }

    static void loadScript(ClassLoader jcl, String resource) {
        final InputStream script = jcl.getResourceAsStream(resource);
        if (script == null) {
            throw new NullPointerException("Can't find " + resource);
        }
        try {
            Reader isr = null;
            try {
                isr = new InputStreamReader(script, "UTF-8");
                FnContext.currentPresenter(false).loadScript(isr);
            } finally {
                if (isr != null) {
                    isr.close();
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Can't execute " + resource, ex);
        } 
    }
    
    
    private static final class FindInClass extends ClassVisitor {
        private String name;
        private int found;
        private ClassLoader loader;
        private String resource;

        public FindInClass(ClassLoader l, ClassVisitor cv) {
            super(Opcodes.ASM4, cv);
            this.loader = l;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            final AnnotationVisitor del = super.visitAnnotation(desc, visible);
            if ("Lnet/java/html/js/JavaScriptResource;".equals(desc)) {
                return new LoadResource(del);
            }
            return del;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new FindInMethod(access, name, desc,
                    super.visitMethod(access & (~Opcodes.ACC_NATIVE), name, desc, signature, exceptions)
            );
        }

        private final class FindInMethod extends MethodVisitor {

            private final String name;
            private final String desc;
            private final int access;
            private FindInAnno fia;
            private boolean bodyGenerated;

            public FindInMethod(int access, String name, String desc, MethodVisitor mv) {
                super(Opcodes.ASM4, mv);
                this.access = access;
                this.name = name;
                this.desc = desc;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if ("Lnet/java/html/js/JavaScriptBody;".equals(desc)) { // NOI18N
                    found++;
                    return new FindInAnno();
                }
                return super.visitAnnotation(desc, visible);
            }

            private void generateJSBody(FindInAnno fia) {
                this.fia = fia;
            }

            @Override
            public void visitCode() {
                if (fia == null) {
                    return;
                }
                generateBody(true);
            }

            private boolean generateBody(boolean hasCode) {
                if (bodyGenerated) {
                    return false;
                }
                bodyGenerated = true;
                if (mv != null) {
                    AnnotationVisitor va = super.visitAnnotation("Lnet/java/html/js/JavaScriptBody;", false);
                    AnnotationVisitor varr = va.visitArray("args");
                    for (String argName : fia.args) {
                        varr.visit(null, argName);
                    }
                    varr.visitEnd();
                    va.visit("javacall", fia.javacall);
                    va.visit("body", fia.body);
                    va.visitEnd();
                }
                
                String body;
                List<String> args;
                if (fia.javacall) {
                    body = callback(fia.body);
                    args = new ArrayList<String>(fia.args);
                    args.add("vm");
                } else {
                    body = fia.body;
                    args = fia.args;
                }

                super.visitFieldInsn(
                        Opcodes.GETSTATIC, FindInClass.this.name,
                        "$$fn$$" + name + "_" + found,
                        "Lorg/apidesign/html/boot/spi/Fn;"
                );
                super.visitInsn(Opcodes.DUP);
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "org/apidesign/html/boot/spi/Fn", "isValid",
                        "(Lorg/apidesign/html/boot/spi/Fn;)Z"
                );
                Label ifNotNull = new Label();
                super.visitJumpInsn(Opcodes.IFNE, ifNotNull);

                // init Fn
                super.visitInsn(Opcodes.POP);
                super.visitLdcInsn(Type.getObjectType(FindInClass.this.name));
                super.visitLdcInsn(body);
                super.visitIntInsn(Opcodes.SIPUSH, args.size());
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
                boolean needsVM = false;
                for (int i = 0; i < args.size(); i++) {
                    assert !needsVM;
                    String argName = args.get(i);
                    needsVM = "vm".equals(argName);
                    super.visitInsn(Opcodes.DUP);
                    super.visitIntInsn(Opcodes.BIPUSH, i);
                    super.visitLdcInsn(argName);
                    super.visitInsn(Opcodes.AASTORE);
                }
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "org/apidesign/html/boot/spi/Fn", "define",
                        "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/String;)Lorg/apidesign/html/boot/spi/Fn;"
                );
                Label noPresenter = new Label();
                if (hasCode) {
                    super.visitInsn(Opcodes.DUP);
                    super.visitJumpInsn(Opcodes.IFNULL, noPresenter);
                }
                if (resource != null) {
                    super.visitLdcInsn(Type.getObjectType(FindInClass.this.name));
                    super.visitLdcInsn(resource);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "org/apidesign/html/boot/spi/Fn", "preload",
                            "(Lorg/apidesign/html/boot/spi/Fn;Ljava/lang/Class;Ljava/lang/String;)Lorg/apidesign/html/boot/spi/Fn;"
                    );
                }
                super.visitInsn(Opcodes.DUP);
                super.visitFieldInsn(
                        Opcodes.PUTSTATIC, FindInClass.this.name,
                        "$$fn$$" + name + "_" + found,
                        "Lorg/apidesign/html/boot/spi/Fn;"
                );
                // end of Fn init

                super.visitLabel(ifNotNull);

                final int offset;
                if ((access & Opcodes.ACC_STATIC) == 0) {
                    offset = 1;
                    super.visitIntInsn(Opcodes.ALOAD, 0);
                } else {
                    offset = 0;
                    super.visitInsn(Opcodes.ACONST_NULL);
                }

                super.visitIntInsn(Opcodes.SIPUSH, args.size());
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

                class SV extends SignatureVisitor {

                    private boolean nowReturn;
                    private Type returnType;
                    private int index;
                    private int loadIndex = offset;

                    public SV() {
                        super(Opcodes.ASM4);
                    }

                    @Override
                    public void visitBaseType(char descriptor) {
                        final Type t = Type.getType("" + descriptor);
                        if (nowReturn) {
                            returnType = t;
                            return;
                        }
                        FindInMethod.super.visitInsn(Opcodes.DUP);
                        FindInMethod.super.visitIntInsn(Opcodes.SIPUSH, index++);
                        FindInMethod.super.visitVarInsn(t.getOpcode(Opcodes.ILOAD), loadIndex++);
                        String factory;
                        switch (descriptor) {
                            case 'I':
                                factory = "java/lang/Integer";
                                break;
                            case 'J':
                                factory = "java/lang/Long";
                                loadIndex++;
                                break;
                            case 'S':
                                factory = "java/lang/Short";
                                break;
                            case 'F':
                                factory = "java/lang/Float";
                                break;
                            case 'D':
                                factory = "java/lang/Double";
                                loadIndex++;
                                break;
                            case 'Z':
                                factory = "java/lang/Boolean";
                                break;
                            case 'C':
                                factory = "java/lang/Character";
                                break;
                            case 'B':
                                factory = "java/lang/Byte";
                                break;
                            default:
                                throw new IllegalStateException(t.toString());
                        }
                        FindInMethod.super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                factory, "valueOf", "(" + descriptor + ")L" + factory + ";"
                        );
                        FindInMethod.super.visitInsn(Opcodes.AASTORE);
                    }

                    @Override
                    public SignatureVisitor visitArrayType() {
                        if (nowReturn) {
                            return new SignatureVisitor(Opcodes.ASM4) {
                                @Override
                                public void visitClassType(String name) {
                                    returnType = Type.getType("[" + Type.getObjectType(name).getDescriptor());
                                }
                            };
                        }
                        loadObject();
                        return new SignatureWriter();
                    }

                    @Override
                    public void visitClassType(String name) {
                        if (nowReturn) {
                            returnType = Type.getObjectType(name);
                            return;
                        }
                        loadObject();
                    }

                    @Override
                    public SignatureVisitor visitReturnType() {
                        nowReturn = true;
                        return this;
                    }

                    private void loadObject() {
                        FindInMethod.super.visitInsn(Opcodes.DUP);
                        FindInMethod.super.visitIntInsn(Opcodes.SIPUSH, index++);
                        FindInMethod.super.visitVarInsn(Opcodes.ALOAD, loadIndex++);
                        FindInMethod.super.visitInsn(Opcodes.AASTORE);
                    }

                }
                SV sv = new SV();
                SignatureReader sr = new SignatureReader(desc);
                sr.accept(sv);

                if (needsVM) {
                    FindInMethod.super.visitInsn(Opcodes.DUP);
                    FindInMethod.super.visitIntInsn(Opcodes.SIPUSH, sv.index);
                    int lastSlash = FindInClass.this.name.lastIndexOf('/');
                    String jsCallbacks = FindInClass.this.name.substring(0, lastSlash + 1) + "$JsCallbacks$";
                    FindInMethod.super.visitFieldInsn(Opcodes.GETSTATIC, jsCallbacks, "VM", "L" + jsCallbacks + ";");
                    FindInMethod.super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, jsCallbacks, "current", "()L" + jsCallbacks + ";");
                    FindInMethod.super.visitInsn(Opcodes.AASTORE);
                }

                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "org/apidesign/html/boot/spi/Fn", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"
                );
                switch (sv.returnType.getSort()) {
                    case Type.VOID:
                        super.visitInsn(Opcodes.RETURN);
                        break;
                    case Type.ARRAY:
                    case Type.OBJECT:
                        super.visitTypeInsn(Opcodes.CHECKCAST, sv.returnType.getInternalName());
                        super.visitInsn(Opcodes.ARETURN);
                        break;
                    case Type.BOOLEAN:
                        super.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                "java/lang/Boolean", "booleanValue", "()Z"
                        );
                        super.visitInsn(Opcodes.IRETURN);
                        break;
                    default:
                        super.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                "java/lang/Number", sv.returnType.getClassName() + "Value", "()" + sv.returnType.getDescriptor()
                        );
                        super.visitInsn(sv.returnType.getOpcode(Opcodes.IRETURN));
                }
                if (hasCode) {
                    super.visitLabel(noPresenter);
                    super.visitCode();
                }
                return true;
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if (fia != null) {
                    if (generateBody(false)) {
                        // native method
                        super.visitMaxs(1, 0);
                    }
                    FindInClass.this.visitField(
                            Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                            "$$fn$$" + name + "_" + found,
                            "Lorg/apidesign/html/boot/spi/Fn;",
                            null, null
                    );
                }
            }

            private final class FindInAnno extends AnnotationVisitor {

                List<String> args = new ArrayList<String>();
                String body;
                boolean javacall = false;

                public FindInAnno() {
                    super(Opcodes.ASM4);
                }

                @Override
                public void visit(String name, Object value) {
                    if (name == null) {
                        args.add((String) value);
                        return;
                    }
                    if (name.equals("javacall")) { // NOI18N
                        javacall = (Boolean) value;
                        return;
                    }
                    assert name.equals("body");
                    body = (String) value;
                }

                @Override
                public AnnotationVisitor visitArray(String name) {
                    return this;
                }

                @Override
                public void visitEnd() {
                    if (body != null) {
                        generateJSBody(this);
                    }
                }
            }
        }

        private final class LoadResource extends AnnotationVisitor {
            public LoadResource(AnnotationVisitor av) {
                super(Opcodes.ASM4, av);
            }

            @Override
            public void visit(String attrName, Object value) {
                super.visit(attrName, value);
                String relPath = (String) value;
                if (relPath.startsWith("/")) {
                    resource = relPath;
                } else {
                    int last = name.lastIndexOf('/');
                    String fullPath = name.substring(0, last + 1) + relPath;
                    resource = fullPath;
                }
            }
        }
    }

    private static class ClassWriterEx extends ClassWriter {

        private ClassLoader loader;

        public ClassWriterEx(ClassLoader l, ClassReader classReader, int flags) {
            super(classReader, flags);
            this.loader = l;
        }

        @Override
        protected String getCommonSuperClass(final String type1, final String type2) {
            Class<?> c, d;
            try {
                c = Class.forName(type1.replace('/', '.'), false, loader);
                d = Class.forName(type2.replace('/', '.'), false, loader);
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            } else {
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            }
        }
    }
}
