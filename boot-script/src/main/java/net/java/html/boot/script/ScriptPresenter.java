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
package net.java.html.boot.script;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
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
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.boot.spi.Fn.Presenter;

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
final class ScriptPresenter 
implements Presenter, Fn.FromJavaScript, Fn.ToJavaScript, Executor {
    private static final Logger LOG = Logger.getLogger(ScriptPresenter.class.getName());
    private final ScriptEngine eng;
    private final Executor exc;

    public ScriptPresenter(Executor exc) {
        this.exc = exc;
        try {
            eng = new ScriptEngineManager().getEngineByName("javascript");
            eng.eval("function alert(msg) { Packages.java.lang.System.out.println(msg); };");
            eng.eval("function confirm(msg) { Packages.java.lang.System.out.println(msg); return true; };");
            eng.eval("function prompt(msg, txt) { Packages.java.lang.System.out.println(msg + ':' + txt); return txt; };");
        } catch (ScriptException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Fn defineFn(String code, String... names) {
        return defineImpl(code, names);
    }
    private FnImpl defineImpl(String code, String... names) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function() {");
        sb.append("  return function(");
        String sep = "";
        if (names != null) for (String n : names) {
            sb.append(sep).append(n);
            sep = ",";
        }
        sb.append(") {\n");
        sb.append(code);
        sb.append("};");
        sb.append("})()");

        final Object fn;
        try {
            fn = eng.eval(sb.toString());
        } catch (ScriptException ex) {
            throw new IllegalStateException(ex);
        }
        return new FnImpl(this, fn);
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
                wrapArrImpl = defineImpl("return Array.prototype.slice.call(arguments);");
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return wrapArrImpl;
    }

    final Object checkArray(Object val) throws Exception {
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
                    + "if (to === null) {\n"
                    + "  if (Object.prototype.toString.call(arr) === '[object Array]') return arr.length;\n"
                    + "  else return -1;\n"
                    + "} else {\n"
                    + "  var l = arr.length;\n"
                    + "  for (var i = 0; i < l; i++) to[i] = arr[i];\n"
                    + "  return l;\n"
                    + "}", "arr", "to"
                );
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return arraySize;
    }

    @Override
    public Object toJava(Object jsArray) {
        try {
            return checkArray(jsArray);
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

        public FnImpl(Presenter presenter, Object fn) {
            super(presenter);
            this.fn = fn;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return invokeImpl(thiz, true, args);
        }

            final Object invokeImpl(Object thiz, boolean arrayChecks, Object... args) throws Exception {
                List<Object> all = new ArrayList<>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                for (int i = 0; i < args.length; i++) {
                    if (arrayChecks) {
                        if (args[i] instanceof Object[]) {
                            Object[] arr = (Object[]) args[i];
                            Object conv = ((ScriptPresenter)presenter()).convertArrays(arr);
                            args[i] = conv;
                        }
                        if (args[i] instanceof Character) {
                            args[i] = (int)((Character)args[i]);
                        }
                    }
                    all.add(args[i]);
                }
                Object ret = ((Invocable)eng).invokeMethod(fn, "call", all.toArray()); // NOI18N
                if (ret == fn) {
                    return null;
                }
                if (!arrayChecks) {
                    return ret;
                }
                return ((ScriptPresenter)presenter()).checkArray(ret);
            }
    }
    
}
