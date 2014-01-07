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
package org.netbeans.html.json.impl;

import net.java.html.json.Model;
import net.java.html.json.Property;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className="Man", properties={
    @Property(name = "name", type = String.class),
    @Property(name = "other", type = Address.class, array = true),
    @Property(name = "primary", type = Address.class),
    @Property(name = "childrenNames", type = String.class, array = true)
})
public class ConstructorTest {
    @Model(className = "Address", properties = {
        @Property(name = "place", type = String.class)
    })
    static final class AddressModel {
    }
    
    @Test public void initializedByDefault() {
        Man m = new Man();
        assertNotNull(m.getPrimary(), "Single subobjects are initialized");
    }
    
    @Test public void hasRichConstructor() {
        Man m = new Man("Jarda", new Address("home"), new Address("work"), new Address("hotel"));
        assertEquals(m.getName(), "Jarda");
        assertNotNull(m.getPrimary(), "Primary address specified");
        assertNotNull(m.getPrimary().getPlace(), "home");
        assertEquals(m.getOther().size(), 2, "Two other addresses");
    }
}
