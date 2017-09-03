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
package net.java.html.js;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Put this annotation on a method to provide its special implementation
 * in JavaScript. This is a way to define <em>native</em> methods that 
 * interact with the surrounding JavaScript environment. Check the list
 * <a href="package-summary.html">use-cases</a> to see real world
 * use of this annotation.
 * <p>
 * Visit an <a target="_blank" href="http://dew.apidesign.org/dew/#7102188">on-line demo</a>
 * to play with {@link JavaScriptBody} annotation for real.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface JavaScriptBody {
    /** Names of parameters for the method generated method that can
     * be referenced from {@link #body()}.
     * 
     * @return array of the names of parameters for the method
     *    in JavaScript
     */
    public String[] args();
    
    /** The actual body of the method in JavaScript. This string will be
     * put into generated header (last character is '{') and footer (e.g. '}').
     * The body can reference provided arguments. In case of non-static
     * instance method it may reference <code>this</code>. 
     * 
     * @return JavaScript body of a function which can access {@link #args()} and possibly
     * <code>this</code>
     */
    public String body();

    /** Should a special syntax for calling back into Java object be turned on?
     * The syntax begins with <b>{@code @}</b> followed by fully qualified
     * package name of the class. Now followed by <b>::</b> and a method in
     * the class followed by its parameters enclosed inside <b>(...)</b>.
     * This is the syntax one can use to call <code>run()</code> 
     * method of {@link Runnable}:
     * <pre>r.@java.lang.Runnable::run()()</pre>.
     * One can also call static methods. Just use:
     * <pre>var ten = @java.lang.Integer::parseInt(Ljava/lang/String;)("10")</pre>
     * 
     * @return true, if the script should be scanned for special callback
     *   syntax
     */
    public boolean javacall() default false;

    /** Should we wait before the JavaScript snippet execution finishes?
     * Or not. 
     * <p>
     * Some implementations that recognize the {@link JavaScriptBody} annotation
     * need to reschedule the JavaScript execution into different thread and
     * then it is easier for them to perform the execution asynchronously
     * and not wait for the result of the execution. This may however be
     * unexpected (for example when one awaits a callback into Java)
     * and as such it has to be explicitly allowed by specifying
     * <code>wait4js = false</code>. Such methods need to return <code>void</code>.
     * <p>
     * Implementations that execute the JavaScript synchronously may ignore
     * this attribute.
     * <p>
     * Implementations that delay execution of JavaScript need to guarantee
     * the order of snippets. Those that were submitted sooner, need to be
     * executed sooner. Each snippet need to be executed in a timely manner
     * (e.g. by a second, or so) even if there are no other calls made
     * in the main program.
     * <p>
     * 
     * @since 0.7.6
     * @return <code>false</code> in case one allows asynchronous execution
     *   of the JavaScript snippet
     */
    public boolean wait4js() default true;
    
    /** Controls garbage collection behavior of method parameters.
     * In general JavaScript garbage
     * collection system makes it close to impossible to find out whether
     * an object is supposed to be still used or not. Some systems have
     * an external hooks to find that out (like <em>JavaFX</em> <code>WebView</code>),
     * in some systems this information is not important (like the 
     * <a href="http://bck2brwsr.apidesign.org">Bck2Brwsr</a> VM running
     * all in JavaScript), but other execution systems just can't find that
     * out. To prevent memory leaks on such systems and help them manage
     * memory more effectively, those who define JavaScript interfacing 
     * methods may indicate whether the non-primitive parameters passed
     * in should be hold only for the time of method invocation or 
     * for the whole application lifetime.
     * <p>
     * The default value is <code>true</code> as that is compatible with
     * previous behavior and also prevents unwanted surprises when something
     * garbage collects pre-maturaly. Framework developers are however 
     * encouraged to use <code>keepAlive=false</code> as much as possible.
     * 
     * @return whether Java objects passed as parameters of the method
     *   should be made guaranteed to be available JavaScript
     *   even after the method invocation is over (e.g. prevent them to be
     *   garbage collected in Java until it is known they are not needed
     *   from JavaScript at all).
     * 
     * @since 1.1
     */
    public boolean keepAlive() default true;
}
