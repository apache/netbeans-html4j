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
package org.netbeans.html.boot.spi;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
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
 * @author Jaroslav Tulach
 */
public abstract class Fn {
    private final Ref presenter;
    
    /**
     * @deprecated Ineffective as of 0.6. 
     * Provide a presenter via {@link #Fn(org.netbeans.html.boot.spi.Fn.Presenter)}
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
        this.presenter = ref(presenter);
    }

    /** True, if currently active presenter is the same as presenter this
     * function has been created for via {@link #Fn(org.netbeans.html.boot.spi.Fn.Presenter)}.
     * 
     * @return true, if proper presenter is used
     */
    public final boolean isValid() {
        return FnContext.currentPresenter(false) == presenter();
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
        return define(caller, false, code, names);
    }

    /** Helper method to find current presenter and ask it to define new
     * function.
     * 
     * @param caller the class who wishes to define the function
     * @param keepParametersAlive whether Java parameters should survive in JavaScript
     *   after the method invocation is over
     * @param code the body of the function (can reference <code>this</code> and <code>names</code> variables)
     * @param names names of individual parameters
     * @return the function object that can be {@link Fn#invoke(java.lang.Object, java.lang.Object...) invoked}
     *    - can return <code>null</code> if there is {@link #activePresenter() no presenter}
     * @since 1.1
     */
    public static Fn define(Class<?> caller, boolean keepParametersAlive, String code, String... names) {
        final Presenter p = FnContext.currentPresenter(false);
        if (p == null) {
            return null;
        }
        if (p instanceof KeepAlive) {
            boolean[] arr;
            if (!keepParametersAlive && names.length > 0) {
                arr = new boolean[names.length];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = false;
                }
            } else {
                arr = null;
            }
            return ((KeepAlive)p).defineFn(code, names, arr);
        }
        return p.defineFn(code, names);
    }
    
    /** Wraps function to ensure that the script represented by <code>resource</code>
     * gets loaded into the browser environment before the function <code>fn</code>
     * is executed.
     * 
     * @param fn original function to call (if <code>null</code> returns <code>null</code>)
     * @param caller the class who wishes to define/call the function
     * @param resource resources (accessible via {@link ClassLoader#getResource(java.lang.String)}) 
     *   with a <em>JavaScript</em> that is supposed to loaded into the browser
     *   environment
     * @return function that ensures the script is loaded and then delegates
     *   to <code>fn</code>. Returns <code>null</code> if the input <code>fn</code> is null
     * @since 0.7
     */
    public static Fn preload(final Fn fn, final Class<?> caller, final String resource) {
        if (fn == null) {
            return null;
        }
        return new Preload(fn.presenter(), fn, resource, caller);
    }

    
    /** The currently active presenter.
     * 
     * @return the currently active presenter or <code>null</code>
     * @since 0.7
     */
    public static Presenter activePresenter() {
        return FnContext.currentPresenter(false);
    }
    
    /** Activates given presenter. Used to associate the native 
     * JavaScript code specified by 
     * {@link JavaScriptBody} annotation with certain presenter:
     * <pre>
     * try ({@link Closeable} c = Fn.activate(presenter)) {
     *   doCallsInPresenterContext();
     * }
     * </pre>
     * 
     * @param p the presenter that should be active until closable is closed
     * @return the closable to close
     * @throws NullPointerException if the {@code p} is {@code null}
     * @since 0.7
     */
    public static Closeable activate(Presenter p) {
        if (p == null) {
            throw new NullPointerException();
        }
        return FnContext.activate(p);
    }

    /** Obtains a (usually {@linkplain WeakReference weak}) reference to
     * the presenter. Such reference is suitable for embedding in various long
     * living structures with a life-cycle that may outspan the one of presenter.
     *
     * @param p the presenter
     * @return reference to the presenter, {@code null} only if {@code p} is {@code null}
     * @since 1.6.1
     * @see Ref
     */
    public static Ref<?> ref(final Presenter p) {
        if (p == null) {
            return null;
        }
        if (p instanceof Ref<?>) {
            Ref<?> r = ((Ref<?>) p).reference();
            if (r == null) {
                throw new NullPointerException();
            }
            return r;
        }
        return new FallbackIdentity(p);
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

    /** Invokes the defined function with specified <code>this</code> and
     * appropriate arguments asynchronously. The invocation may be 
     * happen <em>"later"</em>.
     * 
     * @param thiz the meaning of <code>this</code> inside of the JavaScript
     *   function - can be <code>null</code>
     * @param args arguments for the function
     * @throws Exception if something goes wrong, as exception may be thrown
     * @since 0.7.6
     */
    public void invokeLater(Object thiz, Object... args) throws Exception {
        invoke(thiz, args);
    }
    
    /** Provides the function implementation access to the presenter provided
     * in {@link #Fn(org.netbeans.html.boot.spi.Fn.Presenter) the constructor}.
     * 
     * @return presenter passed in the constructor (may be, but should not be <code>null</code>)
     * @since 0.7
     */
    protected final Presenter presenter() {
        return presenter == null ? null : presenter.presenter();
    }
    
    /** The representation of a <em>presenter</em> - usually a browser window.
     * Should be provided by a library included in the application and registered
     * in <code>META-INF/services</code>, for example with
     * <code>@ServiceProvider(service = Fn.Presenter.class)</code> annotation.
     * To verify the implementation of the presenter is correct, implement
     * associated TCK (e.g. test compatibility kit) at least the headless
     * one as illustrated at:
     * <p>
     * {@codesnippet net.java.html.boot.script.ScriptEngineJavaScriptTCK}
     * 
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

    /** Additional interface to {@link Presenter} to control more precisely
     * garbage collection behavior of individual parameters. See 
     * {@link JavaScriptBody#keepAlive()} attribute for description of the
     * actual behavior of the interface.
     * 
     * @since 1.1
     */
    public interface KeepAlive {
        /** Creates new function with given parameter names and provided body.
         * 
         * @param code the body of the function. Can refer to variables named
         *   as <code>names</code>
         * @param names names of parameters of the function - these will be 
         *   available when the <code>code</code> body executes
         * @param keepAlive array of booleans describing for each parameter
         *   whether it should be kept alive or not. Length of the array
         *   must be the same as length of <code>names</code> array. The
         *   array may be <code>null</code> to signal that all parameters
         *   should be <em>kept alive</em>.
         * 
         * @return function that can be later invoked
         */
        public Fn defineFn(String code, String[] names, boolean[] keepAlive);
    }

    /** Represents a JavaScript Promise.
     * XXX:
     * @since 1.8
     */
    public static final class Promise {
        private static final Fn INVOKE = Fn.define(Promise.class, """
        return fn.call(this, result);
        """, "fn", "result");
        private final Object result;

        public Promise(Object result) {
            this.result = result;
        }

        public Promise then(Object fn) {
            try {
                return new Promise(INVOKE.invoke(this, fn, result));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Reference to a {@link Presenter}.Each implementation of a {@link Presenter}
     * may choose a way to reference itself (usually in a {@linkplain WeakReference weak way})
     * effectively. Various code that needs to hold a reference to a presenter
     * is then encouraged to obtain such reference via {@link Fn#ref(org.netbeans.html.boot.spi.Fn.Presenter)}
     * call and hold on to it. Holding a reference to an instance of {@link Presenter}
     * is discouraged as it may lead to memory leaks.
     * <p>
     * Presenters willing to to represent a reference to itself effectively shall
     * also implement the {@link Ref} interface and return reasonable reference
     * from the {@link #reference()} method.
     *
     * @param <P> the type of the presenter
     * @see Fn#ref(org.netbeans.html.boot.spi.Fn.Presenter)
     * @since 1.6.1
     */
    public interface Ref<P extends Presenter> {
        /** Creates a reference to a presenter.
         * Rather than calling this method directly, call {@link Fn#ref(org.netbeans.html.boot.spi.Fn.Presenter)}.
         * @return a (weak) reference to the associated presenter
         */
        public Ref<P> reference();

        /** The associated presenter.
         *
         * @return the presenter or {@code null}, if it has been GCed meanwhile
         */
        public P presenter();

        /** Reference must properly implement {@link #hashCode} and {@link #equals}.
         *
         * @return proper hashcode
         */
        @Override
        public int hashCode();

        /** Reference must properly implement {@link #hashCode} and {@link #equals}.
         *
         * @return proper equals result
         */
        @Override
        public boolean equals(Object obj);
    }

    private static class Preload extends Fn {
        private static Map<String, Set<Ref>> LOADED;
        private final Fn fn;
        private final String resource;
        private final Class<?> caller;

        Preload(Presenter presenter, Fn fn, String resource, Class<?> caller) {
            super(presenter);
            this.fn = fn;
            this.resource = resource;
            this.caller = caller;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            loadResource();
            return fn.invoke(thiz, args);
        }

        @Override
        public void invokeLater(Object thiz, Object... args) throws Exception {
            loadResource();
            fn.invokeLater(thiz, args);
        }

        private void loadResource() throws Exception {
            Ref id = super.presenter;
            if (id == null) {
                id = ref(FnContext.currentPresenter(false));
            }
            Fn.Presenter realPresenter = id == null ? null : id.presenter();
            if (realPresenter != null) {
                if (LOADED == null) {
                    LOADED = new HashMap<String, Set<Ref>>();
                }
                Set<Ref> there = LOADED.get(resource);
                if (there == null) {
                    there = new HashSet<Ref>();
                    LOADED.put(resource, there);
                }
                if (there.add(id)) {
                    final ClassLoader l = caller.getClassLoader();
                    InputStream is = l.getResourceAsStream(resource);
                    if (is == null && resource.startsWith("/")) {
                        is = l.getResourceAsStream(resource.substring(1));
                    }
                    if (is == null) {
                        throw new IOException("Cannot find " + resource + " in " + l);
                    }
                    try {
                        InputStreamReader r = new InputStreamReader(is, "UTF-8");
                        realPresenter.loadScript(r);
                    } finally {
                        is.close();
                    }
                }
            }
        }
    }

}
