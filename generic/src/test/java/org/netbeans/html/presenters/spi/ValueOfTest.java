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
package org.netbeans.html.presenters.spi;

import org.netbeans.html.presenters.spi.Level;
import org.netbeans.html.presenters.spi.Generic;
import org.netbeans.html.presenters.spi.ProtoPresenterBuilder;
import java.net.URL;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueOfTest {
    private Generic p;
    @BeforeMethod public void initInstance() {
        p = new Generic(true, true, "type", "app") {
            @Override
            void log(Level level, String msg, Object... args) {
            }

            @Override
            void callbackFn(ProtoPresenterBuilder.OnPrepared onReady) {
            }

            @Override
            void loadJS(String js) {
            }

            @Override
            void dispatch(Runnable r) {
            }

            @Override
            public void displayPage(URL url, Runnable r) {
            }
        };
    }
    
    
    @Test public void parseSimpleArray() {
        Object res = p.valueOf("array:1:8:number:6");
        assertTrue(res instanceof Object[], "It is an array: " + res);
        Object[] arr = (Object[]) res;
        assertEquals(arr.length, 1, "One array item");
        assertEquals(arr[0], 6.0, "Value is six");
    }
}
