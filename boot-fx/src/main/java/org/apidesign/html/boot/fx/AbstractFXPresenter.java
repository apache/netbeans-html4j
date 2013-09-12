/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
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
            LOG.log(Level.FINE, "defining function #{0}", ++cnt);
            LOG.fine("-----");
            LOG.fine(code);
            LOG.fine("-----");
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
        public Object handleInvoke(Object thiz, Object... args) throws Exception {
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
