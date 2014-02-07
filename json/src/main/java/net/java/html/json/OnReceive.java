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
package net.java.html.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Static methods in classes annotated by {@link Model}
 * can be marked by this annotation to establish a 
 * <a href="http://en.wikipedia.org/wiki/JSON">JSON</a>
 * communication point.
 * The associated model class then gets new method to invoke a network
 * connection. Example follows:
 * 
 * <pre>
 * {@link Model @Model}(className="MyModel", properties={
 *   {@link Property @Property}(name = "people", type=Person.class, array=true)
 * })
 * class MyModelImpl {
 *   {@link Model @Model}(className="Person", properties={
 *     {@link Property @Property}(name = "firstName", type=String.class),
 *     {@link Property @Property}(name = "lastName", type=String.class)
 *   })
 *   static class PersonImpl {
 *     {@link ComputedProperty @ComputedProperty}
 *     static String fullName(String firstName, String lastName) {
 *       return firstName + " " + lastName;
 *     }
 *   }
 * 
 *   {@link OnReceive @OnReceive}(url = "{protocol}://your.server.com/person/{name}")
 *   static void getANewPerson(MyModel m, Person p) {
 *     alert("Adding " + p.getFullName() + '!');
 *     m.getPeople().add(p);
 *   }
 * 
 *   // the above will generate method <code>getANewPerson</code> in class <code>MyModel</code>.
 *   // with <code>protocol</code> and <code>name</code> arguments
 *   // which asynchronously contacts the server and in case of success calls
 *   // your {@link OnReceive @OnReceive} with parsed in data
 * 
 *   {@link Function @Function}
 *   static void requestSmith(MyModel m) {
 *     m.getANewPerson("http", "Smith");
 *   }
 * }
 * </pre>
 * When the server returns <code>{ "firstName" : "John", "lastName" : "Smith" }</code>
 * the browser will show alert message <em>Adding John Smith!</em>.
 * <p>
 * One can use this method to communicate with the server
 * via <a href="doc-files/websockets.html">WebSocket</a> protocol since version 0.6.
 * Read the <a href="doc-files/websockets.html">tutorial</a> to see how.
 * <p>
 * Visit an <a target="_blank" href="http://dew.apidesign.org/dew/#7138581">on-line demo</a>
 * to see REST access via {@link OnReceive} annotation.
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 0.3
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OnReceive {
    /** The URL to connect to. Can contain variable names surrounded by '{' and '}'.
     * Those parameters will then become variables of the associated method.
     * 
     * @return the (possibly parametrized) url to connect to
     */
    String url();
    
    /** Support for <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> requires
     * a callback from the server generated page to a function defined in the
     * system. The name of such function is usually specified as a property
     * (of possibly different names). By defining the <code>jsonp</code> attribute
     * one turns on the <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> 
     * transmission and specifies the name of the property. The property should
     * also be used in the {@link #url()} attribute on appropriate place.
     * 
     * @return name of a property to carry the name of <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a>
     *    callback function.
     */
    String jsonp() default "";
    
    /** The model class to be send to the server as JSON data.
     * By default no data are sent. However certain {@link #method() transport methods}
     * (like <code>"PUT"</code> and <code>"POST"</code>) require the 
     * data to be specified.
     * 
     * @return name of a class generated using {@link Model @Model} annotation
     * @since 0.3
     */
    Class<?> data() default Object.class;
    
    /** The HTTP transfer method to use. Defaults to <code>"GET"</code>.
     * Other typical methods include <code>"HEAD"</code>, 
     * <code>"DELETE"</code>, <code>"POST"</code>, <code>"PUT"</code>.
     * The last two mentioned methods require {@link #data()} to be specified.
     * <p>
     * When {@link #jsonp() JSONP} transport is requested, the method 
     * has to be <code>"GET"</code>.
     * <p>
     * Since version 0.5 one can specify "<a href="doc-files/websockets.html">WebSocket</a>"
     * as the communication method.
     * 
     * @return name of the HTTP transfer method
     * @since 0.3
     */
    String method() default "GET";
    
    /** Name of a method in this class which should be called in case of 
     * an error. The method has to be non-private and take one model and 
     * one {@link Exception} 
     * parameter. If this method is not specified, the exception is just
     * printed to console.
     * 
     * @return name of method in this class
     * @since 0.5
     */
    public String onError() default "";    
}
