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
package net.java.html.boot.script;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ScriptsTest {
    @DataProvider(name = "engines")
    public static Object[][] primeNumbers() {
        List<Object[]> res = new ArrayList<>();
        final ScriptEngineManager manager = new ScriptEngineManager();
        for (ScriptEngineFactory f : manager.getEngineFactories()) {
            if (!Jsr223JavaScriptTest.isJavaScriptEngineFactory(f)) {
                continue;
            }
            res.add(new Object[] { f.getScriptEngine() });
        }
        return res.toArray(new Object[0][]);
    }

    public ScriptsTest() {
    }

    @Test
    public void testNewPresenterNoExecutor() throws Exception {
        // BEGIN: ScriptsTest#testNewPresenterNoExecutor
        Fn.Presenter presenter = Scripts.newPresenter().build();
        // END: ScriptsTest#testNewPresenterNoExecutor
        assertNotNull(presenter);
        Fn fn = presenter.defineFn("return a * b", "a", "b");
        Object fourtyTwo = fn.invoke(null, 6, 7);
        assertTrue(fourtyTwo instanceof Number);
        assertEquals(((Number)fourtyTwo).intValue(), 42);
    }

    @Test
    public void testActivatePresenterDirectly() throws Exception {
        int fortyTwo = activatePresenterDirectly();
        assertEquals(fortyTwo, 42);
    }

    // BEGIN: ScriptsTest#activatePresenterDirectly
    @JavaScriptBody(args = { "a", "b" }, body = "return a * b;")
    private static native int mul(int a, int b);

    private static int activatePresenterDirectly() throws Exception {
        Fn.Presenter p = Scripts.newPresenter().build();
        try (Closeable c = Fn.activate(p)) {
            int fortyTwo = mul(2, mul(7, 3));
            assert fortyTwo == 42;
            return fortyTwo;
        }
    }
    // END: ScriptsTest#activatePresenterDirectly

    @Test
    public void initViaBrowserBuilder() throws Exception {
        String[] executed = { null };
        // BEGIN: ScriptsTest#initViaBrowserBuilder
        Runnable run = () -> {
            executed[0] = "OK";
        };
        Fn.Presenter p = Scripts.newPresenter().build();
        BrowserBuilder.newBrowser(p)
            .loadFinished(run)
            .loadPage("empty.html")
            .showAndWait();
        // END: ScriptsTest#initViaBrowserBuilder
        assertEquals(executed[0], "OK", "Executed without issues");
    }

    @Test
    public void isSanitizationOnByDefault() throws Exception {
        assertSanitized(Scripts.newPresenter());
    }

    @Test
    public void isSanitizationOnExplicitly() throws Exception {
        assertSanitized(Scripts.newPresenter().sanitize(true));
    }

    @Test(dataProvider = "engines")
    public void noSanitization(ScriptEngine eng) throws Exception {
        boolean relaxed = "Graal.js".equals(eng.getFactory().getEngineName());

        assertNotSanitized(
            Scripts.newPresenter().engine(eng).sanitize(false),
            relaxed
        );
    }

    private void assertSanitized(Scripts newPresenter) throws Exception {
        Fn.Presenter p = newPresenter.build();
        awaitPresenter(p);
        try (Closeable c = Fn.activate(p)) {
            Object Java = p.defineFn("return typeof Java;").invoke(null);
            Object engine = p.defineFn("return typeof engine;").invoke(null);
            Object Packages = p.defineFn("return typeof Packages;").invoke(null);
            Object alert = p.defineFn("return typeof alert;").invoke(null);
            assertEquals(Java, "undefined", "No Java symbol");
            assertEquals(engine, "undefined", "No engine symbol");
            assertEquals(Packages, "undefined", "No Packages symbol");
            assertEquals(alert, "function", "alert is defined symbol");
        }
    }

    private void assertNotSanitized(Scripts builder, boolean relaxed) throws Exception {
        Fn.Presenter p = builder.build();
        try (Closeable c = Fn.activate(p)) {
            Object Java = p.defineFn("return typeof Java;").invoke(null);
            Object Packages = p.defineFn("return typeof Packages;").invoke(null);
            Object alert = p.defineFn("return typeof alert;").invoke(null);
            assertObjectRelaxed(relaxed, Java, "Java symbol found");
            assertObjectRelaxed(relaxed, Packages, "Packages symbol found");
            assertEquals(alert, "function", "alert is defined symbol");
        }
    }

    private static void assertObjectRelaxed(boolean relaxed, Object real, String msg) {
        if ("object".equals(real)) {
            return;
        }
        if (relaxed && "undefined".equals(real)) {
            return;
        }
        assertNull(real, msg);
    }

    private static void awaitPresenter(Fn.Presenter p) {
        Executor e = (Executor) p;
        CountDownLatch cdl = new CountDownLatch(1);
        e.execute(cdl::countDown);
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
