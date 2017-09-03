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
