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
package net.java.html;

import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.netbeans.html.context.impl.CtxAccssr;
import org.netbeans.html.context.impl.CtxImpl;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.context.spi.Contexts.Id;

/** Represents context where the <code>net.java.html.json.Model</code>
 * and other objects
 * operate in. The context is usually a particular HTML page in a browser.
 * The context is also associated with the actual HTML technology
 * in the HTML page - there is likely to be different context for 
 * <a href="http://knockoutjs.com">knockout.js</a> and different one
 * for <a href="http://angularjs.org">angular</a>. Since version 1.1
 * the content of contexts can be selected by registering
 * implementations under specific
 * {@link Id technology identifiers} and requesting them during 
 * {@link Contexts#newBuilder(java.lang.Object...) construction} of the
 * context.
 *
 * @author Jaroslav Tulach
 */
public final class BrwsrCtx implements Executor {
    private static final Logger LOG = Logger.getLogger(BrwsrCtx.class.getName());
    private final CtxImpl impl;
    private BrwsrCtx(CtxImpl impl) {
        this.impl = impl;
    }
    static {
        new CtxAccssr() {
            @Override
            protected BrwsrCtx newContext(CtxImpl impl) {
                return new BrwsrCtx(impl);
            }

            @Override
            protected CtxImpl find(BrwsrCtx context) {
                return context.impl;
            }
        };
    }
    /** Dummy context without binding to any real browser or technology. 
     * Useful for simple unit testing of behavior of various business logic
     * code.
     */
    public static final BrwsrCtx EMPTY = Contexts.newBuilder().build();
    
    /** currently {@link #execute(java.lang.Runnable) activated context} */
    private static final ThreadLocal<BrwsrCtx> CURRENT = new ThreadLocal<BrwsrCtx>();
    
    /** Seeks for the default context that is associated with the requesting
     * class. If no suitable context is found, a warning message is
     * printed and {@link #EMPTY} context is returned. One can enter 
     * a context by calling {@link #execute(java.lang.Runnable)}.
     * 
     * @param requestor the class that makes the request
     * @return appropriate context for the request
     */
    public static BrwsrCtx findDefault(Class<?> requestor) {
        BrwsrCtx brwsr = CURRENT.get();
        if (brwsr != null) {
            return brwsr;
        }
        
        org.netbeans.html.context.spi.Contexts.Builder cb = Contexts.newBuilder();
        boolean found = Contexts.fillInByProviders(requestor, cb);
        if (!found) {
            LOG.warning("No browser context found. Returning empty technology!");
            return EMPTY;
        }
        return cb.build();
    }

    /** 
     * <p>
     * Runs provided code in the context of this {@link BrwsrCtx}.
     * If there is an {@link Executor} {@link Contexts#find(net.java.html.BrwsrCtx, java.lang.Class)  registered in the context}
     * it is used to perform the given code. While the code <code>exec</code>
     * is running the value of {@link #findDefault(java.lang.Class)} returns
     * <code>this</code>. If the executor supports a single thread execution
     * policy, it may execute the runnable later (in such case this method
     * returns immediately). If the call to this method is done on the right
     * thread, the runnable should be executed synchronously.
     * </p>
     * <p>
     * <b>Example Using a Timer</b>
     * </p>
<pre>
<b>public final class</b> Periodicaly <b>extends</b> {@link java.util.TimerTask} {
    <b>private final</b> {@link BrwsrCtx} ctx;

    <b>private</b> Periodicaly(BrwsrCtx ctx) {
        // remember the browser context and use it later
        this.ctx = ctx;
    }
    
    <b>public void</b> run() {
        // arrives on wrong thread, needs to be re-scheduled
        ctx.{@link #execute(java.lang.Runnable) execute}(new Runnable() {
            <b>public void</b> run() {
                // code that needs to run in a browser environment
            }
        });
    }

    // called when your page is ready
    <b>public static void</b> onPageLoad(String... args) <b>throws</b> Exception {
        // the context at the time of page initialization
        BrwsrCtx initialCtx = BrwsrCtx.findDefault(getClass());
        // the task that is associated with context 
        Periodicaly task = new Periodicaly(initialCtx);
        // creates a timer
        {@link java.util.Timer} t = new {@link java.util.Timer}("Move the box");
        // run the task ever 100ms
        t.{@link java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long, long) scheduleAtFixedRate}(task, 0, 100);
    }
}
</pre>    
     * 
     * @param exec the code to execute
     * @since 0.7.6
     */
    @Override public final void execute(final Runnable exec) {
        class Wrap implements Runnable {
            @Override
            public void run() {
                BrwsrCtx prev = CURRENT.get();
                try {
                    CURRENT.set(BrwsrCtx.this);
                    exec.run();
                } finally {
                    CURRENT.set(prev);
                }
            }
        }
        Wrap w = new Wrap();
        Executor runIn = Contexts.find(this, Executor.class);
        if (runIn == null) {
            w.run();
        } else {
            runIn.execute(w);
        }
    }
}

