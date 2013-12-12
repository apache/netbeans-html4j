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

import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
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
public final class FnUtils implements Fn.Presenter {
    
    private FnUtils() {
    }
    
    public static boolean isJavaScriptCapable(ClassLoader l) {
        if (l instanceof JsClassLoader) {
            return true;
        }
        Class<?> clazz;
        try (Closeable c = Fn.activate(new FnUtils())) {
            clazz = Class.forName(Test.class.getName(), true, l);
            final Object is = ((Callable<?>)clazz.newInstance()).call();
            return Boolean.TRUE.equals(is);
        } catch (Exception ex) {
            return false;
        }
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
                FnContext.currentPresenter().loadScript(isr);
            } finally {
                if (isr != null) {
                    isr.close();
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Can't execute " + resource, ex);
        } 
    }

    @Override
    public Fn defineFn(String code, String... names) {
        return new TrueFn();
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
    }

    @Override
    public void loadScript(Reader code) throws Exception {
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
            if ("Lnet/java/html/js/JavaScriptResource;".equals(desc)) {
                return new LoadResource();
            }
            return super.visitAnnotation(desc, visible);
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
            private List<String> args;
            private String body;
            private boolean bodyGenerated;

            public FindInMethod(int access, String name, String desc, MethodVisitor mv) {
                super(Opcodes.ASM4, mv);
                this.access = access;
                this.name = name;
                this.desc = desc;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if ("Lnet/java/html/js/JavaScriptBody;".equals(desc) // NOI18N
                        || "Lorg/apidesign/bck2brwsr/core/JavaScriptBody;".equals(desc) // NOI18N
                        ) {
                    found++;
                    return new FindInAnno();
                }
                return super.visitAnnotation(desc, visible);
            }

            private void generateJSBody(List<String> args, String body) {
                this.args = args;
                this.body = body;
            }

            @Override
            public void visitCode() {
                if (body == null) {
                    return;
                }
                generateBody();
            }

            private boolean generateBody() {
                if (bodyGenerated) {
                    return false;
                }
                bodyGenerated = true;

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
                            throw new IllegalStateException("Not supported yet");
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
                return true;
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if (body != null) {
                    if (generateBody()) {
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

                private List<String> args = new ArrayList<String>();
                private String body;
                private boolean javacall = false;

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
                        if (javacall) {
                            body = callback(body);
                            args.add("vm");
                        }
                        generateJSBody(args, body);
                    }
                }
            }
        }

        private final class LoadResource extends AnnotationVisitor {

            public LoadResource() {
                super(Opcodes.ASM4);
            }

            @Override
            public void visit(String attrName, Object value) {
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

    static byte[] transform(ClassLoader loader, byte[] arr) {
        ClassReader cr = new ClassReader(arr) {
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
            arr = w.toByteArray();
        }
        return arr;
    }

    private static final class TrueFn extends Fn {
        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return Boolean.TRUE;
        }
    } // end of TrueFn
}
