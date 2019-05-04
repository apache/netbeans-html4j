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
package org.netbeans.html.boot.fx;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import javafx.scene.web.WebView;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.junit.NbTestCase;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import org.testng.annotations.Test;

public class AbstractFXPresenterTest {
    @Test
    public void id() {
        AbstractFXPresenter p1 = new AbstractFXPresenter() {
            @Override
            void waitFinished() {
            }

            @Override
            WebView findView(URL resource) {
                return null;
            }
        };
        AbstractFXPresenter p2 = new AbstractFXPresenter() {
            @Override
            void waitFinished() {
            }

            @Override
            WebView findView(URL resource) {
                return null;
            }
        };

        Fn.Ref<?> id1 = Fn.ref(p1);
        Fn.Ref<?> id12 = Fn.ref(p1);

        assertSame(id1, id12);
        assertEquals(id1, id12);
        assertSame(p1, id1.presenter());

        Fn.Ref<?> id2 = Fn.ref(p2);
        Fn.Ref<?> id22 = Fn.ref(p2);
        assertSame(p2, id2.presenter());

        assertSame(id2, id22);
        assertEquals(id2, id22);

        Assert.assertNotEquals(id1, id2);

        Reference<AbstractFXPresenter> ref1 = new WeakReference<>(p1);
        p1 = null;
        NbTestCase.assertGC("Presenter can disappear", ref1);

        AbstractFXPresenter p2Clone = p2.clone();
        Assert.assertNotEquals(p2.reference(), p2Clone.reference());
    }
}
