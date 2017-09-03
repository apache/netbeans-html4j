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

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import java.lang.reflect.Array;

@MessageResolution(receiverType = JavaArray.class, language = TrufflePresenter.JavaLang.class)
final class JavaArray extends JavaValue implements TruffleObject {
    final TrufflePresenter.WrapArray wrap;
    final Object arr;

    public JavaArray(TrufflePresenter.WrapArray wrap, Object arr) {
        this.arr = arr;
        this.wrap = wrap;
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return JavaArrayForeign.ACCESS;
    }

    @Override
    public Object get() {
        return arr;
    }

    static boolean isInstance(TruffleObject obj) {
        return obj instanceof JavaArray;
    }

    static boolean isArray(Object obj) {
        return obj != null && obj.getClass().getComponentType() != null;
    }

    @Resolve(message = "READ")
    static abstract class ReadNode extends Node {
        protected Object access(JavaArray arr, int index) {
            Object obj = Array.get(arr.arr, index);
            return toJavaScript(obj, arr.wrap);
        }
    }

    @Resolve(message = "HAS_SIZE")
    static abstract class HasSizeNode extends Node {
        protected boolean access(JavaArray arr) {
            return true;
        }
    }

    @Resolve(message = "GET_SIZE")
    static abstract class GetSizeNode extends Node {
        protected int access(JavaArray arr) {
            return Array.getLength(arr.arr);
        }
    }

}
