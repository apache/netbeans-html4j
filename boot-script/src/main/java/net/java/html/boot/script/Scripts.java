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
package net.java.html.boot.script;

import java.util.concurrent.Executor;
import javax.script.ScriptEngine;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn.Presenter;

/** Builder to create a {@link Presenter} that delegates
 * to Java {@link ScriptEngine scripting} API. Initialize your presenter
 * like this:
 * <p>
 * {@codesnippet ScriptsTest#initViaBrowserBuilder}
 * 
 * and your runnable can make extensive use of {@link JavaScriptBody} directly or
 * indirectly via APIs using {@link JavaScriptBody such annotation} themselves.
 * <p>
 * Alternatively one can manipulate the presenter manually, which is
 * especially useful when writing tests:
 * <p>
 * {@codesnippet ScriptsTest#activatePresenterDirectly}
 * <p>
 * The previous code snippet relies
 * on try-with-resources <em>Java7</em> syntax. The same block
 * of code can be used on older versions of Java, but it is slightly more
 * verbose.
 * 
 * @author Jaroslav Tulach
 */
public final class Scripts {

    private Executor exc;
    private ScriptEngine engine;
    private boolean sanitize = true;
    
    private Scripts() {
    }
    
    /** {@linkplain #sanitize(boolean) Non-sanitized} version of the presenter.
     * Rather use following code to obtain safer version of the engine:
     * <p> 
     * {@codesnippet ScriptsTest#testNewPresenterNoExecutor}
     * <p>
     * Simple implementation of {@link Presenter} that delegates
     * to Java {@link ScriptEngine scripting} API. The presenter runs headless
     * without appropriate simulation of browser APIs. Its primary usefulness
     * is inside testing environments. The presenter implements {@link Executor}
     * interface, but invokes all runnables passed to {@link Executor#execute(java.lang.Runnable)}
     * immediately.
     * 
     * @return new instance of a presenter that is using its own
     *   {@link ScriptEngine} for <code>text/javascript</code> mimetype
     * @deprecated use {@link #newPresenter()} builder
     */
    @Deprecated
    public static Presenter createPresenter() {
        return newPresenter().sanitize(false).build();
    }

    /** {@linkplain #sanitize(boolean) Non-sanitized} version of the presenter.
     * Rather use following code to obtain safer version of the engine:
     * <p> 
     * {@codesnippet Jsr223JavaScriptTest#createPresenter}
     * <p>
     * Implementation of {@link Presenter} that delegates
     * to Java {@link ScriptEngine scripting} API and can control execution
     * thread. The presenter runs headless
     * without appropriate simulation of browser APIs. Its primary usefulness
     * is inside testing environments. The presenter implements {@link Executor}
     * interface, and passes all runnables from {@link Executor#execute(java.lang.Runnable)}
     * to here in provided <code>exc</code> instance.
     * 
     * @param exc the executor to re-schedule all asynchronous requests to
     * @return new instance of a presenter that is using its own
     *   {@link ScriptEngine} for <code>text/javascript</code> mimetype
     * @deprecated use {@link #newPresenter()} builder
     */
    @Deprecated
    public static Presenter createPresenter(Executor exc) {
        return newPresenter().sanitize(false).executor(exc).build();
    }
    
    /** Creates new scripting {@link Presenter} builder. Simplest way
     * to use is:
     * <p>
     * {@codesnippet ScriptsTest#testNewPresenterNoExecutor}
     * <p>
     * It is possible to specify own 
     * {@link #engine(javax.script.ScriptEngine) scripting engine}
     * and {@link #executor(java.util.concurrent.Executor)}
     * and control the thread that executes the scripts:
     * <p>
     * {@codesnippet Jsr223JavaScriptTest#createPresenter}
     * <p>
     * By default the created presenters are {@linkplain #sanitize(boolean) sanitized}.
     * 
     * @return instance of the new builder
     * @since 1.6.1
     */
    public static Scripts newPresenter() {
        return new Scripts();
    }
    
    /** Associates new executor.
     * The {@linkplain #build() to be created presenter} will implement {@link Executor}
     * interface, and passes all runnables from its own
     * {@link Executor#execute(java.lang.Runnable)} method
     * to here in provided {@code exc} instance of executor.
     * 
     * @param exc dedicated executor to use
     * @return instance of the new builder
     * @since 1.6.1
     */
    public Scripts executor(Executor exc) {
        this.exc = exc;
        return this;
    }
    
    /** Associates a scripting engine.
     * The engine is used to {@link #build() build} an 
     * implementation of {@link Presenter} that delegates
     * to Java {@link ScriptEngine scripting} API. The presenter runs headless
     * without appropriate simulation of browser APIs. 
     * 
     * @param engine dedicated script engine to use
     * @return instance of the new builder
     * @since 1.6.1
     */
    public Scripts engine(ScriptEngine engine) {
        this.engine = engine;
        return this;
    }
    
    /** Turn sandboxing of the engine on or off. When sanitization is on
     * a special care is taken to remove all global symbols not present
     * in the EcmaScript specification. By default the sanitization is on
     * to increase security.
     * 
     * @param yesOrNo do the sanitization or not
     * @return instance of the new builder
     * @since 1.6.1
     */
    public Scripts sanitize(boolean yesOrNo) {
        this.sanitize = yesOrNo;
        return this;
    }
    
    /** Builds new instance of the scripting presenter. Use
     * arguments of this builder and creates new instance.
     * 
     * @return creates new instance of the presenter
     * @since 1.6.1
     */
    public Presenter build() {
        return new ScriptPresenter(engine, exc, sanitize);
    }
}
