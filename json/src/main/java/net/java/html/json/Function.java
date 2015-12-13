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
