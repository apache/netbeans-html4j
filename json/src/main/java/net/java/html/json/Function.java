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

/** Methods in class annotated by {@link Model} can be 
 * annotated by this annotation to signal that they should be available
 * as functions to users of the model classes. The method
 * should be non-private, static and return <code>void</code>.
 * It may take up to two arguments. One argument can be the type of
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
 * // associated <a href="http://knockoutjs.com/">Knockout</a> HTML page:
 * 
 * Selected name: &lt;span data-bind="text: selectedName">&lt;/span&gt;
 * &lt;ul data-bind="foreach: names"&gt;
 *   &lt;li data-bind="text: $data, click: <b>$root.nameSelected</b>">&lt;/li&gt;
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
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Function {
}
