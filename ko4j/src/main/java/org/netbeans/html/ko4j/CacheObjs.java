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

package org.netbeans.html.ko4j;

final class CacheObjs {
    private static final CacheObjs C = new CacheObjs();

    private Object[] jsObjects;
    private int jsIndex;

    static CacheObjs find() {
        return C;
    }

    Object getJSObject() {
        int len = 64;
        if (jsObjects != null && jsIndex < (len = jsObjects.length)) {
            Object ret = jsObjects[jsIndex];
            jsObjects[jsIndex] = null;
            jsIndex++;
            return ret;
        }
        jsObjects = Knockout.allocJS(len * 2);
        jsIndex = 1;
        Object ret = jsObjects[0];
        jsObjects[0] = null;
        return ret;
    }
}
