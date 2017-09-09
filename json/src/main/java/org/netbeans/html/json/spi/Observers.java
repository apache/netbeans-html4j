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
package org.netbeans.html.json.spi;

import java.util.Iterator;
import java.util.List;
import org.netbeans.html.json.impl.SimpleList;

/**
 *
 * @author Jaroslav Tulach
 */
final class Observers {
    private static final List<Watcher> GLOBAL = SimpleList.asList();
    private final List<Watcher> watchers = SimpleList.asList();
    private final List<Ref> observers = SimpleList.asList();

    Observers() {
        assert Thread.holdsLock(GLOBAL);
    }
    
    static Usages beginComputing(Proto p, String name, Usages usages) {
        synchronized (GLOBAL) {
            verifyUnlocked(p);
            final Watcher nw = new Watcher(p, name);
            GLOBAL.add(nw);
            return Usages.register(name, nw, usages);
        }
    }
    
    static void verifyUnlocked(Proto p) {
        synchronized (GLOBAL) {
            for (Watcher w : GLOBAL) {
                if (w.proto == p) {
                    if (w.owner == Thread.currentThread()) {
                        throw new IllegalStateException("Re-entrant attempt to access " + p);
                    }
                }
            }
        }        
    }

    static void accessingValue(Proto p, String propName) {
        synchronized (GLOBAL) {
            verifyUnlocked(p);
            for (Watcher w : GLOBAL) {
                Observers mine = p.observers(true);
                mine.add(w, new Ref(w, propName));
            }
        }
    }
    
    static void finishComputing(Proto p) {
        synchronized (GLOBAL) {
            boolean found = false;
            Iterator<Watcher> it = GLOBAL.iterator();
            while (it.hasNext()) {
                Watcher w = it.next();
                if (w.proto == p && w.owner == Thread.currentThread()) {
                    if (w.prop != null) {
                        Observers mine = p.observers(true);
                        mine.add(w);
                    }
                    found = true;
                    it.remove();
                }
            }
            if (!found) {
                throw new IllegalStateException("Cannot find " + p + " in " + GLOBAL);
            }
        }
    }
    
    private static final class Ref {
        private final Watcher ref;
        private final String prop;
        
        public Ref(Watcher ref, String prop) {
            this.ref = ref;
            this.prop = prop;
        }
        
        final Watcher watcher() {
            Watcher w = get();
            if (w == null) {
                return null;
            }
            final Observers o = w.proto.observers(false);
            if (o == null) {
                return null;
            }
            if (o.find(w.prop) == w) {
                return w;
            }
            return null;
        }

        Watcher get() {
            return ref.proto == null ? null : ref;
        }
    }

    private Watcher find(String prop) {
        if (prop == null) {
            return null;
        }
        for (Watcher w : watchers) {
            if (prop.equals(w.prop)) {
                return w;
            }
        }
        return null;
    }

        final void add(Watcher w) {
        for (int i = 0; i < watchers.size(); i++) {
            Watcher ith = watchers.get(i);
            if (w.prop == null) {
                if (ith.prop == null) {
                    watchers.set(i, w);
                    return;
                }
            } else if (w.prop.equals(ith.prop)) {
                watchers.set(i, w);
                return;
            }
        }
        watchers.add(w);
    }

    static final void valueHasMutated(Proto p, String propName) {
        List<Watcher> mutated = SimpleList.asList();
        synchronized (GLOBAL) {
            Observers mine = p.observers(false);
            if (mine == null) {
                return;
            }
            Iterator<Ref> it = mine.observers.iterator();
            while (it.hasNext()) {
                Ref ref = it.next();
                if (ref.get() == null) {
                    it.remove();
                    continue;
                }
                if (ref.prop.equals(propName)) {
                    Watcher w = ref.watcher();
                    if (w != null) {
                        mutated.add(w);
                    }
                }
            }
        }
        for (Watcher w : mutated) {
            w.proto.valueHasMutated(w.prop);
        }
    }

    void add(Watcher w, Ref r) {
        Thread.holdsLock(GLOBAL);
        if (w == null) {
            return;
        }
        Iterator<Ref> it = observers.iterator();
        while (it.hasNext()) {
            Ref ref = it.next();
            if (r == ref) {
                return;
            }
            final Watcher rw = ref.get();
            if (rw == null) {
                it.remove();
                continue;
            }
            if (rw == w && ref.prop.equals(r.prop)) {
                return;
            }
        }
        observers.add(r);
    }
    
    private static final class Watcher {
        Proto proto;
        final Thread owner;
        final String prop;

        Watcher(Proto proto, String prop) {
            this.owner = Thread.currentThread();
            this.proto = proto;
            this.prop = prop;
        }
        
        @Override
        public String toString() {
            return "Watcher: " + proto + ", " + prop;
        }

        void destroy() {
            proto = null;
        }
    }

    static final class Usages {
        private final List<String> names = SimpleList.asList();
        private final List<Watcher> watchers = SimpleList.asList();

        private Usages() {
        }

        static Usages register(String propName, Watcher w, Usages usages) {
            if (propName != null) {
                if (usages == null) {
                    usages = new Usages();
                }
                int index = usages.names.indexOf(propName);
                if (index == -1) {
                    usages.names.add(propName);
                    usages.watchers.add(w);
                } else {
                    Watcher prev = usages.watchers.get(index);
                    prev.destroy();
                    usages.watchers.set(index, w);
                }
            }
            return usages;
        }

    }
}
