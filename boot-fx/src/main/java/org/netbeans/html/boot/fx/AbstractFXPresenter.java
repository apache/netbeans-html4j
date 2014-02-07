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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apidesign.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public abstract class AbstractFXPresenter 
implements Fn.Presenter, Fn.ToJavaScript, Fn.FromJavaScript, Executor {
    static final Logger LOG = Logger.getLogger(FXPresenter.class.getName());
    protected static int cnt;
    protected List<String> scripts;
    protected Runnable onLoad;
    protected WebEngine engine;

    @Override
    public Fn defineFn(String code, String... names) {
        return defineJSFn(code, names);
    }
    
    final JSFn defineJSFn(String code, String... names) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function() {");
        sb.append("  return function(");
        String sep = "";
        for (String n : names) {
            sb.append(sep).append(n);
            sep = ",";
        }
        sb.append(") {\n");
        sb.append(code);
        sb.append("};");
        sb.append("})()");
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, 
                "defining function #{0}:\n{1}\n", 
                new Object[] { ++cnt, code }
            );
        }
        JSObject x = (JSObject) engine.executeScript(sb.toString());
        return new JSFn(this, x, cnt);
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
        if (scripts != null) {
            scripts.add(script);
        }
        engine.executeScript(script);
    }

    protected final void onPageLoad() {
        if (scripts != null) {
            for (String s : scripts) {
                engine.executeScript(s);
            }
        }
        onLoad.run();
    }

    @Override
    public void displayPage(final URL resource, final Runnable onLoad) {
        this.onLoad = onLoad;
        final WebView view = findView(resource);
        this.engine = view.getEngine();
        try {
            if (FXInspect.initialize(engine)) {
                scripts = new ArrayList<String>();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        class Run implements Runnable {

            @Override
            public void run() {
                if (scripts != null) {
                    view.setContextMenuEnabled(true);
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

    private JSObject wrapArrImpl;
    private final JSObject wrapArrFn() {
        if (wrapArrImpl == null) {
            try {
                wrapArrImpl = (JSObject)defineJSFn("  var k = {};"
                    + "  k.array= function() {"
                    + "    return Array.prototype.slice.call(arguments);"
                    + "  };"
                    + "  return k;"
                ).invokeImpl(null, false);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return wrapArrImpl;
    }

    final Object checkArray(Object val) {
        int length = ((Number) arraySizeFn().call("array", val, null)).intValue();
        if (length == -1) {
            return val;
        }
        Object[] arr = new Object[length];
        arraySizeFn().call("array", val, arr);
        return arr;
    }
    private JSObject arraySize;
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
                    + "  return k;"
                ).invokeImpl(null, false);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return arraySize;
    }

    @Override
    public Object toJava(Object jsArray) {
        return checkArray(jsArray);
    }
    
    @Override
    public Object toJavaScript(Object toReturn) {
        if (toReturn instanceof Object[]) {
            return convertArrays((Object[])toReturn);
        } else {
            return toReturn;
        }
    }
    
    @Override public void execute(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    private static final class JSFn extends Fn {

        private final JSObject fn;
        private static int call;
        private final int id;

        public JSFn(AbstractFXPresenter p, JSObject fn, int id) {
            super(p);
            this.fn = fn;
            this.id = id;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return invokeImpl(thiz, true, args);
        }
        
        final Object invokeImpl(Object thiz, boolean arrayChecks, Object... args) throws Exception {
            try {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "calling {0} function #{1}", new Object[]{++call, id});
                }
                List<Object> all = new ArrayList<Object>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                for (int i = 0; i < args.length; i++) {
                    if (arrayChecks && args[i] instanceof Object[]) {
                        Object[] arr = (Object[]) args[i];
                        Object conv = ((AbstractFXPresenter)presenter()).convertArrays(arr);
                        args[i] = conv;
                    }
                    all.add(args[i]);
                }
                Object ret = fn.call("call", all.toArray()); // NOI18N
                if (ret == fn) {
                    return null;
                }
                if (!arrayChecks) {
                    return ret;
                }
                return ((AbstractFXPresenter)presenter()).checkArray(ret);
            } catch (Error t) {
                t.printStackTrace();
                throw t;
            } catch (Exception t) {
                t.printStackTrace();
                throw t;
            }
        }
    }
    
}
