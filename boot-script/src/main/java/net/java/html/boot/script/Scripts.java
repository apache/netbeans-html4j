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

import java.io.Closeable;
import java.util.concurrent.Executor;
import javax.script.ScriptEngine;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/** Implementations of {@link Presenter}s that delegate
 * to Java {@link ScriptEngine scripting} API. Initialize your presenter
 * like this:
 * 
 * <pre>
 * 
 * {@link Runnable} <em>run</em> = ...; // your own init code
 * {@link Presenter Fn.Presenter} <b>p</b> = Scripts.{@link Scripts#createPresenter()};
 * BrowserBuilder.{@link BrowserBuilder#newBrowser(java.lang.Object...) newBrowser(<b>p</b>)}.
 *      {@link BrowserBuilder#loadFinished(java.lang.Runnable) loadFinished(run)}.
 *      {@link BrowserBuilder#showAndWait()};
 * </pre>
 * 
 * and your runnable can make extensive use of {@link JavaScriptBody} directly or
 * indirectly via APIs using {@link JavaScriptBody such annotation} themselves.
 * <p>
 * Alternatively one can manipulate the presenter manually, which is
 * especially useful when writing tests:
 * <pre>
 * {@code @Test} public void runInASimulatedBrowser() throws Exception {
 *   {@link Presenter Fn.Presenter} <b>p</b> = Scripts.{@link Scripts#createPresenter()};
 *   try ({@link Closeable} c = {@link Fn#activate(org.netbeans.html.boot.spi.Fn.Presenter) Fn.activate}(<b>p</b>)) {
 *     // your code operating in context of <b>p</b>
 *   }
 * }
 * </pre>
 * The previous code snippet requires Java 7 language syntax, as it relies
 * on try-with-resources language syntactic sugar feature. The same block
 * of code can be used on older versions of Java, but it is slightly more
 * verbose.
 * 
 * @author Jaroslav Tulach
 */
public final class Scripts {
    private Scripts() {
    }
    
    /** Simple implementation of {@link Presenter} that delegates
     * to Java {@link ScriptEngine scripting} API. The presenter runs headless
     * without appropriate simulation of browser APIs. Its primary usefulness
     * is inside testing environments. The presenter implements {@link Executor}
     * interface, but invokes all runnables passed to {@link Executor#execute(java.lang.Runnable)}
     * immediately.
     * 
     * @return new instance of a presenter that is using its own
     *   {@link ScriptEngine} for <code>text/javascript</code> mimetype
     */
    public static Presenter createPresenter() {
        return new ScriptPresenter(null);
    }

    /** Implementation of {@link Presenter} that delegates
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
     */
    public static Presenter createPresenter(Executor exc) {
        return new ScriptPresenter(exc);
    }
}
