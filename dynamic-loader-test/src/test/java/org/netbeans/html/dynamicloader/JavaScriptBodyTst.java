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
package org.netbeans.html.dynamicloader;

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

/**
 *
 * @author Jaroslav Tulach
 */
@JavaScriptResource("empty.js")
public class JavaScriptBodyTst {

    public JavaScriptBodyTst() {
    }

    public void assert42() {
        int v = mul(7, 6);
        assert v == 42 : "Really 42: " + v;
    }

    public void assertEmptySymbolDefined() {
        assert Boolean.TRUE.equals(eval("empty")) : "empty.js should defined empty global symbol";
    }
    public void assertEmpty2SymbolDefined() {
        MultiResource.loadIt();
        assert Boolean.TRUE.equals(eval("empty2")) : "empty.js should defined empty global symbol";
    }
    public void assertEmpty3SymbolDefined() {
        MultiResource.loadIt();
        assert Boolean.TRUE.equals(eval("empty3")) : "empty.js should defined empty global symbol";
    }

    public void assertJavaScriptBodyAnnotationPresentInRuntime() throws Exception {
        var mul = JavaScriptBodyTst.class.getDeclaredMethod("mul", int.class, int.class);
        var ann = mul.getAnnotation(JavaScriptBody.class);
        assert ann == null : "DynamicClassLoader doesn't modify retention unlike -javaagent usage";
    }

    public void assertJavaScriptResourceAnnotationPresentInRuntime() throws Exception {
        var ann = JavaScriptBodyTst.class.getAnnotation(JavaScriptResource.class);
        assert ann == null : "DynamicClassLoader doesn't modify retention unlike -javaagent usage";
    }

    public void assertJavaScriptResourceGroupAnnotationPresentInRuntime() throws Exception {
        var ann = MultiResource.class.getAnnotation(JavaScriptResource.Group.class);
        assert ann == null : "DynamicClassLoader doesn't modify retention unlike -javaagent usage";
    }

    @JavaScriptBody(args = { "x", "y" }, body = "return x * y;")
    private static native int mul(int x, int y);

    @JavaScriptBody(args = { "code" }, body = "return eval(code);")
    private static native Object eval(String code);

    @JavaScriptResource("empty_2.js")
    @JavaScriptResource("empty_3.js")
    static final class MultiResource {
        @JavaScriptBody(args = {}, body = "")
        static void loadIt() {
        }
    }
}
