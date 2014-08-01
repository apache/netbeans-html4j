/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.apidesign.html.json.spi;

import java.lang.ref.WeakReference;

/**
 *
 * @author Jaroslav Tulach
 */
final class Watcher {
    private static final Object LOCK = new Object();
    private static Watcher global;

    private final Proto proto;
    private final String prop;
    private Watcher next;

    private Watcher(Proto proto, String prop) {
        this.proto = proto;
        this.prop = prop;
    }
    
    static void beginComputing(Proto p, String name) {
        synchronized (LOCK) {
            Watcher alreadyThere = find(global, p, null);
            if (alreadyThere != null) {
                throw new IllegalStateException("Re-entrant attempt to access " + p);
            }
            final Watcher nw = new Watcher(p, name);
            nw.next = global;
            global = nw;
        }
    }
    
    static void verifyUnlocked(Proto p) {
        synchronized (LOCK) {
            Watcher alreadyThere = find(global, p, null);
            if (alreadyThere != null) {
                throw new IllegalStateException("Re-entrant attempt to access " + p);
            }
        }        
    }

    static Ref accessingValue(Proto p, Ref observers, String propName) {
        synchronized (LOCK) {
            Watcher alreadyThere = find(global, p, null);
            if (alreadyThere != null) {
                throw new IllegalStateException("Re-entrant attempt to access " + p);
            }
            Watcher w = global;
            for (;;) {
                if (w == null) {
                    return observers;
                }
                observers = w.observe(observers, propName);
                w = w.next;
            }
        }
    }
    
    static Watcher finishComputing(Proto p, Watcher mine) {
        synchronized (LOCK) {
            Watcher w = global;
            global = w.next;
            w.next = null;
            if (w.proto != p) {
                throw new IllegalStateException("Inconsistency: " + w.proto + " != " + p);
            }
            return register(mine, w);
        }
    }
    
    static Watcher find(Watcher first, Proto proto, String prop) {
    //    assert Thread.holdsLock(LOCK);
        for (;;) {
            if (first == null) {
                return null;
            }
            if (prop != null && prop.equals(first.prop)) {
                return first;
            }
            if (proto != null && proto == first.proto) {
                return first;
            }
            first = first.next;
        }
    }

    private static Watcher register(Watcher mine, Watcher locked) {
        assert Thread.holdsLock(LOCK);
        if (locked.prop == null) {
            return mine;
        }
        Watcher current = mine;
        for (;;) {
            if (current == null) {
                return locked;
            }
            Watcher next = current.next;
            if (next == null) {
                current.next = locked;
                return mine;
            }
            if (next.prop.equals(locked.prop)) {
                locked.next = next.next;
                current.next = locked;
                return mine;
            }
            current = next;
        }
    }
    
    static Watcher computing(Proto proto, String prop) {
        proto.getClass();
        prop.getClass();
        return new Watcher(proto, prop);
    }
    
    Ref observe(Ref prev, String prop) {
        if (prop == null) {
            return prev;
        }
        return new Ref(this, prop).chain(prev);
    }

    static final class Ref extends WeakReference<Watcher> {
        private final String prop;
        private Ref next;
        
        public Ref(Watcher ref, String prop) {
            super(ref);
            this.prop = prop;
        }
        
        Ref chain(Ref prev) {
            this.next = dropDead(prev, null);
            return this;
        }
        
        private Watcher watcher() {
            Watcher w = get();
            if (w != null && w.proto.watcher(w.prop) == w) {
                return w;
            }
            return null;
        }
        
        private static Ref dropDead(Ref self, String fireProp) {
            while (self != null && self.watcher() == null) {
                self = self.next;
            }
            if (self == null) {
                return null;
            }
            Ref current = self;
            for (;;) {
                Watcher w = current.watcher();
                if (w != null && fireProp != null && fireProp.equals(current.prop)) {
                    w.proto.valueHasMutated(w.prop);
                }
                for (;;) {
                    Ref next = current.next;
                    if (next == null) {
                        return self;
                    }
                    if (next.watcher() != null) {
                        current = next;
                        break;
                    } else {
                        current.next = next.next;
                    }
                }
            }
            
        }

        static Ref valueHasMutated(Ref self, String propName) {
            return dropDead(self, propName);
        }
    }
}
