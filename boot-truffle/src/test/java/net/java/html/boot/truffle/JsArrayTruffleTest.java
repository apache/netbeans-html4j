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
import java.io.Closeable;
import java.io.IOException;
import net.java.html.lib.Array;
import net.java.html.lib.Function;
import org.netbeans.html.boot.spi.Fn;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JsArrayTruffleTest {
    private Fn.Presenter presenter;
    private Closeable close;
    
    @BeforeMethod
    public void initializePresenter() throws Exception {
        PolyglotEngine engine = PolyglotEngine.newBuilder().build();
        int fourtyTwo;
        try {
            fourtyTwo = engine.eval(
                Source.newBuilder("6 * 7").
                mimeType("text/javascript").
                name("meaning.js").
                build()
            ).as(Number.class).intValue();
            presenter = TrufflePresenters.create(null);
            close = Fn.activate(presenter);
        } catch (Throwable ex) {
            fourtyTwo = 42;
        }
        assertEquals(fourtyTwo, 42, "Meaning of Graal");

    }

    @AfterMethod
    public void closePresenter() throws IOException {
        if (close != null) {
            close.close();
        }
    }


    @Test
    public void forEachArray() {
        if (presenter == null) {
            throw new SkipException("No presenter found, not running on GraalVM");
        }
        Array<String> array = new Array<>();
        array.push("Hello");
        array.push("World");
        array.push("how");
        array.push("are", "You?");

        Assert.assertEquals(array.length(), 5, "Five words");

        final Array<String> lowerCaseArray = new Array<>();
        array.forEach(new Function.A1<String, Void>() {
            @Override
            public Void call(String p1) {
                lowerCaseArray.push(p1.toLowerCase());
                return null;
            }

            @Override
            public Void call(String p1, Object p2) {
                return call(p1);
            }

            @Override
            public Void call(String p1, Object p2, Object p3) {
                return call(p1);
            }

            @Override
            public Void call(String p1, Object p2, Object p3, Object p4) {
                return call(p1);
            }

            @Override
            public Void call(String p1, Object p2, Object p3, Object p4, Object p5) {
                return call(p1);
            }
        });

        assertEquals(lowerCaseArray.$get(0), "hello");
        assertEquals(lowerCaseArray.$get(1), "world");
        assertEquals(lowerCaseArray.$get(2), "how");
        assertEquals(lowerCaseArray.$get(3), "are");
        assertEquals(lowerCaseArray.$get(4), "you?");
    }

}
