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
package org.apidesign.html.json.tck;

import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.tests.ConvertTypesTest;
import net.java.html.json.tests.KnockoutTest;
import net.java.html.json.tests.JSONTest;
import org.openide.util.lookup.ServiceProvider;

/** Entry point for providers of different HTML binding technologies (like
 * Knockout.js in bck2brwsr or in JavaFX's WebView). Sample usage:
 * <pre>
{@link ServiceProvider @ServiceProvider}(service = KnockoutTCK.class)
public final class Bck2BrwsrKnockoutTest extends KnockoutTCK {
    {@link Override @Override}
    protected Context createContext() {
        // use {@link ContextBuilder}.{@link ContextBuilder#build() build}();
    }

    {@code @Factory} public static Object[] create() {
        return VMTest.newTests().withClasses({@link KnockoutTCK#testClasses}()).build();
    }
}
 * </pre>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class KnockoutTCK {
    protected KnockoutTCK() {
    }
    
    /** Implement to create new context for the test. 
     * Use {@link ContextBuilder} to implement context for your technology.
     */
    public abstract BrwsrCtx createContext();
    
    /** Create a JSON object as seen by the technology
     * @param values mapping from names to values of properties
     */
    public abstract Object createJSON(Map<String,Object> values);

    /** Executes script in the context of current window
     * 
     * @param script the JavaScript code to execute
     * @param arguments arguments sent to the script (can be referenced as <code>arguments[0]</code>)
     * @return the output of the execution
     */
    public abstract Object executeScript(String script, Object[] arguments);
    
    /** Gives you list of classes included in the TCK. Their test methods
     * are annotated by {@link KOTest} annotation. The methods are public
     * instance methods that take no arguments.
     * 
     * @return classes with methods annotated by {@link BrwsrTest} annotation
     */
    protected static Class<?>[] testClasses() {
        return new Class[] { 
            ConvertTypesTest.class,
            JSONTest.class,
            KnockoutTest.class
        };
    }

    
}
