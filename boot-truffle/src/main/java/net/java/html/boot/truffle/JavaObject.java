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
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(receiverType = JavaObject.class, language = TrufflePresenter.JavaLang.class)
final class JavaObject extends JavaValue implements TruffleObject {
    final Object obj;

    JavaObject(Object obj) {
        this.obj = obj;
    }


    @Override
    public ForeignAccess getForeignAccess() {
        return JavaObjectForeign.ACCESS;
    }

    public static boolean isInstance(TruffleObject obj) {
        return obj instanceof JavaObject;
    }

    @Override
    public Object get() {
        return obj;
    }

    @Resolve(message = "HAS_SIZE")
    static abstract class NoSizeNode extends Node {

        protected boolean access(JavaObject obj) {
            return false;
        }
    }

    @Resolve(message = "INVOKE")
    static abstract class Methods extends Node {

        protected Object access(JavaObject javaObject, String methodName, Object[] args) {
            if (methodName.equals("toString")) {
                return javaObject.obj.toString();
            }
            throw UnknownIdentifierException.raise(methodName);
        }
    }

}
