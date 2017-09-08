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
package org.netbeans.html.ko4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.netbeans.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "LessCalls", targetId = "", properties = {
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
        sw = null;
        LessCalls m = new LessCalls(10).applyBindings();
        assert m.getPlusOne() == 11 : "Expecting 11: " + m.getPlusOne();
        
        assert sw != null : "StringWriter should be initialized: " + sw;
        
        if (sw.toString().contains("$JsCallbacks$")) {
            assert false : "Don't call for initial value via JsCallbacks:\n" + sw;
        }
    }

    @KOTest public void dontCallForChangeValueBackToJavaVM() {
        LessCalls m = new LessCalls(10).applyBindings();
        assert m.getPlusOne() == 11 : "Expecting 11: " + m.getPlusOne();
        
        sw = null;
        m.setValue(5);
        assert m.getPlusOne() == 6: "Expecting 6: " + m.getPlusOne();
        assert sw != null : "StringWriter should be initialized: " + sw;
        
        if (sw.toString().contains("$JsCallbacks$")) {
            assert false : "Don't call for initial value via JsCallbacks:\n" + sw;
        }
    }
}
