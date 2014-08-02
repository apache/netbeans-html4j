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
package org.netbeans.html.json.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import net.java.html.json.Person;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.Transfer;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class OnReceiveTest {
    @Test public void performJSONCall() {
        MockTrans mt = new MockTrans();
        BrwsrCtx ctx = Contexts.newBuilder().register(Transfer.class, mt, 1).build();
        
        Employee e = Models.bind(new Employee(), ctx);
        e.setCall(null);
        Person p = new Person();
        
        mt.result = new HashMap<String, String>();
        mt.result.put("firstName", "Jarda");
        mt.result.put("lastName", "Tulach");
        e.changePersonalities(1, 2.0, "3", p);
        final Call c = e.getCall();
        assertNotNull(c, "A call has been made");
        assertEquals(c.getI(), 1);
        assertEquals(c.getD(), 2.0);
        assertEquals(c.getS(), "3");
        assertEquals(c.getP(), p);
        assertEquals(c.getData().size(), 1, "One result sent over wire");
        assertEquals(c.getData().get(0).getFirstName(), "Jarda");
        assertEquals(c.getData().get(0).getLastName(), "Tulach");
    }

    
    public static class MockTrans implements Transfer {
        Map<String,String> result;
        
        @Override
        public void extract(Object obj, String[] props, Object[] values) {
            assertTrue(obj instanceof Map, "It is a map: " + obj);
            Map<?,?> mt = (Map<?,?>) obj;
            for (int i = 0; i < props.length; i++) {
                values[i] = mt.get(props[i]);
            }
        }

        @Override
        public Object toJSON(InputStream is) throws IOException {
            throw new IOException();
        }

        @Override
        public void loadJSON(JSONCall call) {
            Object r = result;
            assertNotNull(r, "We need a reply!");
            result = null;
            call.notifySuccess(r);
        }
    }
}
