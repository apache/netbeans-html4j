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
package org.apidesign.html.boot.spi;

import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.impl.FnContext;

/** Represents single JavaScript function that can be invoked. 
 * Created via {@link Presenter#defineFn(java.lang.String, java.lang.String...)}.
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public abstract class Fn {
    private final Presenter presenter;
    
    /**
     * @deprecated Ineffective as of 0.6. 
     * Provide a presenter via {@link #Fn(org.apidesign.html.boot.spi.Fn.Presenter)}
     * constructor
     */
    @Deprecated
    protected Fn() {
        this(null);
    }
    
    /** Creates new function object and associates it with given presenter.
     * 
     * @param presenter the browser presenter associated with this function
     * @since 0.6 
     */
    protected Fn(Presenter presenter) {
        this.presenter = presenter;
    }

    /** True, if currently active presenter is the same as presenter this
     * function has been created for via {@link #Fn(org.apidesign.html.boot.spi.Fn.Presenter)}.
     * 
     * @return true, if proper presenter is used
     */
    public final boolean isValid() {
        return presenter != null && FnContext.currentPresenter(false) == presenter;
    }
    
    /** Helper method to check if the provided instance is valid function.
     * Checks if the parameter is non-null and if so, does {@link #isValid()}
     * check.
     * 
     * @param fnOrNull function or <code>null</code>
     * @return true if the parameter is non-null and valid
     * @since 0.7
     */
    public static boolean isValid(Fn fnOrNull) {
        return fnOrNull != null && fnOrNull.isValid();
    }

    /** Helper method to find current presenter and ask it to define new
     * function by calling {@link Presenter#defineFn(java.lang.String, java.lang.String...)}.
     * 
     * @param caller the class who wishes to define the function
     * @param code the body of the function (can reference <code>this</code> and <code>names</code> variables)
     * @param names names of individual parameters
     * @return the function object that can be {@link Fn#invoke(java.lang.Object, java.lang.Object...) invoked}
     *    - can return <code>null</code> if there is {@link #activePresenter() no presenter}
     * @since 0.7
     */
    public static Fn define(Class<?> caller, String code, String... names) {
        final Presenter p = FnContext.currentPresenter(false);
        return p == null ? null : p.defineFn(code, names);
    }
    
    private static final Map<String,Set<Presenter>> LOADED = new HashMap<String, Set<Presenter>>();
    
    /** Wraps function to ensure that the script represented by <code>resource</code>
     * gets loaded into the browser environment before the function <code>fn</code>
     * is executed.
     * 
     * @param fn original function to call
     * @param caller the class who wishes to define/call the function
     * @param resource resources (accessible via {@link ClassLoader#getResource(java.lang.String)}) 
     *   with a <em>JavaScript</em> that is supposed to loaded into the browser
     *   environment
     * @return function that ensures the script is loaded and then delegates
     *   to <code>fn</code>
     * @since 0.7
     */
    public static Fn preload(final Fn fn, final Class<?> caller, final String resource) {
        return new Fn(fn.presenter()) {
            @Override
            public Object invoke(Object thiz, Object... args) throws Exception {
                final Presenter p = FnContext.currentPresenter(false);
                Set<Presenter> there = LOADED.get(resource);
                if (there == null) {
                    there = new HashSet<Presenter>();
                    LOADED.put(resource, there);
                }
                if (there.add(p)) {
                    InputStream is = caller.getClassLoader().getResourceAsStream(resource);
                    try {
                        InputStreamReader r = new InputStreamReader(is, "UTF-8");
                        p.loadScript(r);
                    } finally {
                        is.close();
                    }
                }
                return fn.invoke(thiz, args);
            }
        };
    }
    
    /** The currently active presenter.
     * 
     * @return the currently active presenter or <code>null</code>
     * @since 0.7
     */
    public static Presenter activePresenter() {
        return FnContext.currentPresenter(false);
    }
    
    /** Activates given presenter. Used by the code generated by 
     * {@link JavaScriptBody} annotation: 
     * <pre>
     * try ({@link Closeable} c = Fn.activate(presenter)) {
     *   doCallsInPresenterContext();
     * }
     * </pre>
     * 
     * @param p the presenter that should be active until closable is closed
     * @return the closable to close
     * @since 0.7
     */
    public static Closeable activate(Presenter p) {
        return FnContext.activate(p);
    }
    
    /** Invokes the defined function with specified <code>this</code> and
     * appropriate arguments.
     * 
     * @param thiz the meaning of <code>this</code> inside of the JavaScript
     *   function - can be <code>null</code>
     * @param args arguments for the function
     * @return return value from the function
     * @throws Exception if something goes wrong, as exception may be thrown
     */
    public abstract Object invoke(Object thiz, Object... args) throws Exception;
    
    /** Provides the function implementation access to the presenter provided
     * in {@link #Fn(org.apidesign.html.boot.spi.Fn.Presenter) the constructor}.
     * 
     * @return presenter passed in the constructor (may be, but should not be <code>null</code>)
     * @since 0.7
     */
    protected final Presenter presenter() {
        return presenter;
    }

    /** The representation of a <em>presenter</em> - usually a browser window.
     * Should be provided by a library included in the application and registered
     * in <code>META-INF/services</code>, for example with
     * <code>@ServiceProvider(service = Fn.Presenter.class)</code> annotation.
     * <p>
     * Since 0.7 a presenter may implement {@link Executor} interface, in case
     * it supports single threaded execution environment. The executor's
     * {@link Executor#execute(java.lang.Runnable)} method is then supposed
     * to invoke the runnable immediately (in case we are on the right thread
     * already) or return and asynchronously invoke the runnable later on the
     * right thread (if we are on wrong thread).
     */
    public interface Presenter {
        /** Creates new function with given parameter names and provided body.
         * 
         * @param code the body of the function. Can refer to variables named
         *   as <code>names</code>
         * @param names names of parameters of the function - these will be 
         *   available when the <code>code</code> body executes
         * 
         * @return function that can be later invoked
         */
        public Fn defineFn(String code, String... names);
        
        /** Opens the browser, loads provided page and when the
         * page is ready, it calls back to the provider runnable.
         * 
         * @param page the URL for the page to display
         * @param onPageLoad callback when the page is ready
         */
        public void displayPage(URL page, Runnable onPageLoad);
        
        /** Loads a script into the browser JavaScript interpreter and 
         * executes it.
         * @param code the script to execute
         * @throws Exception if something goes wrong, throw an exception
         */
        public void loadScript(Reader code) throws Exception;
    }
    
    /** Additional interface to be implemented by {@link Presenter}s that
     * wish to control what objects are passed into the JavaScript virtual 
     * machine.
     * <p>
     * If a JavaScript engine makes callback to Java method that returns 
     * a value, the {@link #toJavaScript(java.lang.Object)} method is
     * consulted to convert the Java value to something reasonable inside
     * JavaScript VM.
     * <p>
     * <em>Note:</em> The implementation based on <em>JavaFX</em> <code>WebView</code>
     * uses this interface to convert Java arrays to JavaScript ones.
     * 
     * @see Presenter
     * @since 0.7
     */
    public interface ToJavaScript {
        /** Convert a Java return value into some object suitable for
         * JavaScript virtual machine.
         * 
         * @param toReturn the Java object to be returned
         * @return the replacement value to return instead
         */
        public Object toJavaScript(Object toReturn);
    }
    
    /** Additional interface to be implemented by {@link Presenter}s that
     * need to convert JavaScript object (usually array) to Java object 
     * when calling back from JavaScript to Java.
     * <p>
     * <em>Note:</em> The implementation based on <em>JavaFX</em>
     * <code>WebView</code> uses this interface to convert JavaScript arrays to
     * Java ones.
      * 
     * @since 0.7
     */
    public interface FromJavaScript {
        /** Convert a JavaScript object into suitable Java representation
         * before a Java method is called with this object as an argument.
         * 
         * @param js the JavaScript object
         * @return replacement object for 
         */
        public Object toJava(Object js);
    }
}
