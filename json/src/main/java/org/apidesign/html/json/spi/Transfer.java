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
package org.apidesign.html.json.spi;

import java.io.IOException;
import java.io.InputStream;
import org.apidesign.html.context.spi.Contexts.Builder;

/** A {@link Builder service provider interface} responsible for 
 * conversion of JSON objects to Java ones and vice-versa.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public interface Transfer {
    /**
     * Called to inspect properties of an object (usually a JSON or JavaScript
     * wrapper).
     *
     * @param obj the object to inspect
     * @param props the names of properties to check on the object
     * <code>obj</code>
     * @param values array of the same length as <code>props</code> should be
     * filled by values of properties on the <code>obj</code>. If a property is
     * not defined, a <code>null</code> value should be stored in the array
     */
    public void extract(Object obj, String[] props, Object[] values);
    
    /** Reads content of a stream and creates its JSON representation.
     * The returned object is implementation dependant. It however needs
     * to be acceptable as first argument of {@link #extract(java.lang.Object, java.lang.String[], java.lang.Object[]) extract}
     * method.
     * 
     * @param is input stream to read data from
     * @return an object representing the JSON data
     * @throws IOException if something goes wrong
     */
    public Object toJSON(InputStream is) throws IOException;
    
    /** Starts the JSON or JSONP query. 
     * 
     * @param call description of the call to make
     */
    public void loadJSON(JSONCall call);
    
}
