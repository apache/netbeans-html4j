/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
package org.netbeans.html.boot.fx;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
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
    private JSObject newPOJOImpl;
    private Object undefined;
    private JavaValues values;

    @Override
    protected AbstractFXPresenter clone() {
        try {
            AbstractFXPresenter p = (AbstractFXPresenter) super.clone();
            p.arraySize = null;
            p.wrapArrImpl = null;
            p.undefined = null;
            p.newPOJOImpl = null;
            p.values = null;
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

    private final JavaValues values() {
        if (values == null) {
            values = new JavaValues();
        }
        return values;
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

    JSObject createPOJOWrapper(int hash, int id) {
        if (newPOJOImpl == null) {
            try {
                newPOJOImpl = (JSObject) defineJSFn(
                    "var k = {};\n" +
                    "k.fxBrwsrId = function(hash, id) {\n" +
                    "  return {\n" +
                    "    'fxBrwsrId' : function(callback) { callback.hashAndId(hash, id); }\n" +
                    "  }\n" +
                    "};\n" +
                    "return k;\n", new String[] { "callback" }, null
                ).invokeImpl(null, false);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return (JSObject) newPOJOImpl.call("fxBrwsrId", hash, id);
    }

    final Object undefined() {
        if (undefined == null) {
            undefined = engine.executeScript("undefined");
        }
        return undefined;
    }

    private int getArrayLength(Object val) throws JSException {
        int length = ((Number) arraySizeFn().call("array", val, null)).intValue();
        return length;
    }

    private Object[] toArray(int length, Object val) throws JSException {
        Object[] arr = new Object[length];
        arraySizeFn().call("array", val, arr);
        checkArray(arr);
        return arr;
    }

    private void checkArray(Object[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = toJava(arr[i]);
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
        if (toJS == undefined()) {
            return null;
        }
        if (!(toJS instanceof JSObject)) {
            return toJS;
        }
        JSObject js = (JSObject) toJS;
        int length = getArrayLength(toJS);
        if (length != -1) {
            Object[] arr = toArray(length, toJS);
            System.err.println("converter to Java array:");
            dumpArray(arr, "");
            return arr;
        }
        return values().realValue(js);
    }

    @Override
    public Object toJavaScript(Object value) {
        return toJavaScript(value, true);
    }

    final Object toJavaScript(Object value, boolean keep) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return value;
        }
        if (value instanceof Number) {
            return value;
        }
        if (value instanceof JSObject) {
            return value;
        }
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof Character) {
            return (int) (char) (Character) value;
        }
        int len = isArray(value);
        if (len >= 0) {
            Object[] copy = new Object[len];
            for (int i = 0; i < len; i++) {
                copy[i] = toJavaScript(Array.get(value, i));
            }
            final JSObject wrapArr = (JSObject)wrapArrFn().call("array", copy); // NOI18N
            return wrapArr;
        }
        if (value.getClass().getName().endsWith("$JsCallbacks$")) {
            return value;
        }
        return values().wrap(value, keep);
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

    private void dumpArray(Object[] arr, String spaces) {
        for (int i = 0; i < arr.length; i++) {
            System.err.println(spaces + "[" + i + "] = " + arr[i]);
            if (arr[i] instanceof Object[]) {
                dumpArray((Object[]) arr[i], spaces + "  ");
            }
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
                all.add(thiz == null ? presenter.undefined() : thiz);
                for (int i = 0; i < args.length; i++) {
                    Object conv = args[i];
                    if (arrayChecks) {
                        boolean alive = keepAlive == null || keepAlive[i];
                        conv = presenter.toJavaScript(conv, alive);
                    }
                    all.add(conv);
                }
                Object ret = fn.call("call", all.toArray()); // NOI18N
                if (ret == presenter.undefined()) {
                    return null;
                }
                if (!arrayChecks) {
                    return ret;
                }
                return presenter.toJava(ret);
            } catch (Error t) {
                t.printStackTrace();
                throw t;
            } catch (Exception t) {
                t.printStackTrace();
                throw t;
            }
        }
    }

    protected int isArray(Object value) {
        try {
            return Array.getLength(value);
        } catch (IllegalArgumentException ex) {
            return -1;
        }
    }

    private interface Ref extends Comparable<Ref> {
        Object value();
        int id();
        JSObject jsObj();
    }

    private final class WeakRef extends WeakReference<Object> implements Ref {
        private final int id;
        private final JSObject js;

        WeakRef(Object value, int id, JSObject js) {
            super(value);
            this.id = id;
            this.js = js;
        }

        @Override
        public Object value() {
            return get();
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public JSObject jsObj() {
            return js;
        }

        @Override
        public int compareTo(Ref o) {
            return this.id() - o.id();
        }
    }

    private final class StrongRef implements Ref {
        private final Object value;
        private final int id;
        private final JSObject js;

        StrongRef(Object value, int id, JSObject js) {
            this.value = value;
            this.id = id;
            this.js = js;
        }

        @Override
        public Object value() {
            return value;
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public JSObject jsObj() {
            return js;
        }

        @Override
        public int compareTo(Ref o) {
            return this.id() - o.id();
        }
    }

    public final class JavaValues {
        private final Map<Integer,NavigableSet<Ref>> values;
        private int hash;
        private int id;

        JavaValues() {
            this.values = new HashMap<Integer,NavigableSet<Ref>>();
        }

        synchronized final JSObject wrap(Object pojo, boolean keep) {
            int hash = System.identityHashCode(pojo);
            NavigableSet<Ref> refs = values.get(hash);
            if (refs != null) {
                for (Ref ref : refs) {
                    if (ref.value() == pojo) {
                        return ref.jsObj();
                    }
                }
            } else {
                refs = new TreeSet<Ref>();
                values.put(hash, refs);
            }
            int id = findId(refs);
            JSObject js = createPOJOWrapper(hash, id);
            Ref newRef = keep ? new StrongRef(pojo, id, js) : new WeakRef(pojo, id, js);
            refs.add(newRef);
            return newRef.jsObj();
        }

        private int findId(NavigableSet<Ref> refs) {
            if (refs.isEmpty()) {
                return 0;
            }
            final Ref first = refs.first();
            int previous = first.id();
            if (previous > 0) {
                return 0;
            }
            for (Ref ref : refs.tailSet(first, false)) {
                int next = ref.id();
                if (previous + 1 < next) {
                    return previous + 1;
                }
                previous = next;
            }
            return previous + 1;
        }

        public void hashAndId(int hash, int id) {
            assert this.hash == -1;
            assert this.id == -1;
            this.hash = hash;
            this.id = id;
        }

        Object realValue(JSObject obj) {
            Object java = obj.getMember("fxBrwsrId");
            if (java instanceof JSObject) {
                for (;;) {
                    int resultHash;
                    int resultId;
                    synchronized (this) {
                        this.hash = -1;
                        this.id = -1;
                        obj.call("fxBrwsrId", this);
                        assert this.hash != -1;
                        assert this.id != -1;
                        resultHash = this.hash;
                        resultId = this.id;
                    }

                    final NavigableSet<Ref> refs = values.get(resultHash);
                    Iterator<Ref> it = refs.iterator();
                    while (it.hasNext()) {
                        Ref next = it.next();
                        Object pojo = next.value();
                        if (next.id() == resultId) {
                            return pojo;
                        }
                        if (pojo == null) {
                            it.remove();
                        }
                    }
                    if (refs.isEmpty()) {
                        values.remove(resultHash);
                    }
                }
            }
            return obj;
        }
    }


}
