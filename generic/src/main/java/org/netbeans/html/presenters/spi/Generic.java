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
package org.netbeans.html.presenters.spi;

import com.dukescript.api.strings.Texts;
import java.io.Flushable;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.html.boot.spi.Fn;

abstract class Generic implements Fn.Presenter, Fn.KeepAlive, Flushable {
    private StringBuilder msg;
    /** @GuardedBy("this") */
    private int callCounter;
    /** @GuardedBy("this") */
    private Item call;
    private final NavigableSet<Exported> exported;
    private final int key;
    private final boolean synchronous;
    private final boolean evalJS;
    private final String type;
    private final String app;
    private final CountDownLatch initialized = new CountDownLatch(1);
    
    Generic(
        boolean synchronous, boolean evalJS, String type, String app
    ) {
        this.exported = new TreeSet<Exported>();
        this.key = (int)(System.currentTimeMillis() / 777) % 1000;
        this.synchronous = synchronous;
        this.evalJS = evalJS;
        this.type = type;
        this.app = app;
    }
    
    final Object lock() {
        return initialized;
    }
    
    final void log(Level level, String msg, Object... args) {
        StringBuilder sb = this.msg;
        if (sb != null) {
            for (int i = 0; i < args.length; i++) {
                String txt = args[i] == null ? "null" : args[i].toString();
                msg = msg.replace("{" + i + "}", txt);
            }
            synchronized (lock()) {
                sb.append('[').append(level).append("] ");
                sb.append(msg);
                sb.append('\n');
            }
        }
        handleLog(level, msg, args);
    }
    abstract void handleLog(Level level, String msg, Object... args);
    
    @Texts({
        """
        begin=try {
          @1('r', -1, 'OK', 'Connected', null);
        } catch (e) {
          console.warn(e);
        }
        """, """
             init=(function(global) {
               var fncns = new Array();
               var js2j = new Array();
               function jobject(id,value) {
                 Object.defineProperty(this, 'id', { value : id });
                 Object.defineProperty(this, 'v', { value : value });
                 return this;
               };
               Object.defineProperty(jobject.prototype, 'native', { value : true });
               Object.defineProperty(jobject.prototype, 'valueOf', { value : function() { return this.v ? this.v : '[jobject ' + this.id + ']'; } });
               Object.defineProperty(jobject.prototype, 'toString', { value : jobject.prototype.valueOf });
               var toVM = global['@2'];
               delete global['@2'];
               if (typeof toVM !== 'function') {
                 throw 'toVM should be a function: ' + toVM;
               }
               function toJava(method, id, r) {
                   var t = typeof r;
                   if (t === 'function') t = 'object';
                   if (t === 'undefined' || r === null) {
                     t = 'null';
                     r = null;
                   } else if (t === 'object') {
                     if (r['native']) {
                       t = 'java';
                       r = r.id;
                     } else if (Object.prototype.toString.call(r) === '[object Array]') {
                     t = 'array';
                     var l = r.length + ':';
                     for (var i = 0; i < r.length; i++) {
                         var toObj = toJava(null, id, r[i]);
                         l += toObj.length + ':' + toObj;
                       }
                       r = l;
                     } else {
                       var size = js2j.length;
                       js2j.push(r);
                       r = size;
                     }
                   }
                   if (method !== null) toVM(method, id, t, r, null);
                   else return t + ':' + r;
               }
               var impl = {};
               impl.key = @1;
               global.ds = function(key) {
                 if (key != impl.key) {
                   impl = null;
                   console.warn('Surprising access to Java with ' + key);
                 }
                 return impl;
               };
               impl.toJava = toJava;
               impl.rg = function(id, fn) {
                 fncns[id] = fn;
               };
               impl.fn = function(index, n, id, self) {
                 var args = Array.prototype.slice.call(arguments, 4);
                 try {
                   var fn = fncns[index];
                   if (typeof fn !== 'function') throw 'Cannot find function at index: ' + index + ' in ' + fn + ' apply: ' + (fn ? fn.apply : undefined);
                   var r = fn.apply(self, args);
                   if (n) toJava('r', id, r);
                 } catch (err) {
                   if (typeof console !== 'undefined') console.warn('Error ' + err + ' at:\\n' + err.stack);
                   if (n) toVM('r', id, 'error', '' + err + ' at:\\n' + err.stack, null, null);
                 }
               };
               impl.o = function(i) {
                 return js2j[i];
               };
               impl.j = function(n,v) {
                var r = new jobject(n,v);
                if (arguments.length > 2) {
                  for (var i = 2; i < arguments.length; i++) {
                    r[i - 2] = arguments[i];
                  }
                  r.length = arguments.length - 2;
                }
                return r;
               };
               impl.v = function(i) {
                 return fncns[i];
               };
               impl.toVM = toVM;
               impl.toVM('r', -1, 'OK', 'Initialized', null);
             })(this);""",
        "initializationProtocol=--- Initialization protocol ---\n",
        "error=Cannot initialize DukeScript: @1",
        "version=$version"
    })
    final void init() {
        if (initialized.getCount() == 0) {
            return;
        }
        synchronized (lock()) {
            if (initialized.getCount() == 0) {
                return;
            }
            if (msg == null) {
                this.msg = new StringBuilder(Strings.initializationProtocol());
                callbackFn(new ProtoPresenterBuilder.OnPrepared() {
                    @Override
                    public void callbackIsPrepared(String clbk) {
                        log(Level.FINE, "callbackReady with {0}", clbk);
                        loadJS(Strings.begin(clbk).toString());
                        log(Level.FINE, "checking OK state");
                        loadJS(Strings.init(key, clbk).toString());
                    }
                });
            }
        }
        for (int counter = 0;; counter++) {
            try {
                handleLog(Level.FINE, "Awaiting as of {0}", counter);
                if (initialized.await(10, TimeUnit.SECONDS)) {
                    handleLog(Level.FINE, "Waiting is over");
                    return;
                }
                handleLog(Level.INFO, msg.toString());
            } catch (InterruptedException ex) {
                handleLog(Level.INFO, "Interrupt", ex);
            }
        }
    }

    /** @return the name of the callback function */
    abstract void callbackFn(ProtoPresenterBuilder.OnPrepared onReady);
    abstract void loadJS(String js);
    
    public final String js2java(String method,
        String a1, String a2, String a3, String a4
    ) throws Exception {
        if ("r".equals(method)) {
            result(a1, a2, a3);
            return null;
        } else if ("c".equals(method)) {
            return javacall(a1, a2, a3, a4);
        } else if ("jr".equals(method)) {
            return javaresult();
        } else {
            throw new IllegalArgumentException(method);
        }
    }
    
    abstract void dispatch(Runnable r);

    /** Makes sure all pending calls into JavaScript are immediately 
     * performed. 
     * 
     * @throws IOException if something goes wrong
     */
    @Override
    public void flush() throws IOException {
        if (initialized.getCount() == 0) {
            flushImpl();
        }
    }
    
    @Override
    public Fn defineFn(String code, String[] names, boolean[] keepAlive) {
        init();
        return new GFn(code, names, keepAlive);
    }    
    
    @Override
    public Fn defineFn(String code, String... names) {
        init();
        return new GFn(code, names, null);
    }
    
    private static final class Key extends WeakReference<Object> {
        private int hash;
        
        public Key(Object obj) {
            super(obj);
            this.hash = System.identityHashCode(obj);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + this.hash;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key other = (Key)obj;
                if (hash != other.hash) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }
    
    private Map<Key,Integer> ids = new HashMap<Key, Integer>();
    int identityHashCode(Object o) {
        Key k = new Key(o);
        Integer val = ids.get(k);
        if (val == null) {
            int s = ids.size();
            ids.put(k, s);
            return s;
        }
        return val;
    }
    
    final int registerObject(Object o, boolean weak, boolean[] justAdded, String[] valueOf) {
        if (o instanceof Enum && valueOf != null) {
            valueOf[0] = o.toString();
        }
        int id = identityHashCode(o);
        for (;;) {
            Object exp = findObject(id);
            if (o == exp) {
                return id;
            }
            if (exp == null) {
                if (justAdded != null) {
                    justAdded[0] = true;
                }
                exported.add(new Exported(id, weak, o));
                return id;
            }
            throw new IllegalStateException("Collision!");
        }
    }
    
    final Object findObject(int id) {
        Exported obj = exported.floor(new Exported(id, false, null));
        return obj == null || obj.id != id ? null : obj.get();
    }
    
    @Texts({
        "fnHead=var jsvm = {};\n",
        "fnName=jsvm.@1 = function(",
        "fnThiz=thiz",
        "fnNoThiz=  var thiz = null;\n",
        "fnSep=,",
        "fnParam=p@1",
        "fnClose=) {\n",
        "fnBegin=  var encParams = ds(@1).toJava(null, -1, [",
        "fnPPar=@2 p@1", """
        fnBody=]);
          var v = ds(@3).toVM('c', '@1', '@2', thiz ? thiz.id : null, encParams);
          while (v !== null && v.indexOf && v.indexOf('javascript:') === 0) {
            var script = v.substring(11);
            try {
              var r = eval.call(null, script);
            } catch (e) {  console.warn('error: ' + e + ' executing: ' + script + ' at:\\n' + e.stack); }
            v = ds(@3).toVM('jr', null, null, null, null);  }
          return @4 ? eval('(' + v + ')') : v;
        };""",
        "fnFoot=ds(@2).rg(@1, jsvm);\n"
    })
    final Integer exportVm(Object vm) {
        int jNumber = registerObject(vm, false, null, null);
        int vmNumber = COUNTER.getAndIncrement();
        StringBuilder sb = new StringBuilder();
        sb.append(Strings.fnHead());
        for (Method m : vm.getClass().getMethods()) {
            if (m.getDeclaringClass() == Object.class) {
                continue;
            }
            final Class<?>[] types = m.getParameterTypes();
            boolean instanceMethod = 
                types.length > 0 && 
                m.getName().startsWith(types[0].getName().replace('.', '_') + "$");
            int params = instanceMethod ? types.length - 1 : types.length;
            sb.append(Strings.fnName(m.getName()));
            String sep;
            if (instanceMethod) {
                sb.append(Strings.fnThiz());
                sep = Strings.fnSep();
            } else {
                sep = "";
            }
            for (int i = 0; i < params; i++) {
                sb.append(sep);
                sb.append(Strings.fnParam(i));
                sep = Strings.fnSep();
            }
            sb.append(Strings.fnClose());
            if (!instanceMethod) {
                sb.append(Strings.fnNoThiz());
            }
            sb.append(Strings.fnBegin(key));
            for (int i = 0; i < params; i++) {
                sb.append(Strings.fnPPar(i, i == 0 ? "" : ","));
            }
            sb.append(Strings.fnBody(jNumber, m.getName(), key, evalJS));
        }
        sb.append(Strings.fnFoot(vmNumber, key));
        deferExec(sb);
        return vmNumber;
    }

    @Texts({
        "v_null=null",
        "v_number=number",
        "v_java=java",
        "v_object=object",
        "v_array=array",
        "v_boolean=boolean",
        "v_error=error"
    })
    final Object valueOf(String typeof, String res) {
        if (Strings.v_null().equals(typeof)) { // NOI18N
            return null;
        }
        if (Strings.v_number().equals(typeof)) { // NOI18N
            return Double.valueOf(res);
        }
        if (Strings.v_java().equals(typeof)) { // NOI18N
            return findObject(Integer.parseInt(res));
        }
        if (Strings.v_object().equals(typeof)) { // NOI18N
            return new JSObject(Integer.parseInt(res));
        }
        if (Strings.v_array().equals(typeof)) { // NOI18N
            int at = res.indexOf(':');
            int size = Integer.parseInt(res.substring(0, at));
            Object[] arr = new Object[size];
            at++;
            for (int i = 0; i < size; i++) {
                int next = res.indexOf(':', at);
                int length = Integer.parseInt(res.substring(at, next));
                at = next + 1 + length;
                arr[i] = valueOf(res.substring(next + 1, at));
            }
            return arr;
        }
        if (Strings.v_boolean().equals(typeof)) { // NOI18N
            return Boolean.valueOf(res);
        }
        if (Strings.v_error().equals(typeof)) { // NOI18N
            throw new IllegalStateException(res);
        }
        return res;
    }
    
    final Object valueOf(String typeAndValue) {
        int colon = typeAndValue.indexOf(':');
        return valueOf(typeAndValue.substring(0, colon), typeAndValue.substring(colon + 1));
    }

    final void encodeObject(Object a, boolean weak, StringBuilder sb, int[] vmId) {
        if (a == null) {
            sb.append(Strings.v_null());
        } else if (a.getClass().isArray()) {
            int len = Array.getLength(a);
            sb.append('[');
            String sep = "";
            for (int i = 0; i < len; i++) {
                Object o = Array.get(a, i);
                sb.append(sep);
                encodeObject(o, weak, sb, null);
                sep = ",";
            }
            sb.append(']');
        } else if (a instanceof Number) {
            sb.append(a.toString());
        } else if (a instanceof String) {
            sb.append('"');
            String s = (String)a;
            int len = s.length();
            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);
                switch (ch) {
                    case '\\': sb.append("\\\\"); break;
                    case '\n': sb.append("\\n"); break;
                    case '\"': sb.append("\\\""); break;
                    default:
                        sb.append(ch);
                        break;
                }
            }
            sb.append('"');
        } else if (a instanceof Boolean) {
            sb.append(a.toString());
        } else if (a instanceof Character) {
            sb.append((int)(Character)a);
        } else if (a instanceof JSObject) {
            sb.append("ds(").append(key).append(").o(").append(((JSObject) a).index).append(")");
        } else {
            if (vmId != null) {
                sb.append("ds(").append(key).append(").v(").append(vmId[0]).append(")");
            } else {
                String[] valueOf = { null };
                sb.append("ds(").append(key).append(").j(").append(registerObject(a, weak, null, valueOf));
                sb.append(",");
                encodeObject(valueOf[0], weak, sb, null);
                if (a instanceof Object[]) {
                    for (Object n : ((Object[])a)) {
                        sb.append(",");
                        encodeObject(n, weak, sb, null);
                    }
                }
                sb.append(")");
            }
        }
    }

    interface OnReady {
        void callbackReady(String name);
    }
    
    private class Item {
        final int id;
        final Item prev;
        Boolean done;

        final Method method;
        final Object thiz;
        final Object[] params;
        Object result;
        
        Item(int id, Item prev, Method method, Object thiz, Object[] params) {
            this.id = id;
            this.prev = prev;
            this.method = method;
            this.thiz = thiz;
            this.params = adaptParams(method, Arrays.asList(params));
            this.toExec = null;
        }
        
        
        protected final String inJavaScript(boolean[] finished) {
            if (this.method != null) {
                return js(finished);
            } else {
                return sj(finished);
            }
        }
        protected final void inJava() {
            if (this.method == null) {
                return;
            }
            if (done == null) {
                done = false;
                try {
                    log(Level.FINE, "Calling {0}", method);
                    result = method.invoke(thiz, params);
                } catch (Exception ex) {
                    log(Level.SEVERE, "Cannot invoke " + method + " on " + thiz + " with " + Arrays.toString(params), ex);
                } finally {
                    done = true;
                    log(Level.FINE, "Result: {0}", result);
                }
            }
        }
        
        protected String js(boolean[] finished) {
            if (Boolean.TRUE.equals(done)) {
                StringBuilder sb = new StringBuilder();
                encodeObject(result, false, sb, null);
                finished[0] = true;
                return sb.toString();
            }
            return null;
        }

        private final String toExec;
        private String typeof;
        
        Item(int id, Item prev, String toExec) {
            this.id = id;
            this.prev = prev;
            this.toExec = toExec;
            
            this.method = null;
            this.params = null;
            this.thiz = null;
        }

        protected String sj(boolean[] finished) {
            finished[0] = false;
            if (Boolean.TRUE.equals(done)) {
                return null;
            }
            done = true;
            return "javascript:" + toExec;
        }

        protected final void result(String typeof, String result) {
            if (this.method != null) {
                throw new UnsupportedOperationException();
            }
            this.typeof = typeof;
            this.result = result;
            log(Level.FINE, "result ({0}): {1} for {2}", typeof, result, toExec);
        }
    } // end of Item
    
    final void result(String counterId, String typeof, String res) {
        log(Level.FINE, "result#{2}@{0}: {1}", typeof, res, counterId);
        synchronized (lock()) {
            if ("OK".equals(typeof)) {
                log(Level.FINE, "init: {0}", res);
                lock().notifyAll();
                if ("Initialized".equals(res)) {
                    log(Level.FINE, "callbackReady: countingDown");
                    handleLog(Level.FINE, msg.toString());
                    msg = null;
                    initialized.countDown();
                }
                return;
            }
            final int id = Integer.parseInt(counterId);
            final Item top = topMostCall();
            if (top.id == id) {
                top.result(typeof, res);
                registerCall(top.prev);
                return;
            }
            Item it = top;
            while (it != null) {
                Item process = it.prev;
                if (process.id == id) {
                    process.result(typeof, res);
                    return;
                }
                it = process;
            }
            throw new IllegalStateException("Cannot find " + id + " for " + typeof + " res: " + res);
        }
    }

    final String javacall(
            String vmNumber, String fnName, String thizId, String encParams
    ) throws Exception {
        synchronized (lock()) {
            Object vm = findObject(Integer.parseInt(vmNumber));
            assert vm != null;
            final Object obj = thizId == null || "null".equals(thizId)
                    ? null : valueOf("java", thizId);
            Method method = null;
            for (Method m : vm.getClass().getMethods()) {
                if (m.getName().equals(fnName)) {
                    method = m;
                    break;
                }
            }
            assert method != null;
            List<Object> params = new ArrayList<Object>();
            if (obj != null) {
                params.add(obj);
            }
            final Object args = valueOf(encParams);
            if (!(args instanceof Object[])) {
                throw new IllegalStateException("Expecting array: " + args);
            }
            params.addAll(Arrays.asList((Object[]) args));
            Object[] converted = adaptParams(method, params);
            Item top = topMostCall();
            boolean first = top == null || Boolean.TRUE.equals(top.done);
            log(Level.FINE, "jc: {0}@{1}args: {2} is first: {3}, now: {4}", new Object[]{method.getName(), vm, params, first, topMostCall()});
            Item newItem = registerCall(new Item(nextCallId(), top, method, vm, converted));
            return javaresult();
        }
    }

    final String javaresult() throws IllegalStateException, InterruptedException {
        synchronized (lock()) {
            boolean[] finished = {false};
            for (;;) {
                StringBuilder def = getDeferred(true);
                if (def != null) {
                    def.insert(0, "javascript:");
                    return def.toString();
                }
                finished[0] = false;
                final Item top = dispatchPendingItem();
                if (top == null) {
                    continue;
                }
                String jsToExec = top.inJavaScript(finished);
                log(Level.FINE, "jr: {0} jsToExec: {1} finished: {2}", new Object[]{topMostCall(), jsToExec, finished[0]});
                if (jsToExec != null) {
                    if (finished[0]) {
                        registerCall(top.prev);
                    }
                    return jsToExec;
                }
                lock().wait();
            }
        }
    }

    private Item dispatchPendingItem() {
        final Item top = topMostCall();
        if (top.method != null && top.done == null) {
            dispatch(() -> {
                synchronized (lock()) {
                    Item pending = topMostCall();
                    if (pending != null) {
                        pending.inJava();
                        lock().notifyAll();
                    }
                }
            });
            if (getDeferred(false) != null) {
                return null;
            }
        }
        return top;
    }

    private StringBuilder deferred;
    private boolean deferredDisabled;
    private Collection<Object> arguments = new LinkedList<Object>();

    private StringBuilder getDeferred(boolean clear) {
        assert Thread.holdsLock(lock());
        StringBuilder sb = deferred;
        if (clear) {
            deferred = null;
            if (sb != null) {
                deferredDisabled = true;
            }
        }
        return sb;
    }

    final void deferExec(StringBuilder sb) {
        synchronized (lock()) {
            log(Level.FINE, "deferExec: {0} empty: {1}, call: {2}", new Object[]{sb, deferred == null, topMostCall()});
            if (deferred == null) {
                deferred = sb;
            } else {
                deferred.append(sb);
            }
        }
    }

    public final void loadScript(final Reader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        char[] arr = new char[4092];
        for (;;) {
            int len = reader.read(arr);
            if (len == -1) {
                break;
            }
            sb.append(arr, 0, len);
        }
        deferExec(sb);
    }


    @Texts({
        "flushExec=\n\nds(@1).toJava('r', '@2', null);\n"
    })
    void flushImpl() {
        synchronized (lock()) {
            StringBuilder def = getDeferred(false);
            if (def != null) {
                final int id = nextCallId();
                log(Level.FINE, "flush#{1}: {0}", def, id);
                exec(id, Strings.flushExec(key, id).toString());
            }
            if (topMostCall() == null) {
                deferredDisabled = false;
            }
        }
    }

    final Object exec(int id, String fn) {
        assert Thread.holdsLock(lock());
        boolean first;
        {
            StringBuilder def = getDeferred(true);
            if (def != null) {
                def.append(fn);
                fn = def.toString();
                log(Level.FINE, "Flushing {0}", fn);
            }
            Item c = topMostCall();
            if (c != null && c.method != null) {
                c.inJava();
                lock().notifyAll();
            }
        }

        Item myCall;
        boolean load;
        final Item top = topMostCall();
        if (top != null) {
            myCall = registerCall(new Item(id, top, fn));
            load = synchronous;
            first = false;
        } else {
            myCall = registerCall(new Item(id, null, null));
            load = true;
            first = true;
        }
        if (load) {
            loadJS(fn);
        }
        for (;;) {
            if (myCall.typeof != null) {
                break;
            }
            try {
                lock().wait();
            } catch (InterruptedException ex) {
                log(Level.SEVERE, null, ex);
            }
            Item c = topMostCall();
            if (c != null) {
                c.inJava();
            }
            lock().notifyAll();
        }
        Object ret = valueOf(myCall.typeof, (String) myCall.result);
        if (first) {
            arguments.clear();
        }
        return ret;
    }
    
    private static Object[] adaptParams(Method toCall, List<Object> args) {
        final Object[] arr = new Object[args.size()];
        final Class<?>[] types = toCall.getParameterTypes();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = adaptType(types[i], args.get(i));
        }
        return arr;
    }
    
    private static Object adaptType(Class<?> type, Object value) {
        if (type.isPrimitive() && value instanceof Number) {
            final Number n = (Number)value;
            if (type == Byte.TYPE) return n.byteValue();
            if (type == Short.TYPE) return n.shortValue();
            if (type == Integer.TYPE) return n.intValue();
            if (type == Long.TYPE) return n.longValue();
            if (type == Float.TYPE) return n.floatValue();
            if (type == Double.TYPE) return n.doubleValue();
            if (type == Character.TYPE) return (char)n.intValue();
        }
        return value;
    }
    
    private static final class JSObject {
        private final int index;

        public JSObject(int index) {
            this.index = index;
        }

        @Override
        public int hashCode() {
            return 37 * this.index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JSObject other = (JSObject) obj;
            return this.index == other.index;
        }

        @Texts({
            "jsObject=[jsobject-@1]"
        })
        @Override
        public String toString() {
            return Strings.jsObject(index).toString();
        }
        
    } // end of JSObject
    
    static final AtomicInteger COUNTER = new AtomicInteger(0);
    @Texts({
        "registerFn=ds(@2).rg(@1, function(",
        "registerCode=) {\n@1\n});",
        "v_vm=vm"
    })
    private final class GFn extends Fn {
        private final int id;
        private final int[] vmId;
        private final boolean[] keepAlive;
        
        public GFn(String code, String[] names, boolean[] ka) {
            super(Generic.this);
            this.id = COUNTER.getAndIncrement();
            this.keepAlive = ka;
            
            StringBuilder sb = new StringBuilder(1024);
            sb.append(Strings.registerFn(id, key));
            String sep = "";
            boolean isVm = false;
            for (String n : names) {
                sb.append(sep).append(n);
                sep = ",";
                isVm = false;
                if (Strings.v_vm().equals(n)) {
                    isVm = true;
                }
            }
            sb.append(Strings.registerCode(code));
            this.vmId = isVm ? new int[] { -1 } : null;
            deferExec(sb);
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return invokeImpl(true, thiz, args);
        }

        @Override
        public void invokeLater(Object thiz, Object... args) throws Exception {
            invokeImpl(false, thiz, args);
        }

        @Texts({
            "invokeImplFn=ds(@3).fn(@1, @2, @4, "
        })
        private Object invokeImpl(boolean wait4js, Object thiz, Object... args) throws Exception {
            if (vmId != null && vmId[0] < 0) {
                vmId[0] = exportVm(args[args.length - 1]);
            }

            StringBuilder sb = new StringBuilder(256);
            encodeObject(thiz, false, sb, null);
            for (int i = 0; i < args.length; i++) {
                sb.append(", ");
                boolean weak = keepAlive != null && !keepAlive[i];
                encodeObject(args[i], weak, sb, i == args.length - 1 ? vmId : null);
            }
            sb.append(");");
            
            arguments.add(thiz);
            arguments.add(args);

            synchronized (lock()) {
                boolean synchronizedExecution = wait4js || deferredDisabled;
                int callId = nextCallId();
                sb.insert(0, Strings.invokeImplFn(id, synchronizedExecution, key, callId));
                if (synchronizedExecution) {
                    return exec(callId, sb.toString());
                } else {
                    deferExec(sb);
                    return null;
                }
            }
        }
    }
    
    private static final class Exported implements Comparable<Exported> {
        private final int id;
        private final Object obj;
        private final boolean ref;

        Exported(int id, boolean ref, Object obj) {
            this.id = id;
            this.obj = ref ? createReferenceFor(obj) : obj;
            this.ref = ref;
            WeakHolder.clean();
        }
        
        protected Object get() {
            if (ref) {
                return ((Reference<?>)obj).get();
            } else {
                return obj;
            }
        }
        
        @Override
        public int compareTo(Exported o) {
            return id - o.id;
        }

        private static Object createReferenceFor(Object obj) {
            Reference<Object> ref = new WeakReference<Object>(obj);
            if (obj instanceof Reference) {
                Reference<?> myRef = (Reference<?>) obj;
                if (obj.getClass().getName().equals("org.netbeans.html.ko4j.Knockout")) {
                    // workaround for #255677
                    WeakHolder h = new WeakHolder(myRef.get(), obj);
                    h.register();
                }
            }
            return ref;
        }
    }

    private static final class WeakHolder extends PhantomReference<Object> {
        private static final ReferenceQueue QUEUE = new ReferenceQueue();
        private static final Set<WeakHolder> active = new HashSet<WeakHolder>();
        private final Object knockout;

        public WeakHolder(Object referent, Object knockout) {
            super(referent, QUEUE);
            this.knockout = knockout;
        }

        static void clean() {
            for (;;) {
                WeakHolder h = (WeakHolder) QUEUE.poll();
                if (h == null) {
                    break;
                }
                active.remove(h);
            }
        }

        void register() {
            active.add(this);
        }
    }

    private Item topMostCall() {
        assert Thread.holdsLock(lock());
        return call;
    }

    private Item registerCall(Item call) {
        assert Thread.holdsLock(lock());
        this.call = call;
        lock().notifyAll();
        return call;
    }

    private int nextCallId() {
        assert Thread.holdsLock(lock());
        return ++callCounter;
    }
}
