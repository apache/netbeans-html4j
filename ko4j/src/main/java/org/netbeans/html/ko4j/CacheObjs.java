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

import org.netbeans.html.boot.spi.Fn;

final class CacheObjs {
    /* both @GuardedBy CacheObjs.class */
    private static final CacheObjs[] list = new CacheObjs[16];
    private static int listAt = 0;
    private final Fn.Ref<?> ref;

    /* both @GuardedBy presenter single threaded access */
    private Object[] jsObjects;
    private int jsIndex;

    private CacheObjs(Fn.Presenter p) {
        this.ref = Fn.ref(p);
    }

    Fn.Presenter get() {
        return ref.presenter();
    }

    static synchronized CacheObjs find(Fn.Presenter key) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] != null && list[i].get() == key) {
                return list[i];
            }
        }
        CacheObjs co = new CacheObjs(key);
        list[listAt] = co;
        listAt = (listAt + 1) % list.length;
        return co;
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
