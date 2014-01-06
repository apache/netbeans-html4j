/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package org.apidesign.html.json.tck;

import java.net.URI;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.tests.ConvertTypesTest;
import net.java.html.json.tests.JSONTest;
import net.java.html.json.tests.KnockoutTest;
import net.java.html.json.tests.OperationsTest;
import net.java.html.json.tests.Utils;
import net.java.html.json.tests.WebSocketTest;
import org.openide.util.lookup.ServiceProvider;

/** Entry point for providers of different HTML binding technologies (like
 * Knockout.js in JavaFX's WebView). Sample usage:
 * <pre>
{@link ServiceProvider @ServiceProvider}(service = KnockoutTCK.class)
public final class MyKnockoutBindingTest extends KnockoutTCK {
    {@link Override @Override}
    protected BrwsrCtx createContext() {
        // use {@link ContextBuilder}.{@link ContextBuilder#build() build}();
    }

    {@code @Factory} public static Object[] create() {
        return VMTest.newTests().withClasses({@link KnockoutTCK#testClasses}()).build();
    }
}
 * </pre>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class KnockoutTCK {
    protected KnockoutTCK() {
        Utils.registerTCK(this);
    }
    
    /** Implement to create new context for the test. 
     * Use {@link ContextBuilder} to implement context for your technology.
     */
    public abstract BrwsrCtx createContext();
    
    /** Create a JSON object as seen by the technology
     * @param values mapping from names to values of properties
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
     * @param content
     * @param mimeType
     * @param parameters
     * @return 
     */
    public abstract URI prepareURL(String content, String mimeType, String[] parameters);
    
    /** Gives you list of classes included in the TCK. Their test methods
     * are annotated by {@link KOTest} annotation. The methods are public
     * instance methods that take no arguments.
     * 
     * @return classes with methods annotated by {@link BrwsrTest} annotation
     */
    protected static Class<?>[] testClasses() {
        return new Class[] { 
            ConvertTypesTest.class,
            JSONTest.class,
            KnockoutTest.class,
            OperationsTest.class,
            WebSocketTest.class
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


}
