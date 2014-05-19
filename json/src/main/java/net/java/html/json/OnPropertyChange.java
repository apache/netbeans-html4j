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
 * associated {@link Model model class}.
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
