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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import org.netbeans.html.boot.spi.Fn;
import org.testng.Assert;
import org.testng.annotations.Test;

@JavaScriptResource("throw.js")
public final class ScriptEngineEvalTest {
    public ScriptEngineEvalTest() {
    }

    @Test
    public void fileNameIsDefinedWhenEvaluatingAResourceFile() throws Exception {
        try (var p = Fn.activate(Scripts.newPresenter().build())) {
            yieldError("Everything is OK!");
        } catch (Exception ex) {
            assertStackContains(ex,
                "Caused by.*Exception: Everything is OK!",
                "at <js>.yieldError.*throw.js:[0-9]+",
                "at <js>.*anonymous.*eval.*[0-9]+"
            );
        }
    }

    @JavaScriptBody(args = { "msg" }, body = """
    yieldError(msg);
    """)
    private static void yieldError(String msg) {
        throw new AssertionError(msg);
    }

    private void assertStackContains(Exception ex, String... patterns) {
        var w = new StringWriter();
        ex.printStackTrace(new PrintWriter(w));
        var lines = w.toString().split("\n");

        FOUND: for (var p : patterns) {
            var c = Pattern.compile(p);
            for (var l : lines) {
                if (c.matcher(l).find()) {
                    continue FOUND;
                }
            }
            Assert.fail("Cannot find " + p + " in:\n" + w);
        }
    }

}
