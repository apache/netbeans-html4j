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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

/**
 *
 * @author Jaroslav Tulach
 */
final class Compile implements DiagnosticListener<JavaFileObject> {
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    private final Map<String, byte[]> classes;
    private final String pkg;
    private final String cls;
    private final String html;
    private final String sourceLevel;

    private Compile(String html, String code, String sl) throws IOException {
        this.pkg = findPkg(code);
        this.cls = findCls(code);
        this.html = html;
        this.sourceLevel = sl;
        classes = compile(html, code);
    }

    /** Performs compilation of given HTML page and associated Java code
     */
    public static Compile create(String html, String code) throws IOException {
        return create(html, code, "8");
    }
    static Compile create(String html, String code, String sourceLevel) throws IOException {
        return new Compile(html, code, sourceLevel);
    }
    
    /** Checks for given class among compiled resources */
    public byte[] get(String res) {
        return classes.get(res);
    }
    
    /** Obtains errors created during compilation.
     */
    public List<Diagnostic<? extends JavaFileObject>> getErrors() {
        return getDiagnostics(Diagnostic.Kind.ERROR);
    }
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics(Diagnostic.Kind kind) {
        List<Diagnostic<? extends JavaFileObject>> err = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : errors) {
            if (diagnostic.getKind() == kind) {
                err.add(diagnostic);
            }
        }
        return err;
    }
    
    private Map<String, byte[]> compile(final String html, final String code) throws IOException {
        StandardJavaFileManager sjfm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(this, null, null);

        final Map<String, ByteArrayOutputStream> class2BAOS = new HashMap<>();

        JavaFileObject file = new SimpleJavaFileObject(URI.create("mem://mem"), Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return code;
            }
        };
        final JavaFileObject htmlFile = new SimpleJavaFileObject(URI.create("mem://mem2"), Kind.OTHER) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return html;
            }

            @Override
            public InputStream openInputStream() throws IOException {
                return new ByteArrayInputStream(html.getBytes());
            }
        };
        
        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(sjfm) {
            @Override
            public FileObject getFileForOutput(JavaFileManager.Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
                try {
                    return new VirtFO(new URI("mem://resource/" + relativeName), Kind.OTHER, relativeName);
                } catch (URISyntaxException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            
            
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
                if (kind  == Kind.CLASS) {
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    class2BAOS.put(className.replace('.', '/') + ".class", buffer);
                    return new SimpleJavaFileObject(sibling.toUri(), kind) {
                        @Override
                        public OutputStream openOutputStream() throws IOException {
                            return buffer;
                        }
                    };
                }
                
                if (kind == Kind.SOURCE) {
                    final String n = className.replace('.', '/') + ".java";
                    final URI un;
                    try {
                        un = new URI("mem://" + n);
                    } catch (URISyntaxException ex) {
                        throw new IOException(ex);
                    }
                    return new VirtFO(un/*sibling.toUri()*/, kind, n);
                }
                
                throw new IllegalStateException();
            }

            @Override
            public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
                if (location == StandardLocation.SOURCE_PATH) {
                    if (packageName.equals(pkg)) {
                        return htmlFile;
                    }
                    if (packageName.isEmpty() && relativeName.startsWith(pkg.replace('.', '/'))) {
                        return htmlFile;
                    }
                }
                
                return null;
            }

            @Override
            public boolean isSameFile(FileObject a, FileObject b) {
                if (a instanceof VirtFO && b instanceof VirtFO) {
                    return ((VirtFO)a).getName().equals(((VirtFO)b).getName());
                }
                
                return super.isSameFile(a, b);
            }

            class VirtFO extends SimpleJavaFileObject {

                private final String n;

                public VirtFO(URI uri, Kind kind, String n) {
                    super(uri, kind);
                    this.n = n;
                }
                private final ByteArrayOutputStream data = new ByteArrayOutputStream();

                @Override
                public OutputStream openOutputStream() throws IOException {
                    return data;
                }

                @Override
                public String getName() {
                    return n;
                }

                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                    data.close();
                    return new String(data.toByteArray());
                }
            }
        };

        ToolProvider.getSystemJavaCompiler().getTask(null, jfm, this, /*XXX:*/Arrays.asList("-source", sourceLevel, "-target", sourceLevel), null, Arrays.asList(file)).call();

        Map<String, byte[]> result = new HashMap<>();

        for (Map.Entry<String, ByteArrayOutputStream> e : class2BAOS.entrySet()) {
            result.put(e.getKey(), e.getValue().toByteArray());
        }

        return result;
    }


    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        errors.add(diagnostic);
    }
    private static String findPkg(String java) throws IOException {
        Pattern p = Pattern.compile("package\\p{javaWhitespace}*([\\p{Alnum}\\.]+)\\p{javaWhitespace}*;", Pattern.MULTILINE);
        Matcher m = p.matcher(java);
        if (!m.find()) {
            throw new IOException("Can't find package declaration in the java file");
        }
        String pkg = m.group(1);
        return pkg;
    }
    private static String findCls(String java) throws IOException {
        Pattern p = Pattern.compile("class\\p{javaWhitespace}*([\\p{Alnum}\\.]+)\\p{javaWhitespace}", Pattern.MULTILINE);
        Matcher m = p.matcher(java);
        if (!m.find()) {
            throw new IOException("Can't find package declaration in the java file");
        }
        String cls = m.group(1);
        return cls;
    }

    String getHtml() {
        String fqn = "'" + pkg + '.' + cls + "'";
        return html.replace("'${fqn}'", fqn);
    }
    void assertErrors() {
        assertFalse(getErrors().isEmpty(), "There are supposed to be some errors");
    }

    void assertError(String expMsg) {
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find ").append(expMsg).append(" among:");
        for (Diagnostic<? extends JavaFileObject> e : errors) {
            String msg = e.getMessage(Locale.US);
            if (msg.contains(expMsg)) {
                return;
            }
            sb.append("\n");
            sb.append(msg);
        }
        fail(sb.toString());
    }

    void assertNoErrors() {
        assertTrue(getErrors().isEmpty(), "No errors expected: " + getErrors());
    }
}
