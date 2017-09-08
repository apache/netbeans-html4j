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
package org.netbeans.html.json.impl;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
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
