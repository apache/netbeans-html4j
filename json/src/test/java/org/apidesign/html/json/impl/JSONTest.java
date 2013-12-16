/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.apidesign.html.json.impl;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JSONTest {
    
    public JSONTest() {
    }

    @Test public void longToStringValue() {
        assertEquals(JSON.stringValue(Long.valueOf(1)), "1");
    }
    
    @Test public void booleanIsSortOfNumber() {
        assertEquals(JSON.numberValue(Boolean.TRUE), Integer.valueOf(1));
        assertEquals(JSON.numberValue(Boolean.FALSE), Integer.valueOf(0));
    }
    
    @Test public void numberToChar() {
        assertEquals(JSON.charValue(65), Character.valueOf('A'));
    }
    @Test public void booleanToChar() {
        assertEquals(JSON.charValue(false), Character.valueOf((char)0));
        assertEquals(JSON.charValue(true), Character.valueOf((char)1));
    }
    @Test public void stringToChar() {
        assertEquals(JSON.charValue("Ahoj"), Character.valueOf('A'));
    }
    @Test public void stringToBoolean() {
        assertEquals(JSON.boolValue("false"), Boolean.FALSE);
        assertEquals(JSON.boolValue("True"), Boolean.TRUE);
    }
    @Test public void numberToBoolean() {
        assertEquals(JSON.boolValue(0), Boolean.FALSE);
        assertEquals(JSON.boolValue(1), Boolean.TRUE);
    }
}
