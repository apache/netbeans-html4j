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

import net.java.html.json.tests.ConvertTypesTest;
import net.java.html.json.tests.KnockoutTest;
import net.java.html.json.tests.JSONTest;
import java.util.ServiceLoader;
import net.java.html.json.Context;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.apidesign.html.json.spi.ContextBuilder;
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
        return VMTest.create({@link KnockoutTCK#testClasses}());
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
    protected abstract Context createContext();
    
    
    /** Gives you list of classes included in the TCK. Send them
     * to {@link VMTest#create(java.lang.Class)} factory method.
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
    

    /** Finds registered implementation of {@link KnockoutTCK} and obtains
     * new context. 
     * 
     * @return context to use for currently running test
     * @throws AssertionError if no context has been found
     */
    public static Context newContext() {
        for (KnockoutTCK tck : ServiceLoader.load(KnockoutTCK.class)) {
            Context c = tck.createContext();
            if (c != null) {
                return c;
            }
        }
        throw new AssertionError("Can't find appropriate Context in ServiceLoader!");
    }
    
}
