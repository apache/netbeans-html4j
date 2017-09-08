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
package net.java.html.boot.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.java.JavaInterop;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/**
 * Implementation of {@link Presenter} that delegates to Truffle.
 *
 * @author Jaroslav Tulach
 */
final class TrufflePresenter implements Fn.KeepAlive,
    Presenter, Fn.FromJavaScript, Fn.ToJavaScript, Executor {

    private Eval eval;
    private WrapArray copy;
    private final Executor exc;
    private final CallTarget isNull;
    private final CallTarget isArray;
    private Apply apply;
    private TruffleObject jsNull;

    TrufflePresenter(Executor exc, TruffleObject eval) {
        this.exc = exc;
        this.eval = eval == null ? null : JavaInterop.asJavaFunction(Eval.class, eval);
        this.isNull = Truffle.getRuntime().createCallTarget(new IsNullNode());
        this.isArray = Truffle.getRuntime().createCallTarget(new IsArrayNode());
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
        sb.append("  var args = Array.prototype.slice.call(arguments);\n");
        sb.append("  var thiz = args.shift();\n");
        sb.append("  return (function(");
        String sep = "";
        for (String n : names) {
            sb.append(sep).append(n);
            sep = ",";
        }
        sb.append(") {\n");
        sb.append(code);
        sb.append("\n  }).apply(thiz, args);\n");
        sb.append("})\n");

        TruffleObject fn = (TruffleObject) getEval().eval(sb.toString());
        return new FnImpl(this, fn, names.length);
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        if (onPageLoad != null) {
            onPageLoad.run();
        }
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        Source src = Source.newBuilder(code).
            name("unknown.js").
            mimeType("text/javascript").
            build();
        getEval().eval(src.getCode());
    }

    interface Apply {
        public Object apply(Object... args);
    }

    interface WrapArray {
        public Object copy(Object arr);
    }

    interface Eval {
        public Object eval(String code);
    }

    final Object checkArray(Object val) throws Exception {
        if (val instanceof TruffleObject) {
            final TruffleObject truffleObj = (TruffleObject)val;
            boolean hasSize = (boolean) isArray.call(truffleObj);
            if (hasSize) {
                List<?> list = JavaInterop.asJavaObject(List.class, truffleObj);
                Object[] arr = list.toArray();
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = toJava(arr[i]);
                }
                return arr;
            }
        }
        return val;
    }

    @Override
    public Object toJava(Object jsArray) {
        if (jsArray instanceof JavaValue) {
            jsArray = ((JavaValue) jsArray).get();
        }
        if (jsArray instanceof TruffleObject) {
            boolean checkNull = (boolean) isNull.call(jsArray);
            if (checkNull) {
                return null;
            }
        }
        try {
            return checkArray(jsArray);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Object toJavaScript(Object conv) {
        return JavaValue.toJavaScript(conv, getWrap());
    }

    @Override
    public void execute(final Runnable command) {
        if (Fn.activePresenter() == this) {
            command.run();
            return;
        }

        class Wrap implements Runnable {

            @Override
            public void run() {
                try (Closeable c = Fn.activate(TrufflePresenter.this)) {
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

    private Eval getEval() {
        if (eval == null) {
            try {
                final PolyglotEngine engine = PolyglotEngine.newBuilder().build();
                TruffleObject fn = (TruffleObject) engine.eval(
                    Source.newBuilder("eval.bind(this)").
                        mimeType("text/javascript").
                        name("eval.js").build()
                ).get();
                eval = JavaInterop.asJavaFunction(Eval.class, fn);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return eval;
    }

    private WrapArray getWrap() {
        if (copy == null) {
            TruffleObject fn = (TruffleObject) getEval().eval("(function(arr) {\n"
                + "  var n = [];\n"
                + "  for (var i = 0; i < arr.length; i++) {\n"
                + "    n[i] = arr[i];\n"
                + "  }\n"
                + "  return n;\n"
                + "}).bind(this)"
            );
            copy = JavaInterop.asJavaFunction(WrapArray.class, fn);
        }
        return copy;
    }

    private Apply getApply() {
        if (apply == null) {
            TruffleObject fn = (TruffleObject) getEval().eval("(function() {\n"
                + "  var args = Array.prototype.slice.call(arguments);\n"
                + "  var fn = args.shift();\n"
                + "  return fn.apply(null, args);\n"
                + "}).bind(this)"
            );
            apply = JavaInterop.asJavaFunction(Apply.class, fn);
        }
        return apply;
    }

    private TruffleObject jsNull() {
        if (jsNull == null) {
            jsNull = (TruffleObject) getEval().eval("null"); // NOI18N
        }
        return jsNull;
    }

    private class FnImpl extends Fn {

        private final TruffleObject fn;

        public FnImpl(Presenter presenter, TruffleObject fn, int arity) {
            super(presenter);
            this.fn = fn;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            List<Object> all = new ArrayList<>(args.length + 1);
            all.add(fn);
            all.add(thiz == null ? jsNull() : toJavaScript(thiz));
            for (Object conv : args) {
                conv = toJavaScript(conv);
                all.add(conv);
            }
            Object ret = getApply().apply(all.toArray());
            if (ret instanceof JavaValue) {
                ret = ((JavaValue)ret).get();
            }
            if (ret == fn) {
                return null;
            }
            return toJava(ret);
        }
    }

    static abstract class JavaLang extends TruffleLanguage<Object> {
    }
}
