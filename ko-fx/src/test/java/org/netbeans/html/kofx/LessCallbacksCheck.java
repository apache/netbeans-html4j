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
package org.netbeans.html.kofx;

import java.io.PrintWriter;
import java.io.StringWriter;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.apidesign.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "LessCalls", properties = {
    @Property(name = "value", type = int.class)
})
public class LessCallbacksCheck {
    private static StringWriter sw;
    
    @ComputedProperty static int plusOne(int value) {
        if (sw == null) {
            sw = new StringWriter();
        }
        new Exception("Who calls me?").printStackTrace(
            new PrintWriter(sw)
        );
        return value + 1;
    }
    
    @KOTest public void dontCallForInitialValueBackToJavaVM() {
        LessCalls m = new LessCalls(10).applyBindings();
        assert m.getPlusOne() == 11 : "Expecting 11: " + m.getPlusOne();
        
        assert sw != null : "StringWriter should be initialized: " + sw;
        
        if (sw.toString().contains("$JsCallbacks$")) {
            assert false : "Don't call for initial value via JsCallbacks:\n" + sw;
        }
    }
}
