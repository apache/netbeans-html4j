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

import java.io.Reader;
import java.net.URL;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MapObjsTest {

    private Pres p1;
    private Pres p2;

    public MapObjsTest() {
    }

    @BeforeMethod
    public void setUpClass() throws Exception {
        MapObjs.reset();
        p1 = new Pres();
        p2 = new Pres();
    }

    @Test
    public void testValuesForP1P2() {
        Value v1 = new Value();
        Value v2 = new Value();

        v1.put(p1, "p1");
        v2.put(p1, "p1");
        v1.put(p2, "p2");

        assertEquals(v1.get(p1), "p1");
        assertEquals(v2.get(p1), "p1");
        assertEquals(v1.get(p2), "p2");
        assertEquals(v2.get(p2), null);
    }

    private static final class Value {
        private Object now;

        void put(Fn.Presenter p, Object v) {
            now = MapObjs.put(now, p, v);
        }

        Object get(Fn.Presenter p) {
            return MapObjs.get(now, p);
        }
    }

    private static final class Pres implements Fn.Presenter {
        @Override
        public Fn defineFn(String code, String... names) {
            return null;
        }

        @Override
        public void displayPage(URL page, Runnable onPageLoad) {
        }

        @Override
        public void loadScript(Reader code) throws Exception {
        }
    }
}
