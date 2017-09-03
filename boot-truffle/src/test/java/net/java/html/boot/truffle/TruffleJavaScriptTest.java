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
package net.java.html.boot.truffle;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import net.java.html.boot.BrowserBuilder;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.JavaScriptTCK;
import org.netbeans.html.json.tck.KOTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 *
 * @author Jaroslav Tulach
 */
public class TruffleJavaScriptTest {
    private static Class<?> browserClass;
    private static Fn.Presenter browserPresenter;
    
    public TruffleJavaScriptTest() {
    }

    @Factory public static Object[] compatibilityTests() throws Exception {
        PolyglotEngine engine = PolyglotEngine.newBuilder().build();
        PolyglotEngine.Value result = null;
        try {
            result = engine.eval(Source.fromText("6 * 7", "test.js").withMimeType("text/javascript"));
        } catch (Exception notSupported) {
            if (notSupported.getMessage().contains("text/javascript")) {
                return new Object[] { new Skip(true, notSupported.getMessage()) };
            }
        }
        assertEquals(42, result.as(Number.class).intValue(), "Executed OK");

        final BrowserBuilder bb = BrowserBuilder.newBrowser(TrufflePresenters.create(SingleCase.JS)).
            loadClass(TruffleJavaScriptTest.class).
            loadPage("empty.html").
            invoke("initialized");

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                bb.showAndWait();
            }
        });

        List<Object> res = new ArrayList<>();
        Class<? extends Annotation> test = 
            loadClass().getClassLoader().loadClass(KOTest.class.getName()).
            asSubclass(Annotation.class);

        Class[] arr = (Class[]) loadClass().getDeclaredMethod("tests").invoke(null);
        for (Class c : arr) {
            if (c.getSimpleName().contains("GC")) {
                continue;
            }
            for (Method m : c.getMethods()) {
                if (m.getAnnotation(test) != null) {
                    res.add(new SingleCase(browserPresenter, m));
                }
            }
        }
        return res.toArray();
    }

    static synchronized Class<?> loadClass() throws InterruptedException {
        while (browserClass == null) {
            TruffleJavaScriptTest.class.wait();
        }
        return browserClass;
    }
    
    public static synchronized void ready(Class<?> browserCls) throws Exception {
        browserClass = browserCls;
        browserPresenter = Fn.activePresenter();
        TruffleJavaScriptTest.class.notifyAll();
    }
    
    public static void initialized() throws Exception {
        Assert.assertSame(TruffleJavaScriptTest.class.getClassLoader(),
            ClassLoader.getSystemClassLoader(),
            "No special classloaders"
        );
        TruffleJavaScriptTest.ready(Tck.class);
    }

    public static final class Tck extends JavaScriptTCK {

        public static Class[] tests() {
            return testClasses();
        }
    }

    public static final class Skip {
        private final String message;
        private final boolean fail;

        public Skip(String message) {
            this(false, message);
        }

        Skip(boolean fail, String message) {
            this.message = message;
            this.fail = fail;
        }

        @Test
        public void needsGraalVMToExecuteTheTests() {
            if (fail) {
                throw new SkipException(message);
            }
        }
    }
}
