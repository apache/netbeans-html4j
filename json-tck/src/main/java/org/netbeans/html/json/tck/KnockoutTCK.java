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

import java.net.URI;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.tests.ConvertTypesTest;
import net.java.html.json.tests.GCKnockoutTest;
import net.java.html.json.tests.JSONTest;
import net.java.html.json.tests.KnockoutTest;
import net.java.html.json.tests.MinesTest;
import net.java.html.json.tests.OperationsTest;
import net.java.html.json.tests.Utils;
import net.java.html.json.tests.WebSocketTest;
import org.netbeans.html.context.spi.Contexts.Builder;
import org.openide.util.lookup.ServiceProvider;
import org.testng.annotations.Factory;

/** Entry point for providers of different HTML binding technologies (like
 * Knockout.js in JavaFX's WebView). Sample usage:
 * 
<pre>
{@link ServiceProvider @ServiceProvider}(service = KnockoutTCK.class)
public final class MyKnockoutBindingTest extends KnockoutTCK {
    {@link Override @Override}
    protected BrwsrCtx createContext() {
        // use {@link Builder}.{@link Builder#build() build}();
    }

    {@code @}{@link Factory} public static Object[] create() {
        return VMTest.newTests().withClasses({@link KnockoutTCK#testClasses}()).build();
    }
}
 * </pre>
 *
 * @author Jaroslav Tulach
 */
public abstract class KnockoutTCK {
    protected KnockoutTCK() {
        Utils.registerTCK(this);
    }
    
    /** Implement to create new context for the test. 
     * Use {@link Builder} to set context for your technology up.
     * @return the final context for the test
     */
    public abstract BrwsrCtx createContext();
    
    /** Create a JSON object as seen by the technology
     * @param values mapping from names to values of properties
     * @return the JSON object with filled in values
     */
    public abstract Object createJSON(Map<String,Object> values);

    /** Executes script in the context of current window
     * 
     * @param script the JavaScript code to execute
     * @param arguments arguments sent to the script (can be referenced as <code>arguments[0]</code>)
     * @return the output of the execution
     */
    public abstract Object executeScript(String script, Object[] arguments);

    /** Creates a URL which later returns content with given
     * <code>mimeType</code> and <code>content</code>. The 
     * content may be processed by the provided <code>parameters</code>.
     * 
     * @param content what should be available on the URL. Can contain <code>$0</code>
     *   <code>$1</code> to reference <code>parameters</code> by their position
     * @param mimeType the type of the resource
     * @param parameters names of parameters as reference by <code>content</code>
     * @return URI the test can connect to to obtain the (processed) content
     */
    public abstract URI prepareURL(String content, String mimeType, String[] parameters);
    
    /** Gives you list of classes included in the TCK. Their test methods
     * are annotated by {@link KOTest} annotation. The methods are public
     * instance methods that take no arguments.
     * 
     * @return classes with methods annotated by {@link KOTest} annotation
     */
    protected static Class<?>[] testClasses() {
        return new Class[] { 
            ConvertTypesTest.class,
            JSONTest.class,
            KnockoutTest.class,
            MinesTest.class,
            OperationsTest.class,
            WebSocketTest.class,
            GCKnockoutTest.class
        };
    }

    /** Some implementations cannot fully support web sockets and fail.
     * 
     * @return true, if UnsupportedOperationException reported from a web
     *    socket open operation is acceptable reply
     */
    public boolean canFailWebSocketTest() {
        return false;
    }


}
