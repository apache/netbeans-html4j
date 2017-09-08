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
    /** currently {@link #execute(java.lang.Runnable) activated context} */
    private static final ThreadLocal<BrwsrCtx> CURRENT = new ThreadLocal<BrwsrCtx>();
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
    
    
    /** Seeks for the default context that is associated with the requesting
     * class. If no suitable context is found, a warning message is
     * printed and {@link #EMPTY} context is returned. One can enter 
     * a context by calling {@link #execute(java.lang.Runnable)}.
     * 
     * @param requestor the class that makes the request
     * @return appropriate context for the request
     */
    public static BrwsrCtx findDefault(Class<?> requestor) {
        if (requestor == CtxAccssr.class) {
            return EMPTY;
        }
        BrwsrCtx brwsr = CURRENT.get();
        if (brwsr != null) {
            return brwsr;
        }
        
        org.netbeans.html.context.spi.Contexts.Builder cb = Contexts.newBuilder();
        boolean found = Contexts.fillInByProviders(requestor, cb);
        if (!found) {
            LOG.config("No browser context found. Returning empty technology!");
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
     * {@codesnippet org.netbeans.html.boot.fx.Periodicaly}
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

