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
package org.netbeans.html.boot.spi;

import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class FallbackIdentityTest {

    public FallbackIdentityTest() {
    }

    @Test
    public void testIdAndWeak() {
        Fn.Presenter p = new Fn.Presenter() {
            @Override
            public Fn defineFn(String arg0, String... arg1) {
                return null;
            }

            @Override
            public void displayPage(URL arg0, Runnable arg1) {
            }

            @Override
            public void loadScript(Reader arg0) throws Exception {
            }
        };

        Fn.Ref<?> id1 = Fn.ref(p);
        Fn.Ref<?> id2 = Fn.ref(p);

        assertNotSame(id1, id2);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());

        Reference<Fn.Presenter> ref = new WeakReference<>(p);
        p = null;
        NbTestCase.assertGC("Presenter is held weakly", ref);
    }

    @Test
    public void testPresenterCanProvideItsOwnIdentity() {
        class IdPresenter implements Fn.Presenter, Fn.Ref {
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

            @Override
            public Fn.Ref reference() {
                return this;
            }

            @Override
            public Fn.Presenter presenter() {
                return this;
            }
        }
        IdPresenter p = new IdPresenter();

        assertSame(p, Fn.ref(p));
    }

    @Test
    public void nullYieldsNullReference() {
        assertNull(Fn.ref(null));
    }
}
