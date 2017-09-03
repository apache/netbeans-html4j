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
package net.java.html.json;

import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.html.json.spi.FunctionBinding;

/** Methods in class annotated by {@link Model} can be 
 * annotated by this annotation to signal that they should be available
 * as functions to users of the model classes. The method
 * should be non-private, static (unless {@link Model#instance() instance mode} is on)
 * and return <code>void</code>.
 * It may take few arguments. The first argument can be the type of
 * the associated model class, the other argument can be of any type,
 * but has to be named <code>data</code> - this one represents the
 * actual data the function was invoked on. Example:
 * <pre>
 * 
 * {@link Model @Model}(className="Names", properties={
 *   {@link Property @Property}(name = "selectedName", type=String.class),
 *   {@link Property @Property}(name = "names", type=String.class, array = true)
 * })
 * static class NamesModel {
 *   {@link Function @Function} static void <b>nameSelected</b>(Names myModel, String data) {
 *     myModel.setSelectedName(data);
 *   }
 * 
 *   static void initialize() {
 *     Names pageModel = new Names("---", "Jarda", "Pepa", "Honza", "Jirka", "Tomáš");
 *     pageModel.applyBindings();
 *   }
 * }
 * 
 * // associated <a target="_blank" href="http://knockoutjs.com/">Knockout</a> HTML page:
 * 
 * Selected name: &lt;span data-bind="text: selectedName"&gt;&lt;/span&gt;
 * &lt;ul data-bind="foreach: names"&gt;
 *   &lt;li&gt;
 *     &lt;a data-bind="text: $data, click: $root.nameSelected" href="#"&gt;&lt;/a&gt;
 *   &lt;/li&gt;
 * &lt;/ul&gt; 
 * </pre>
 * The above example would render:
 * <hr>
 * Selected name: <span>---</span>
 * <ul>
 *   <li>Jarda</li>
 *   <li>Pepa</li>
 *   <li>Honza</li>
 *   <li>Jirka</li>
 *   <li>Tomáš</li>
 * </ul>
 * <hr>
 * and after clicking on one of the names the <code>---</code> would be replaced
 * by selected name. 
 * Try <a target="_blank" href="http://dew.apidesign.org/dew/#8848505">this sample on-line</a>!
 * <p>
 * There can be additional arguments in the method which can extract information
 * from (typically event object) sent as a second parameter of the function
 * {@link FunctionBinding#call(java.lang.Object, java.lang.Object) dispatch method}.
 * Such arguments can be of primitive types (<code>int</code>, <code>double</code>
 * or {@link String}). Their names are used to extract values of appropriate
 * properties from the event object. The following function...
 * <pre>
 * {@link Function @Function} static void <b>meaningOfWorld</b>(Names myModel, String data, int answer) {
 *   {@link System}.out.{@link PrintStream#println(int) println}(answer);
 * }
 * // would print <b>42</b> if the dispatch method:
 * {@link FunctionBinding#call(java.lang.Object, java.lang.Object) meaningOfWorld.call}(model, json)
 * </pre>
 * is called with equivalent of <code>var json = { 'answer' : 42 }</code>.
 * 
 * @author Jaroslav Tulach
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Function {
}
