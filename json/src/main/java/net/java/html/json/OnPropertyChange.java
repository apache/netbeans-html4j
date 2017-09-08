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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a method that is going to be notified when a 
 * property defined by {@link Model} has been changed. This is
 * especially useful when one wants to react to changes in the 
 * model caused by the rendered view. In case of 
 * <a href="http://knockoutjs.com">knockout.js</a> technology
 * one could for example react to selection of a name from a combo
 * box:
 * <pre>
 * 
 * &lt;!-- associates the selected value with property <em>name</em> --&gt;
 * 
 * &lt;select data-bind="value: name"&gt;
 *   &lt;option&gt;Jiří&lt;/option&gt;
 *   &lt;option&gt;Jarda&lt;/option&gt;
 *   &lt;option&gt;Petr&lt;/option&gt;
 *   &lt;option&gt;Tomáš&lt;/option&gt;
 * &lt;/select&gt;
 * 
 * // Java code snippet reacting to change of the <em>name</em> property:
 * 
 * {@link OnPropertyChange @OnPropertyChange}("name") 
 * <b>static void</b> propertyChanged(AModel inst, {@link String} propertyName) {
 *   // schedule some operation
 *   // on the model
 * }
 * </pre>
 * The method's first argument should be the instance of the 
 * associated {@link Model model class}. The method shall be non-private
 * and unless {@link Model#instance() instance mode} is on also static.
 * There can be an optional second {@link String} argument which will be set
 * to the name of the changed property. The second argument is only useful when
 * a single method reacts to changes in multiple properties.
 * <p>
 * An online example using this technique is 
 * <a target="_blank" href="http://dew.apidesign.org/dew/#7138581">available here</a> -
 * it observes selection in a combo box and in case it changes 
 * the example sends a network
 * request and {@link net.java.html.json.OnReceive asynchronously updates}
 * list of code snippets.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OnPropertyChange {
    /** Name(s) of the properties. One wishes to observe.
     * 
     * @return valid java identifier
     */
    String[] value();
}
