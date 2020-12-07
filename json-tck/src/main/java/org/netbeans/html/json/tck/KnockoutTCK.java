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
import java.net.URISyntaxException;
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

/**
 * An enhanced, visual <em>Test Compatibility Kit</em> for people providing their own
 * implementation of {@link org.netbeans.html.boot.spi.Fn.Presenter} or any other system that understands
 * {@link net.java.html.js.JavaScriptBody} annotation. The {@link KnockoutTCK}
 * is an extension over {@link JavaScriptTCK} - the <em>headless</em> test
 * compatibility kit. Once the <em>headless</em> functionality works fine,
 * it is time to test the visual aspects - at the end the goal is to run visual
 * <b>Java</b> applications in browser, right?
 * <p>
 * The {@link KnockoutTCK} shall be subclassesed and {@code abstract} methods
 * of the class implemented to provide the necessary environment for execution.
 * A typical way to obtain all the methods to be tested for each of {@link #testClasses} is:
 * <p>
 * {@codesnippet org.netbeans.html.ko4j.KnockoutFXTest}
 * <p>
 * The visual tests interact with browser environment and perform asynchronous
 * network calls. That has two consequences:
 * <ul>
 *  <li>the test may ask you to set a server up via {@link KnockoutTCK#prepareWebResource(java.lang.String, java.lang.String, java.lang.String[])} call -
 *    return URL which the test later connects to
 *  <li>the test may need to wait - in such case it throws {@link InterruptedException} - wait a while
 *    and call the test method again (while keeping the instance and its internal state)
 * </ul>
 * <p>
 * The typical way to execute the visual tests requires one to perform something like:
 * <p>
 * {@codesnippet org.netbeans.html.ko4j.KOFx}
 * e.g. initialize the test instance, run the test method. If it yields an
 * {@link InterruptedException}, run the test again. Should there be no success
 * in a fixed time, give up and fail the test. Succeed otherwise.
 * This is more complicated than running headless {@link JavaScriptTCK} tests,
 * but so is the behavior of typical applications in the browser with access to
 * network.
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
     * @return URL the test can connect to to obtain the (processed) content
     * @since 1.5
     */
    public String prepareWebResource(String content, String mimeType, String[] parameters) {
        return prepareURL(content, mimeType, parameters).toString();
    }

    /** Creates a URL which later returns content with given
     * <code>mimeType</code> and <code>content</code>. The
     * content may be processed by the provided <code>parameters</code>.
     *
     * @param content content to be available
     * @param mimeType mime type of the content
     * @param parameters any parameters to process the content with
     * @return URI to reference the content
     * @deprecated provide {@link #prepareWebResource(java.lang.String, java.lang.String, java.lang.String[])}
     *    implementation instead since post 1.4 version of HTML/Java API.
     */
    @Deprecated
    public URI prepareURL(String content, String mimeType, String[] parameters) {
        try {
            return new URI(prepareWebResource(content, mimeType, parameters));
        } catch (URISyntaxException ex) {
            throw new IllegalStateException();
        }
    }
    
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

    /** Schedules the given runnable to run later.
     *
     * @param delay the delay in milliseconds
     * @param r the runnable to run
     * @return <code>true</code> if the runnable was really scheduled,
     *   <code>false</code> otherwise
     * @since 1.5 version
     */
    public boolean scheduleLater(int delay, Runnable r) {
        return false;
    }

}
