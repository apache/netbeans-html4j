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

package org.netbeans.html.mojo;

import java.io.Closeable;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class Gradle1Test {
    @Test
    public void checkTheResultOfTheBuild() throws Exception {
        try {
            Class.forName("java.lang.Module");
            throw new SkipException("Don't test Gradle on new JDKs yet");
        } catch (ClassNotFoundException ex) {
            // OK, go on with the test
        }

        URL b = Gradle1Test.class.getResource("gradle1/build.gradle");
        assertNotNull(b, "gradle build script found");
        URL u = Gradle1Test.class.getResource("gradle1/build/libs/gradle1-1.0-SNAPSHOT.jar");
        assertNotNull(u, "Result of gradle1 build found");
        URLClassLoader l = new URLClassLoader(new URL[] { u }, Gradle1Test.class.getClassLoader());
        Class<?> clazz = l.loadClass("Gradle1Check");
        Callable<?> r = (Callable<?>) clazz.newInstance();

        final NumberPresenter mockPresenter = new NumberPresenter();
        try (Closeable c = Fn.activate(mockPresenter)) {
            Object value = r.call();
            assertTrue(value instanceof Number, "It is a number");
            assertEquals(((Number)value).intValue(), 42, "The meaning is returned");
        }
        assertEquals(mockPresenter.loadScriptCount, 1, "One script loaded");
    }

    private static final class NumberPresenter implements Fn.Presenter {
        private final Properties p = new Properties();

        private int loadScriptCount;

        @Override
        public Fn defineFn(String code, String... ignore) {
            if (code.startsWith("return")) {
                code = code.substring(6);
            }
            code = code.replace(';', ' ').trim();
            String number = p.getProperty(code);
            if (number == null) {
                return new NumberFn(42);
            }
            return new NumberFn(Integer.valueOf(number));
        }

        @Override
        public void displayPage(URL url, Runnable r) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void loadScript(Reader reader) throws Exception {
            p.load(reader);
            loadScriptCount++;
        }

    }

    private static class NumberFn extends Fn {

        private final int value;

        public NumberFn(int value) {
            this.value = value;
        }

        @Override
        public Object invoke(Object o, Object... os) throws Exception {
            return value;
        }
    }
}
