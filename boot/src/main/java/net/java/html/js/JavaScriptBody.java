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
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
    
    public boolean wait4js() default true;
}
