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
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 0.5
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
