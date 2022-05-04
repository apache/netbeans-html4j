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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.java.html.boot.BrowserBuilder;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class ScriptEngineTest {
    public ScriptEngineTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        List<Object> res = new ArrayList<>();
        final ScriptEngineManager manager = new ScriptEngineManager();
        for (ScriptEngineFactory f : manager.getEngineFactories()) {
            if (!isJavaScriptEngineFactory(f)) {
                continue;
            }
            collectTestsForEngine(f.getScriptEngine(), res);
        }
        return res.toArray();
    }

    static boolean isJavaScriptEngineFactory(ScriptEngineFactory f) {
        if (f.getNames().contains("nashorn")) {
            return true;
        }
        return f.getMimeTypes().contains("text/javascript");
    }

    private static void collectTestsForEngine(ScriptEngine engine, List<Object> res) throws Exception {
        Fn.Presenter browserPresenter[] = { null };
        CountDownLatch cdl = new CountDownLatch(1);
        Fn.Presenter presenter = createPresenter(engine);
        final BrowserBuilder bb = BrowserBuilder.newBrowser(presenter).
            loadPage("empty.html").
            loadFinished(() -> {
                browserPresenter[0] = Fn.activePresenter();
                cdl.countDown();
            });

        Executors.newSingleThreadExecutor().submit(bb::showAndWait);
        cdl.await();

        assertNoGlobalSymbolsLeft(engine);
        final String prefix = "[" + engine.getFactory().getEngineName() + "] ";

        ScriptEngineJavaScriptTCK.collectTckTests(res, (m) -> new ScriptEngineCase(prefix, browserPresenter[0], m));
    }

    private static void assertNoGlobalSymbolsLeft(ScriptEngine engine) throws ScriptException {
        Object left = engine.eval("""
            (function() {
              var names = Object.getOwnPropertyNames(this);
              for (var i = 0; i < names.length; i++) {
                var n = names[i];
                if (n === 'Object') continue;
                if (n === 'Number') continue;
                if (n === 'Boolean') continue;
                if (n === 'Array') continue;
                if (n === 'eval') continue;
                if (n === 'Promise') continue;
                delete this[n];
              }
              return Object.getOwnPropertyNames(this).toString();
            })()
            """
        );
        assertEquals(left.toString().toLowerCase().indexOf("java"), -1, "No Java symbols " + left);
    }

    private static Fn.Presenter createPresenter(ScriptEngine engine) {
        final Executor someExecutor = (r) -> r.run();
        // BEGIN: Jsr223JavaScriptTest#createPresenter
        return Scripts.newPresenter()
            .engine(engine)
            .executor(someExecutor)
            .build();
        // END: Jsr223JavaScriptTest#createPresenter
    }

}
