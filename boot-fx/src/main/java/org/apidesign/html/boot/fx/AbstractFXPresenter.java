/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package org.apidesign.html.boot.fx;

import java.io.BufferedReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public abstract class AbstractFXPresenter implements Fn.Presenter {
    static final Logger LOG = Logger.getLogger(FXPresenter.class.getName());
    protected static int cnt;
    protected List<String> scripts;
    protected Runnable onLoad;
    protected WebEngine engine;

    @Override
    public Fn defineFn(String code, String... names) {
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
            try {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "calling {0} function #{1}", new Object[]{++call, id});
                }
                List<Object> all = new ArrayList<Object>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                all.addAll(Arrays.asList(args));
                Object ret = fn.call("call", all.toArray()); // NOI18N
                return ret == fn ? null : ret;
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
