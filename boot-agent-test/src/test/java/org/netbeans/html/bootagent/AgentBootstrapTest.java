/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.html.bootagent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import net.java.html.boot.script.Scripts;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class AgentBootstrapTest {
    public AgentBootstrapTest() {
    }

    @Factory
    public static Object[] compatibilityTests() throws Exception {
        var presenter = Scripts.newPresenter().build();
        assertNotNull("Presenter has been initialized", presenter);

        var res = new ArrayList<Object>();

        Class[] arr = new Class[] { JavaScriptBodyTst.class };
        for (Class c : arr) {
            for (Method m : c.getDeclaredMethods()) {
                if ((m.getModifiers() & Modifier.PUBLIC) != 0) {
                    res.add(new KOFx(presenter, m));
                }
            }
        }
        return res.toArray();
    }
}
