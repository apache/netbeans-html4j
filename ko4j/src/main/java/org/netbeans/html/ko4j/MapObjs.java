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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.html.boot.spi.Fn;

final class MapObjs {
    static Object put(Object now, Fn.Presenter key, Object js) {
        Map<Fn.Presenter, Object> map = (Map<Fn.Presenter, Object>) now;
        if (map == null) {
            map = new HashMap<Fn.Presenter, Object>();
        }
        map.put(key, js);
        return map;
    }

    static Object get(Object now, Fn.Presenter key) {
        if (now instanceof Map) {
            Map<?,?> map = (Map<?,?>) now;
            return map.get(key);
        }
        return null;
    }

    static Object[] remove(Object now, Fn.Presenter key) {
        Map<?,?> map = (Map<?,?>) now;
        Object prev = map.remove(key);
        return new Object[] { prev, map };
    }

    static Object[] toArray(Object now) {
        if (now instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) now;
            Object[] res = new Object[map.size() * 2];
            int at = 0;
            for (Map.Entry<? extends Object, ? extends Object> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                res[at] = key;
                res[at + 1] = value;
                at += 2;
            }
            return res;
        }
        return new Object[0];
    }
}
