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
package org.netbeans.html.presenters.webkit;

import org.netbeans.html.presenters.render.JSC;
import org.netbeans.html.presenters.render.Show;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.io.Reader;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.html.boot.spi.Fn;
import org.openide.util.lookup.ServiceProvider;
import com.dukescript.api.strings.Texts;
import net.java.html.boot.BrowserBuilder;

/** Displays using native WebKit component on Linux and Mac OS X.
 * Requires necessary native libraries to be installed. Uses GTK3 on
 * Linux which may not co-exist well with GTK2 (used by JDK's default AWT
 * toolkit).
 */
@ServiceProvider(service = Fn.Presenter.class)
public final class WebKitPresenter implements Fn.Presenter, Fn.KeepAlive, Executor {
    private static final Logger LOG = Logger.getLogger(WebKitPresenter.class.getName());
    private final Show shell;
    private Runnable onPageLoad;
    private OnFinalize onFinalize;
    private Pointer ctx;
    private Pointer javaClazz;
    private final Map<Object,Object> toJava = new HashMap<>();
    private Pointer arrayLength;
    private Pointer valueTrue;
    private Pointer valueFalse;
    private String onPageApp;

    /** Default constructor. Rather than dealing with this class directly,
     * consider using it via {@link BrowserBuilder} API.
     */
    public WebKitPresenter() {
        this(false);
    }

    /** Visible or invisible presenter. This constructor allows one to
     * launch the presenter in headless mode.
     *
     * {@codesnippet org.netbeans.html.presenters.webkit.GtkJavaScriptTest}
     *
     * @param headless {@code true} if the presenter shall run headless
     */
    public WebKitPresenter(boolean headless) {
        shell = Show.open(this, new Runnable() {
            @Override
            public void run() {
                onPageLoad.run();
            }
        }, new Runnable() {
            @Override
            public void run() {
                jsContext(shell.jsContext());
            }
        }, headless);
    }
    
    @Override
    public Fn defineFn(String code, String... names) {
        return defineFn(code, names, null);
    }
    @Override
    public Fn defineFn(String code, String[] names, boolean[] keepAlive) {
        JSC jsc = shell.jsc();
        Pointer[] jsNames = new Pointer[names.length];
        for (int i = 0; i < jsNames.length; i++) {
            jsNames[i] = jsc.JSStringCreateWithUTF8CString(names[i]);
        }
        Pointer jsCode = jsc.JSStringCreateWithUTF8CString(code);
        PointerByReference exc = new PointerByReference();
        Pointer fn = jsc.JSObjectMakeFunction(ctx, null, names.length, jsNames, jsCode, null, 1, exc);
        if (fn == null) {
            throw new IllegalStateException("Cannot initialize function: " + exc.getValue());
        }
        
        jsc.JSStringRelease(jsCode);
        for (Pointer jsName : jsNames) {
            jsc.JSStringRelease(jsName);
        }
        return new JSCFn(fn, keepAlive);
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        this.onPageLoad = onPageLoad;
        this.onPageApp = findCalleeClassName();
        try {
            if ("jar".equals(page.getProtocol())) {
                page = UnJarResources.extract(page);
            }

            shell.show(page.toURI());
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, onPageApp, t);
        }
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            int ch = code.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        Pointer script = shell.jsc().JSStringCreateWithUTF8CString(sb.toString());
        PointerByReference ex = new PointerByReference();
        shell.jsc().JSEvaluateScript(ctx, script, null, null, 1, ex);
        shell.jsc().JSStringRelease(script);
        if (ex.getValue() != null) {
            throw new Exception(convertToString(shell.jsc(), ex.getValue()));
        }
    }
    
    Pointer[] convertFromJava(Object... args) throws Exception {
        return convertFromJava(args, null);
    }
    Pointer[] convertFromJava(Object[] args, boolean[] keepAlive) throws Exception {
        JSC jsc = shell.jsc();
        Pointer[] arr = new Pointer[args.length];
        for (int i = 0; i < arr.length; i++) {
            Object v = args[i];
            if (v == null) {
                v = jsc.JSValueMakeNull(ctx);
            } else if (v instanceof Number) {
                v = jsc.JSValueMakeNumber(ctx, ((Number)v).doubleValue());
            } else if (v instanceof Boolean) {
                v = ((Boolean)v) ? valueTrue : valueFalse;
            } else if (v instanceof String) {
                Pointer str = jsc.JSStringCreateWithUTF8CString((String)v);
                v = jsc.JSValueMakeString(ctx, str);
                jsc.JSStringRelease(str);
            } else if (v instanceof Enum) {
                Pointer str = jsc.JSStringCreateWithUTF8CString(((Enum)v).name());
                v = jsc.JSValueMakeString(ctx, str);
                jsc.JSStringRelease(str);
            } else if (v instanceof Character) {
                v = jsc.JSValueMakeNumber(ctx, (Character)v);
            } else if (v instanceof JSObject) {
                v = ((JSObject)v).value;
            } else if (v instanceof int[]) {
                int[] numbers = (int[])v;
                Pointer[] content = new Pointer[numbers.length];
                for (int j = 0; j < content.length; j++) {
                    content[j] = jsc.JSValueMakeNumber(ctx, numbers[j]);
                }
                v = jsc.JSObjectMakeArray(ctx, content.length, content, null);
            } else if (v instanceof double[]) {
                double[] numbers = (double[])v;
                Pointer[] content = new Pointer[numbers.length];
                for (int j = 0; j < content.length; j++) {
                    content[j] = jsc.JSValueMakeNumber(ctx, numbers[j]);
                }
                v = jsc.JSObjectMakeArray(ctx, content.length, content, null);
            } else if (v instanceof Object[]) {
                Pointer[] content = convertFromJava((Object[])v);
                v = jsc.JSObjectMakeArray(ctx, content.length, content, null);
            } else if (v.getClass().isArray()) {
                int len = Array.getLength(v);
                Object[] boxed = new Object[len];
                for (int j = 0; j < len; j++) {
                    boxed[j] = Array.get(v, j);
                }
                Pointer[] content = convertFromJava(boxed);
                v = jsc.JSObjectMakeArray(ctx, content.length, content, null);
            } else if (v.getClass().getSimpleName().equals("$JsCallbacks$")) {
                Pointer vm = jsc.JSObjectMake(ctx, null, null);
                for (Method method : v.getClass().getMethods()) {
                    if (method.getDeclaringClass() != v.getClass()) {
                        continue;
                    }
                    Pointer name = jsc.JSStringCreateWithUTF8CString(method.getName());
                    FnCallback fnC = new FnCallback(v, method);
                    toJava.put(fnC, fnC);
                    Pointer fn = jsc.JSObjectMakeFunctionWithCallback(ctx, null, fnC);
                    jsc.JSObjectSetProperty(ctx, vm, name, fn, 0, null);
                    jsc.JSStringRelease(name);
                }
                v = vm;
            } else {
                Pointer p = jsc.JSObjectMake(ctx, javaClazz, null);
                if (keepAlive == null || keepAlive[i]) { 
                    toJava.put(p, v);
                } else {
                    toJava.put(p, new WeakVal(v));
                }
                protect(v, p);
                v = p;
            }
            arr[i] = (Pointer) v;
        }
        return arr;
    }
    
    final String convertToString(JSC jsc, Pointer value) {
        int type = jsc.JSValueGetType(ctx, value);
        if (type == 5) {
            Pointer toStr = jsc.JSStringCreateWithUTF8CString("this.toString()");
            value = jsc.JSEvaluateScript(ctx, toStr, value, null, 0, null);
            jsc.JSStringRelease(toStr);
        }
        Object ret = convertToJava(jsc, String.class, value);
        return ret != null ? ret.toString() : "<null value>";
    }
    
    final Object convertToJava(JSC jsc, Class<?> expectedType, Pointer value) throws IllegalStateException {
        int type = jsc.JSValueGetType(ctx, value);
        /*
        typedef enum {
        kJSTypeUndefined,
        kJSTypeNull,
        kJSTypeBoolean,
        kJSTypeNumber,
        kJSTypeString,
        kJSTypeObject
        } JSType;
        */
        switch (type) {
            case 0: 
            case 1:
                return null;
            case 2: {
                double probability = jsc.JSValueToNumber(ctx, value, null);
                if (expectedType == boolean.class) {
                    expectedType = Boolean.class;
                }
                return expectedType.cast(probability >= 0.5);
            }
            case 3: {
                Double ret = jsc.JSValueToNumber(ctx, value, null);
                if (expectedType.isInstance(ret) || expectedType == double.class) {
                    return ret;
                }
                if (expectedType == Integer.class || expectedType == int.class) {
                    return ret.intValue();
                }
                if (expectedType == Float.class || expectedType == float.class) {
                    return ret.floatValue();
                }
                if (expectedType == Long.class || expectedType == long.class) {
                    return ret.longValue();
                }
                if (expectedType == Short.class || expectedType == short.class) {
                    return ret.shortValue();
                }
                if (expectedType == Byte.class || expectedType == byte.class) {
                    return ret.byteValue();
                }
                if (expectedType == Character.class || expectedType == char.class) {
                    return (char)ret.intValue();
                }
                throw new ClassCastException("Cannot convert double to " + expectedType);
            }
            case 4: {
                Pointer val = jsc.JSValueToStringCopy(ctx, value, null);
                int max = jsc.JSStringGetMaximumUTF8CStringSize(val);
                Memory mem = new Memory(max);
                jsc.JSStringGetUTF8CString(val, mem, max);
                return expectedType.cast(mem.getString(0));
            }
            case 5: {
                Object ret;
                if (isJavaClazz(value)) {
                    ret = toJava.get(value);
                    if (ret instanceof WeakVal) {
                        ret = ((WeakVal)ret).get();
                    }
                } else {
                    PointerByReference ex = new PointerByReference();
                    Pointer checkArray = jsc.JSObjectCallAsFunction(ctx, arrayLength, null, 1, new Pointer[] { value }, ex);
                    if (checkArray == null) {
                        throw new RuntimeException(convertToString(jsc, ex.getValue()));
                    }
                    int len = (int)jsc.JSValueToNumber(ctx, checkArray, null);
                    if (len >= 0) {
                        Object[] arr = new Object[len];
                        for (int i = 0; i < len; i++) {
                            Pointer val = jsc.JSObjectGetPropertyAtIndex(ctx, value, i, null);
                            arr[i] = convertToJava(jsc, Object.class, val);
                        }
                        return arr;
                    }
                    ret = new JSObject(this, value);
                }
                return expectedType.cast(ret);
            }
            default:
                throw new IllegalStateException("Uknown type: " + type);
        }
    }

    @Override
    public void execute(Runnable command) {
        shell.execute(command);
    }

    final void jsContext(Pointer ctx) {
        this.ctx = ctx;

        JSC jsc = shell.jsc();
        onFinalize = new WebKitPresenter.OnFinalize();
        javaClazz = jsc.JSClassCreate(new JSC.JSClassDefinition(onFinalize));

        boolean testInstance = false;
        assert testInstance = true;
        if (testInstance) {
            Pointer testObj = jsc.JSObjectMake(ctx, javaClazz, null);
            assert isJavaClazz(testObj) : "Own classes has to be recognized";
        }

        {
            Pointer jsGlobal = ctx;
            Pointer arrArg = jsc.JSStringCreateWithUTF8CString("x");
            Pointer arrT = jsc.JSStringCreateWithUTF8CString("var res = x.constructor === Array ? x.length : -1; return res;");
            Pointer arrFn = jsc.JSObjectMakeFunction(jsGlobal, null, 1, new Pointer[]{arrArg}, arrT, null, 0, null);
            arrayLength = arrFn;
            jsc.JSValueProtect(ctx, arrFn);
            assert !isJavaClazz(arrayLength) : "functions aren't Java classes";
        }
        {
            Pointer trueScr = jsc.JSStringCreateWithUTF8CString("true");
            valueTrue = jsc.JSEvaluateScript(ctx, trueScr, null, null, 1, null);
            jsc.JSStringRelease(trueScr);
            jsc.JSValueProtect(ctx, valueTrue);
            int vT = jsc.JSValueGetType(ctx, valueTrue);
            assert vT == 2;
            assert !isJavaClazz(valueTrue) : "true isn't Java class";
        }
        {
            Pointer falseScr = jsc.JSStringCreateWithUTF8CString("false");
            valueFalse = jsc.JSEvaluateScript(ctx, falseScr, null, null, 1, null);
            jsc.JSValueProtect(ctx, valueFalse);
            jsc.JSStringRelease(falseScr);
            int vF = jsc.JSValueGetType(ctx, valueFalse);
            assert vF == 2;
            assert !isJavaClazz(valueFalse) : "false isn't Java class";
        }
    }

    private boolean isJavaClazz(Pointer obj) {
        final int ret = shell.jsc().JSValueIsObjectOfClass(ctx, obj, javaClazz);
        return ret == 1;
    }


    @Texts({
        "version=$version"
    })
    final void onPageLoad() {
        final String who = "WebKitPresenter:" + shell.getClass().getSimpleName();
        onPageLoad.run();
    }
    
    private static final class JSObject {
        private final Pointer value;

        public JSObject(WebKitPresenter p, Pointer val) {
            this.value = val;
            p.protect(this, val);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof JSObject && value.equals(((JSObject)other).value);
        }
    }
    
    private static final ReferenceQueue<? super Object> QUEUE = new ReferenceQueue<Object>();
    private static final Set<Protector> ALL = new HashSet<>();
    private void protect(Object obj, Pointer pointer) {
        JSC jsc = shell.jsc();
        jsc.JSValueProtect(ctx, pointer);
        ALL.add(new Protector(obj, pointer));
        cleanProtected();
    }

    private void cleanProtected() {
        for (;;) {
            Protector p = (Protector)QUEUE.poll();
            if (p == null) {
                break;
            }
            ALL.remove(p);
            p.unprotect();
        }
    }
    private final class Protector extends PhantomReference<Object> {
        private final Pointer pointer;
        
        public Protector(Object referent, Pointer p) {
            super(referent, QUEUE);
            this.pointer = p;
        }

        public void unprotect() {
            JSC jsc = shell.jsc();
            jsc.JSValueUnprotect(ctx, pointer);
        }
    }
        
    private final class JSCFn extends Fn {
        private final Pointer fn;
        private final boolean[] keepAlive;
        
        public JSCFn(Pointer fn, boolean[] keepAlive) {
            this.fn = fn;
            this.keepAlive = keepAlive;
            protect(this, fn);
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            cleanProtected();
            JSC jsc = shell.jsc();
            Pointer[] arr = convertFromJava(args, keepAlive);
            Pointer jsThis = thiz == null ? null : convertFromJava(thiz)[0];
            PointerByReference exception = new PointerByReference();
            Pointer ret = jsc.JSObjectCallAsFunction(ctx, fn, jsThis, arr.length, arr, exception);
            if (exception.getValue() != null) {
                throw new Exception(convertToString(jsc, exception.getValue()));
            }
            
            return convertToJava(jsc, Object.class, ret);
        }
    }

    
    public final class FnCallback implements Callback {
        private final Object vm;
        private final Method method;

        public FnCallback(Object vm, Method method) {
            this.vm = vm;
            this.method = method;
        }
        
        public Pointer call(
            Pointer jsContextRef, Pointer jsFunction, Pointer thisObject,
            int argumentCount, PointerByReference ref, Pointer exception
        ) throws Exception {
            JSC jsc = shell.jsc();
            int size = Native.getNativeSize(Pointer.class);
            Object[] args = new Object[argumentCount];
            for (int i = 0, offset = 0; i < argumentCount; i++, offset += size) {
                args[i] = convertToJava(jsc, method.getParameterTypes()[i], ref.getPointer().getPointer(offset));
            }
            return convertFromJava(method.invoke(vm, args))[0];
        }
    }
    
    private final class OnFinalize implements Callback {
        public void callback(Pointer obj) {
            java.util.Iterator<Map.Entry<Object,Object>> it = toJava.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object,Object> entry = it.next();
                if (entry.getValue() == obj) {
                    it.remove();
                    break;
                }
            }
        }
    }
    
    private static final class WeakVal extends WeakReference<Object> {
        public WeakVal(Object referent) {
            super(referent);
        }
    }

    private static String findCalleeClassName() {
        StackTraceElement[] frames = new Exception().getStackTrace();
        for (StackTraceElement e : frames) {
            String cn = e.getClassName();
            if (cn.startsWith("com.dukescript.presenters.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("org.netbeans.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("net.java.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("java.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("javafx.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("com.sun.")) { // NOI18N
                continue;
            }
            return cn;
        }
        return "org.netbeans.html"; // NOI18N
    }
}
