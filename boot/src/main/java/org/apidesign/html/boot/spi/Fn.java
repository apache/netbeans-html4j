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
package org.apidesign.html.boot.spi;

import java.io.Reader;
import java.net.URL;
import org.apidesign.html.boot.impl.FnContext;

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
        return FnContext.currentPresenter() == presenter;
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

    /** The representation of a <em>presenter</em> - usually a browser window.
     * Should be provided by a library included in the application and registered
     * in <code>META-INF/services</code>, for example with
     * <code>@ServiceProvider(service = Fn.Presenter.class)</code> annotation.
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
}
