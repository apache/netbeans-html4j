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
package org.netbeans.html.json.tck;

import java.io.StringReader;
import net.java.html.js.tests.AsyncJavaTest;
import net.java.html.js.tests.GCBodyTest;
import net.java.html.js.tests.JavaScriptBodyTest;
import net.java.html.js.tests.ExposedPropertiesTest;
import net.java.html.js.tests.JsUtils;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/** Basic <em>Test Compatibility Kit</em> for people providing their own
 * implementation of {@link Presenter} or any other system that understands
 * {@link net.java.html.js.JavaScriptBody} annotation. The {@link JavaScriptTCK}
 * tests are <em>headless</em> - e.g. they don't need access to browser APIs
 * like DOM. It is possible to execute them in <em>node</em>
 * or in a plain {@link javax.script.ScriptEngine} environment. The tests are focused
 * on Java/JavaScript interactions, calls and exchange of objects between the
 * two worlds. See {@link KnockoutTCK} for UI oriented tests.
 * <p>
 * Implement your system, setup your <em>headless</em> environment for
 * execution of JavaScript and execute the tests. There are the steps to follow:
 * <ul>
 *  <li>subclass this class</li>
 *  <li>get list of {@link #testClasses() test classes}</li>
 *  <li>find their methods annotated by {@link KOTest} annotation</li>
 *  <li>execute them</li>
 * </ul>
 * <p>
 * Typical way to iterate through all the test methods looks like this:
 * <p>
 * {@codesnippet net.java.html.boot.script.ScriptEngineJavaScriptTCK}
 * <p>
 * by subclassing {@link JavaScriptTCK} one gets access to {@code protected}
 * method {@link JavaScriptTCK#testClasses} and can obtain all the TCK classes.
 * One can use any <em>factory</em> and create objects suitable for any
 * testing framework. Typical invocation of a single test then looks like
 * <p>
 * {@codesnippet net.java.html.boot.script.ScriptEngineCase#run}
 * e.g. one creates an instance of the object and invokes its test {@code method}.
 * <p>
 * When the test provided by this <em>headless TCK</em> are passing,
 * consider also testing your environment on a
 * {@link KnockoutTCK visual DOM-based TCK}.
 *
 * @author Jaroslav Tulach
 * @see KnockoutTCK
 * @since 0.7
 */
public abstract class JavaScriptTCK {
    /** Creates and registers instance of the TCK. */
    public JavaScriptTCK() {
        JsUtils.registerTCK(this);
    }

    /** Gives you list of classes included in the TCK. Their test methods
     * are annotated by {@link KOTest} annotation. The methods are public
     * instance methods that take no arguments. The method should be
     * invoke in a presenter context {@link Fn#activate(org.netbeans.html.boot.spi.Fn.Presenter)}.
     *
     * @return classes with methods annotated by {@link KOTest} annotation
     */
    protected static Class<?>[] testClasses() {
        return new Class[] {
            JavaScriptBodyTest.class, GCBodyTest.class, ExposedPropertiesTest.class, AsyncJavaTest.class
        };
    }

    /** Executes JavaScript now. Simulates that something suddenly happens
     * in the JavaScript while Java code may already be doing something
     * different. If it is not possible to execute the JavaScript - for
     * example the JavaScript engine is blocked by currently running request,
     * then return {@code false} and let the TCK test terminate gracefully.
     *
     * @param script the script to execute in the JavaScript
     * @return {@code true} if the script was executed, {@code false} if it couldn't be
     * @throws Exception if the script contains an error
     * @since 1.7.1
     */
    public boolean executeNow(String script) throws Exception {
        Presenter p = Fn.activePresenter();
        p.loadScript(new StringReader(script));
        return true;
    }
}
