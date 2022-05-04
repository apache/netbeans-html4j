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
package org.netbeans.html.boot.impl;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/** Verify behavior of the callback parser.
 *
 * @author Jaroslav Tulach
 */
public class JsCallbackTest {
    
    public JsCallbackTest() {
    }
    @Test public void missingTypeSpecification() {
        String body = """
                      console[attr] = function(msg) {
                        @org.netbeans.html.charts.Main::log(msg);
                      };
                      """;
        JsCallback instance = new JsCallbackImpl();
        try {
            String result = instance.parse(body, false);
            fail("The parsing should fail!");
        } catch (IllegalStateException ex) {
            // OK
        }
    }


    public class JsCallbackImpl extends JsCallback {
        private String ident;
        private String fqn;
        private String method;
        private String params;
        
        @Override
        public CharSequence callMethod(String ident, boolean promise, String fqn, String method, String params) {
            this.ident = ident;
            this.fqn = fqn;
            this.method = method;
            this.params = params;
            return "call";
        }
    }
    
}
