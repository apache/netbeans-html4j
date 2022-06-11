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

import java.util.List;
import net.java.html.json.Models;
import org.netbeans.html.boot.spi.Fn;

final class MapObjs {
    private static Fn.Ref onlyPresenter;
    private static boolean usePresenter;

    static {
        reset();
    }

    synchronized static void reset() {
        onlyPresenter = null;
        usePresenter = true;
    }

    private final List<Object> all;

    private MapObjs(Fn.Ref id1, Object js) {
        this.all = Models.asList(id1, js);
    }

    private MapObjs(Fn.Ref id1, Object js1, Fn.Ref id2, Object js2) {
        this.all = Models.asList(id1, js1, id2, js2);
    }


    synchronized static Object put(Object now, Fn.Presenter key, Object js) {
        if (now instanceof MapObjs) {
            return ((MapObjs)now).put(key, js);
        } else {
            if (usePresenter) {
                if (getOnlyPresenter() == null) {
                    setOnlyPresenter(key);
                    return js;
                } else if (getOnlyPresenter() == key) {
                    return js;
                } else {
                    usePresenter = false;
                }
            }
            if (now == null) {
                return new MapObjs(Fn.ref(key), js);
            } else {
                return new MapObjs(onlyPresenter, now, Fn.ref(key), js);
            }
        }
    }

    synchronized static Object get(Object now, Fn.Presenter key) {
        if (now instanceof MapObjs) {
            return ((MapObjs)now).get(key);
        }
        return key == getOnlyPresenter() ? now : null;
    }

    synchronized static Object[] remove(Object now, Fn.Presenter key) {
        if (now instanceof MapObjs) {
            return ((MapObjs)now).remove(key);
        }
        return new Object[] { now, null };
    }

    synchronized static Object[] toArray(Object now) {
        if (now instanceof MapObjs) {
            return ((MapObjs) now).all.toArray();
        }
        final Fn.Presenter p = getOnlyPresenter();
        if (p == null) {
            return new Object[0];
        }
        return new Object[] { p, now };
    }

    private Object put(Fn.Presenter key, Object js) {
        for (int i = 0; i < all.size(); i += 2) {
            if (isSameKey(i, key)) {
                all.set(i + 1, js);
                return this;
            }
        }
        all.add(Fn.ref(key));
        all.add(js);
        return this;
    }

    boolean isSameKey(int index, Fn.Presenter key) {
        Object at = all.get(index);
        if (at instanceof Fn.Ref) {
            at = ((Fn.Ref)at).presenter();
        }
        return at == key;
    }

    private Object get(Fn.Presenter key) {
        for (int i = 0; i < all.size(); i += 2) {
            if (isSameKey(i, key)) {
                return all.get(i + 1);
            }
        }
        return null;
    }

    private Object[] remove(Fn.Presenter key) {
        for (int i = 0; i < all.size(); i += 2) {
            if (isSameKey(i, key)) {
                return new Object[] { all.get(i + 1), this };
            }
        }
        return new Object[] { null, this };
    }

    private static Fn.Presenter getOnlyPresenter() {
        final Fn.Presenter p = onlyPresenter == null ? null : onlyPresenter.presenter();
        return p;
    }

    private static void setOnlyPresenter(Fn.Presenter p) {
        onlyPresenter = Fn.ref(p);
    }
}
