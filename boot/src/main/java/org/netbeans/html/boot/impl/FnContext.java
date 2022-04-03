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
package org.netbeans.html.boot.impl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach
 */
public final class FnContext implements Closeable {
    private static final FnContext DUMMY = new FnContext(null, null);
    private final Fn.Presenter current;
    private Object prev;
    private FnContext(Fn.Presenter prevP, Fn.Presenter newP) {
        this.current = newP;
        this.prev = prevP != null ? prevP : this;
    }


    public static URL isJavaScriptCapable(ClassLoader l) {
        if (l instanceof JsClassLoader) {
            return null;
        }
        return l.getResource("META-INF/net.java.html.js.classes");
    }

    public static ClassLoader newLoader(URL res, FindResources impl, Fn.Presenter p, ClassLoader parent) {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        Throwable t;
        try {
            Method newLoader = Class.forName("org.netbeans.html.boot.impl.FnUtils") // NOI18N
                .getMethod("newLoader", FindResources.class, Fn.Presenter.class, ClassLoader.class);
            return (ClassLoader) newLoader.invoke(null, impl, p, parent);
        } catch (LinkageError ex) {
            t = ex;
        } catch (Exception ex) {
            t = ex;
        }
        pw.println("When using @JavaScriptBody methods, one needs to either:");
        pw.println(" - include asm-5.0.jar on runtime classpath");
        pw.println(" - post process classes, see http://bits.netbeans.org/html+java/dev/net/java/html/js/package-summary.html#post-process");
        pw.append("However following classes has not been processed from ").println(res);
        
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(res.openStream()));
            for (;;) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                pw.append("  ").println(line);
            }
            r.close();
        } catch (IOException io) {
            pw.append("Cannot read ").println(res);
            io.printStackTrace(pw);
        }
        pw.println("Cannot initialize asm-5.0.jar!");
        pw.flush();
        Logger.getLogger(FnContext.class.getName()).log(Level.SEVERE, w.toString(), t);
        return null;
    }

    private static ThreadLocal<LinkedList<Runnable>> PENDING = new ThreadLocal<>();
    public static void registerMicrotask(Runnable promise) {
        var list = PENDING.get();
        if (list == null) {
            list = new LinkedList<>();
            PENDING.set(list);
        }
        list.add(promise);
    }

    @Override
    public void close() throws IOException {
        if (prev != this) {
            for (;;) {
                var list = PENDING.get();
                var p = list == null ? null : list.pollFirst();
                if (p == null) {
                    break;
                }
                p.run();
            }
            currentPresenter((Fn.Presenter)prev);
            prev = this;
            if (current instanceof Flushable) {
                ((Flushable)current).flush();
            }
        }
    }
/*
    @Override
    protected void finalize() throws Throwable {
        if (prev != null) {
            LOG.warning("Unclosed context!");
        }
    }
*/
    public static Closeable activate(Fn.Presenter newP) {
        final Fn.Presenter oldP = currentPresenter(newP);
        if (oldP == newP) {
            return DUMMY;
        }
        return new FnContext(oldP, newP);
    }
    
    
    private static final ThreadLocal<Fn.Presenter> CURRENT = new ThreadLocal<Fn.Presenter>();

    public static Fn.Presenter currentPresenter(Fn.Presenter p) {
        Fn.Presenter prev = CURRENT.get();
        CURRENT.set(p);
        return prev;
    }

    public static Fn.Presenter currentPresenter(boolean fail) {
        Fn.Presenter p = CURRENT.get();
        if (p == null && fail) {
            throw new IllegalStateException("No current WebView context around!");
        }
        return p;
    }
    
}
