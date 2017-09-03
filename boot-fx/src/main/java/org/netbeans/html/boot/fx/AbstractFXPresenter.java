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
package org.netbeans.html.boot.fx;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.netbeans.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach
 */
public abstract class AbstractFXPresenter implements Fn.Presenter,
Fn.KeepAlive, Fn.ToJavaScript, Fn.FromJavaScript, Executor, Cloneable {
    static final Logger LOG = Logger.getLogger(FXPresenter.class.getName());
    protected static int cnt;
    protected Runnable onLoad;
    protected WebEngine engine;

    // transient - e.g. not cloneable
    private JSObject arraySize;
    private JSObject wrapArrImpl;
    private Object undefined;

    @Override
    protected AbstractFXPresenter clone() {
        try {
            AbstractFXPresenter p = (AbstractFXPresenter) super.clone();
            p.arraySize = null;
            p.wrapArrImpl = null;
            p.undefined = null;
            return p;
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Fn defineFn(String code, String... names) {
        return defineJSFn(code, names, null);
    }

    @Override
    public Fn defineFn(String code, String[] names, boolean[] keepAlive) {
        return defineJSFn(code, names, keepAlive);
    }



    final JSFn defineJSFn(String code, String[] names, boolean[] keepAlive) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function() {\n");
        sb.append("  return function(\n    ");
        String sep = "";
        if (names != null) for (String n : names) {
            sb.append(sep).append(n);
            sep = ",";
        }
        sb.append("  \n) {\n");
        sb.append(code);
        sb.append("};\n");
        sb.append("})();\n");
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE,
                "defining function #{0}:\n{1}\n",
                new Object[] { ++cnt, code }
            );
        }
        JSObject x = (JSObject) engine.executeScript(sb.toString());
        return new JSFn(this, x, cnt, keepAlive);
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        BufferedReader r = new BufferedReader(code);
        StringBuilder sb = new StringBuilder();
        for (;;) {
            String l = r.readLine();
            if (l == null) {
                break;
            }
            sb.append(l).append('\n');
        }
        final String script = sb.toString();
        engine.executeScript(script);
    }

    protected final void onPageLoad() {
        Closeable c = Fn.activate(this.clone());
        try {
            onLoad.run();
        } finally {
            try {
                c.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void displayPage(final URL resource, final Runnable onLoad) {
        this.onLoad = onLoad;
        final WebView view = findView(resource);
        this.engine = view.getEngine();
        boolean inspectOn = false;
        try {
            if (FXInspect.initialize(engine)) {
                inspectOn = true;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        final boolean isFirebugOn = Boolean.getBoolean("firebug.lite"); // NOI18N
        final boolean isInspectOn = inspectOn;
        class Run implements Runnable {

            @Override
            public void run() {
                if (isInspectOn || isFirebugOn) {
                    view.setContextMenuEnabled(true);
                    final Parent p = view.getParent();
                    if (p instanceof BorderPane) {
                        BorderPane bp = (BorderPane) p;
                        if (bp.getTop() == null) {
                            bp.setTop(new FXToolbar(view, bp, isFirebugOn));
                        }
                    }
                }
                engine.load(resource.toExternalForm());
            }
        }
        Run run = new Run();
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
        waitFinished();
    }

    protected abstract void waitFinished();

    protected abstract WebView findView(final URL resource);

    final JSObject convertArrays(Object[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Object[]) {
                arr[i] = convertArrays((Object[]) arr[i]);
            }
        }
        final JSObject wrapArr = (JSObject)wrapArrFn().call("array", arr); // NOI18N
        return wrapArr;
    }

    private final JSObject wrapArrFn() {
        if (wrapArrImpl == null) {
            try {
                wrapArrImpl = (JSObject)defineJSFn("  var k = {};"
                    + "  k.array= function() {"
                    + "    return Array.prototype.slice.call(arguments);"
                    + "  };"
                    + "  return k;", null, null
                ).invokeImpl(null, false);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return wrapArrImpl;
    }

    final Object undefined() {
        if (undefined == null) {
            undefined = engine.executeScript("undefined");
        }
        return undefined;
    }

    final Object checkArray(Object val) {
        if (!(val instanceof JSObject)) {
            return val;
        }
        int length = ((Number) arraySizeFn().call("array", val, null)).intValue();
        if (length == -1) {
            return val;
        }
        Object[] arr = new Object[length];
        arraySizeFn().call("array", val, arr);
        clearUndefinedArray(arr);
        return arr;
    }

    private void clearUndefinedArray(Object[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == undefined) {
                arr[i] = null;
                continue;
            }
            if (arr[i] instanceof Object[]) {
                clearUndefinedArray((Object[])arr[i]);
            }
        }
    }

    private final JSObject arraySizeFn() {
        if (arraySize == null) {
            try {
                arraySize = (JSObject)defineJSFn("  var k = {};"
                    + "  k.array = function(arr, to) {"
                    + "    if (to === null) {"
                    + "      if (Object.prototype.toString.call(arr) === '[object Array]') return arr.length;"
                    + "      else return -1;"
                    + "    } else {"
                    + "      var l = arr.length;"
                    + "      for (var i = 0; i < l; i++) to[i] = arr[i];"
                    + "      return l;"
                    + "    }"
                    + "  };"
                    + "  return k;", null, null
                ).invokeImpl(null, false);
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
        if (toJS == undefined()) {
            return null;
        }
        return checkArray(toJS);
    }

    @Override
    public Object toJavaScript(Object toReturn) {
        if (toReturn instanceof Object[]) {
            return convertArrays((Object[])toReturn);
        } else {
            if (toReturn instanceof Character) {
                return (int)(Character)toReturn;
            }
            return toReturn;
        }
    }

    @Override public void execute(final Runnable r) {
        if (Platform.isFxApplicationThread()) {
            Closeable c = Fn.activate(this);
            try {
                r.run();
            } finally {
                try {
                    c.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        } else {
            class Wrap implements Runnable {
                @Override
                public void run() {
                    Closeable c = Fn.activate(AbstractFXPresenter.this);
                    try {
                        r.run();
                    } finally {
                        try {
                            c.close();
                        } catch (IOException ex) {
                            // ignore
                        }
                    }
                }
            }
            Platform.runLater(new Wrap());
        }
    }

    private static final class JSFn extends Fn {

        private final JSObject fn;
        private static int call;
        private final int id;
        private final boolean[] keepAlive;

        public JSFn(AbstractFXPresenter p, JSObject fn, int id, boolean[] keepAlive) {
            super(p);
            this.fn = fn;
            this.id = id;
            this.keepAlive = keepAlive;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return invokeImpl(thiz, true, args);
        }

        final Object invokeImpl(Object thiz, boolean arrayChecks, Object... args) throws Exception {
            try {
                final AbstractFXPresenter presenter = (AbstractFXPresenter) presenter();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "calling {0} function #{1}", new Object[]{++call, id});
                    LOG.log(Level.FINER, "  thiz  : {0}", thiz);
                    LOG.log(Level.FINER, "  params: {0}", Arrays.asList(args));
                }
                List<Object> all = new ArrayList<Object>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                for (int i = 0; i < args.length; i++) {
                    Object conv = args[i];
                    if (arrayChecks) {
                        if (args[i] instanceof Object[]) {
                            Object[] arr = (Object[]) args[i];
                            conv = presenter.convertArrays(arr);
                        }
                        if (conv != null && keepAlive != null &&
                            !keepAlive[i] && !isJSReady(conv) &&
                            !conv.getClass().getSimpleName().equals("$JsCallbacks$") // NOI18N
                        ) {
                            conv = new Weak(conv);
                        }
                        if (conv instanceof Character) {
                            conv = (int)(Character)conv;
                        }
                    }
                    all.add(conv);
                }
                Object ret = fn.call("call", all.toArray()); // NOI18N
                if (ret instanceof Weak) {
                    ret = ((Weak)ret).get();
                }
                if (ret == fn || ret == presenter.undefined()) {
                    return null;
                }
                if (!arrayChecks) {
                    return ret;
                }
                return presenter.checkArray(ret);
            } catch (Error t) {
                t.printStackTrace();
                throw t;
            } catch (Exception t) {
                t.printStackTrace();
                throw t;
            }
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
        if (obj instanceof JSObject) {
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
            assert !(referent instanceof Weak);
        }
    } // end of Weak
}
