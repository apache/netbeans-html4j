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

import static org.testng.Assert.*;
import org.testng.annotations.Test;

@Model(className = "Keywords", properties = {
    @Property(name = "private", type = String.class),
    @Property(name = "public", type = double.class),
    @Property(name = "final", type = String.class),
    @Property(name = "int", type = int.class),
    @Property(name = "class", type = String.class),
//    @Property(name = "{", type = String.class),
    @Property(name = "array", type = KeywordsInArray.class)
})
public class KeywordsTest {
    @Model(className = "KeywordsInArray", properties = {
        @Property(name = "private", type = String.class, array = true),
        @Property(name = "public", type = double.class, array = true),
        @Property(name = "final", type = String.class, array = true),
        @Property(name = "int", type = int.class, array = true),
        @Property(name = "class", type = String.class, array = true),
//    @Property(name = "{", type = String.class),
        @Property(name = "array", type = Keywords.class, array = true)
    })
    static class KeywordsInArrayCntrl {
    }
    
    @Test public void verifyKeywordsClassCompiles() {
        Keywords k = new Keywords();
        k.setClass("c");
        k.setFinal("f");
        k.setInt(10);
        k.setPrivate("p");
        k.setPublic(42.0);
        
        assertEquals(k.accessClass(), "c");
        assertEquals(k.getFinal(), "f");
        assertEquals(k.getInt(), 10);
        assertEquals(k.getPrivate(), "p");
        assertEquals(k.getPublic(), 42.0);
    }
    
    @Test public void verifyKeywordsInArrayClassCompiles() {
        KeywordsInArray k = new KeywordsInArray();
        k.accessClass().add("c");
        k.getFinal().add("f");
        k.getInt().add(10);
        k.getPrivate().add("p");
        k.getPublic().add(42.0);
        
        assertEquals(k.accessClass().get(0), "c");
        assertEquals(k.getFinal().get(0), "f");
        assertEquals(k.getInt().get(0), Integer.valueOf(10));
        assertEquals(k.getPrivate().get(0), "p");
        assertEquals(k.getPublic().get(0), 42.0);
    }
}
