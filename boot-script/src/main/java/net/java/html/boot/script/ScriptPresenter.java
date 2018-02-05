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
package net.java.html.boot.script;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/** Implementation of {@link Presenter} that delegates
 * to Java {@link ScriptEngine scripting} API. The presenter runs headless
 * without appropriate simulation of browser APIs. Its primary usefulness
 * is inside testing environments. 
 * <p>
 * One can load in browser simulation for example from 
 * <a href="http://www.envjs.com/">env.js</a>. The best way to achieve so,
 * is to wait until JDK-8046013 gets fixed....
 * 
 *
 * @author Jaroslav Tulach
 */
final class ScriptPresenter implements Fn.KeepAlive,
Presenter, Fn.FromJavaScript, Fn.ToJavaScript, Executor {
    private static final Logger LOG = Logger.getLogger(ScriptPresenter.class.getName());
    private static final boolean JDK7;
    static {
        boolean jdk7;
        try {
            Class.forName("java.lang.FunctionalInterface");
            jdk7 = false;
        } catch (ClassNotFoundException ex) {
            jdk7 = true;
        }
        JDK7 = jdk7;
    }
    private final ScriptEngine eng;
    private final Executor exc;
    private final Object undefined;

    ScriptPresenter(Executor exc) {
        this(new ScriptEngineManager().getEngineByName("javascript"), exc);
    }

    ScriptPresenter(ScriptEngine eng, Executor exc) {
        this.eng = eng;
        this.exc = exc;
        try {
            eng.eval("function alert(msg) { Packages.java.lang.System.out.println(msg); };");
            eng.eval("function confirm(msg) { Packages.java.lang.System.out.println(msg); return true; };");
            eng.eval("function prompt(msg, txt) { Packages.java.lang.System.out.println(msg + ':' + txt); return txt; };");
            Object undef;
            if (JDK7) {
                undef = new JDK7Callback().undefined(eng);
            } else {
                undef = ((Object[])eng.eval("Java.to([undefined])"))[0];
            }
            this.undefined = undef;
        } catch (ScriptException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Fn defineFn(String code, String... names) {
        return defineImpl(code, names, null);
    }

    @Override
    public Fn defineFn(String code, String[] names, boolean[] keepAlive) {
        return defineImpl(code, names, keepAlive);
    }    
    private FnImpl defineImpl(String code, String[] names, boolean[] keepAlive) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function() {\n");
        sb.append("  return function(");
        String sep = "";
        if (names != null) for (String n : names) {
            sb.append(sep).append(n);
            sep = ",";
        }
        sb.append(") {\n");
        sb.append(code);
        sb.append("\n  };\n");
        sb.append("})()\n");

        final Object fn;
        try {
            fn = eng.eval(sb.toString());
        } catch (ScriptException ex) {
            throw new IllegalStateException(ex);
        }
        return new FnImpl(this, fn, keepAlive);
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        try {
            eng.eval("if (typeof window !== 'undefined') window.location = '" + page + "'");
        } catch (ScriptException ex) {
            LOG.log(Level.SEVERE, "Cannot load " + page, ex);
        }
        if (onPageLoad != null) {
            onPageLoad.run();
        }
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        eng.eval(code);
    }
    
    //
    // array conversions
    //
    
    final Object convertArrays(Object[] arr) throws Exception {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Object[]) {
                arr[i] = convertArrays((Object[]) arr[i]);
            }
        }
        final Object wrapArr = wrapArrFn().invokeImpl(null, false, arr); // NOI18N
        return wrapArr;
    }

    private FnImpl wrapArrImpl;
    private FnImpl wrapArrFn() {
        if (wrapArrImpl == null) {
            try {
                wrapArrImpl = defineImpl("return Array.prototype.slice.call(arguments);", null, null);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return wrapArrImpl;
    }

    final Object checkArray(Object val) throws Exception {
        if (val instanceof Boolean || val instanceof Number || val instanceof String) {
            return val;
        }
        final FnImpl fn = arraySizeFn();
        final Object fnRes = fn.invokeImpl(null, false, val, null);
        int length = ((Number) fnRes).intValue();
        if (length == -1) {
            return val;
        }
        Object[] arr = new Object[length];
        fn.invokeImpl(null, false, val, arr);
        return arr;
    }

    private FnImpl arraySize;
    private FnImpl arraySizeFn() {
        if (arraySize == null) {
            try {
                arraySize = defineImpl("\n"
                    + "if (to == null) {\n"
                    + "  if (Object.prototype.toString.call(arr) === '[object Array]') return arr.length;\n"
                    + "  else return -1;\n"
                    + "} else {\n"
                    + "  var l = arr.length;\n"
                    + "  for (var i = 0; i < l; i++) {\n"
                    + "    to[i] = arr[i] === undefined ? null : arr[i];\n"
                    + "  }\n"
                    + "  return l;\n"
                    + "}", new String[] { "arr", "to" }, null
                );
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return arraySize;
    }

    @Override
    public Object toJava(Object toJS) {
        if (toJS instanceof Weak) {
            toJS = ((Weak)toJS).get();
        }
        if (toJS == undefined) {
            return null;
        }
        try {
            return checkArray(toJS);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public Object toJavaScript(Object toReturn) {
        if (toReturn instanceof Object[]) {
            try {
                return convertArrays((Object[])toReturn);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            if (JDK7) {
                if (toReturn instanceof Boolean) {
                    return ((Boolean)toReturn) ? true : null;
                }
            }
            return toReturn;
        }
    }

    @Override
    public void execute(final Runnable command) {
        if (Fn.activePresenter() == this) {
            command.run();
            return;
        }
        
        class Wrap implements Runnable {
            public void run() {
                try (Closeable c = Fn.activate(ScriptPresenter.this)) {
                    command.run();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        final Runnable wrap = new Wrap();
        if (exc == null) {
            wrap.run();
        } else {
            exc.execute(wrap);
        }
    }

    private class FnImpl extends Fn {

        private final Object fn;
        private final boolean[] keepAlive;

        public FnImpl(Presenter presenter, Object fn, boolean[] keepAlive) {
            super(presenter);
            this.fn = fn;
            this.keepAlive = keepAlive;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return invokeImpl(thiz, true, args);
        }

            final Object invokeImpl(Object thiz, boolean arrayChecks, Object... args) throws Exception {
                List<Object> all = new ArrayList<>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                for (int i = 0; i < args.length; i++) {
                    Object conv = args[i];
                    if (arrayChecks) {
                        if (args[i] instanceof Object[]) {
                            Object[] arr = (Object[]) args[i];
                            conv = ((ScriptPresenter) presenter()).convertArrays(arr);
                        }
                        if (conv != null && keepAlive != null
                            && !keepAlive[i] && !isJSReady(conv)
                            && !conv.getClass().getSimpleName().equals("$JsCallbacks$") // NOI18N
                            ) {
                            conv = new Weak(conv);
                        }
                        if (conv instanceof Character) {
                            conv = (int)(Character)conv;
                        }
                    }
                    all.add(conv);
                }
                Object ret = ((Invocable)eng).invokeMethod(fn, "call", all.toArray()); // NOI18N
                if (ret instanceof Weak) {
                    ret = ((Weak)ret).get();
                }
                if (ret == fn) {
                    return null;
                }
                if (!arrayChecks) {
                    return ret;
                }
                return ((ScriptPresenter)presenter()).checkArray(ret);
            }
    }
    
    private static boolean isJSReady(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return true;
        }
        if (obj instanceof Number) {
            return true;
        }
        final String cn = obj.getClass().getName();
        if (
            cn.startsWith("com.oracle.truffle") || // NOI18N
            cn.startsWith("jdk.nashorn") || // NOI18N
            (cn.contains(".mozilla.") && cn.contains(".Native")) // NOI18N
        ) {
            return true;
        }
        if (obj instanceof Character) {
            return true;
        }
        return false;
    }    
    
    private static final class Weak extends WeakReference<Object> {
        public Weak(Object referent) {
            super(referent);
        }
    }

    private static final class JDK7Callback implements ObjectOutput {
        private Object undefined;

        @Override
        public void writeObject(Object obj) throws IOException {
            undefined = obj;
        }

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
        }

        @Override
        public void writeByte(int v) throws IOException {
        }

        @Override
        public void writeShort(int v) throws IOException {
        }

        @Override
        public void writeChar(int v) throws IOException {
        }

        @Override
        public void writeInt(int v) throws IOException {
        }

        @Override
        public void writeLong(long v) throws IOException {
        }

        @Override
        public void writeFloat(float v) throws IOException {
        }

        @Override
        public void writeDouble(double v) throws IOException {
        }

        @Override
        public void writeBytes(String s) throws IOException {
        }

        @Override
        public void writeChars(String s) throws IOException {
        }

        @Override
        public void writeUTF(String s) throws IOException {
        }

        public Object undefined(ScriptEngine eng) {
            try {
                eng.eval("function isJDK7Undefined(js) { js.writeObject(undefined); }");
                Invocable inv = (Invocable) eng;
                inv.invokeFunction("isJDK7Undefined", this);
            } catch (NoSuchMethodException | ScriptException ex) {
                throw new IllegalStateException(ex);
            }
            return undefined;
        }
    }

}
