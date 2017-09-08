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
package org.netbeans.html.context.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts;

/** Implementation detail. Holds list of technologies for particular
 * {@link BrwsrCtx}.
 *
 * @author Jaroslav Tulach
 */
public final class CtxImpl {
    private final List<Bind<?>> techs;
    private final Object[] context;
    
    public CtxImpl(Object[] context) {
        this(context, new ArrayList<Bind<?>>());
    }
    
    private CtxImpl(Object[] context, List<Bind<?>> techs) {
        this.techs = techs;
        this.context = context;
    }
    
    public static <Tech> Tech find(BrwsrCtx context, Class<Tech> technology) {
        CtxImpl impl = CtxAccssr.getDefault().find(context);
        for (Bind<?> bind : impl.techs) {
            if (technology == bind.clazz) {
                return technology.cast(bind.impl);
            }
        }
        return null;
    }

    public BrwsrCtx build() {
        Collections.sort(techs, new BindCompare());
        final List<Bind<?>> arr = Collections.unmodifiableList(techs);
        CtxImpl impl = new CtxImpl(context, arr);
        BrwsrCtx ctx = CtxAccssr.getDefault().newContext(impl);
        return ctx;
    }

    public <Tech> void register(Class<Tech> type, Tech impl, int priority) {
        techs.add(new Bind<Tech>(type, impl, priority));
    }
    
    private static final class Bind<Tech> {
        private final Class<Tech> clazz;
        private final Tech impl;
        private final int priority;

        public Bind(Class<Tech> clazz, Tech impl, int priority) {
            this.clazz = clazz;
            this.impl = impl;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return "Bind{" + "clazz=" + clazz + "@" + clazz.getClassLoader() + ", impl=" + impl + ", priority=" + priority + '}';
        }
    }
    
    private final class BindCompare implements Comparator<Bind<?>> {
        boolean isPrefered(Bind<?> b) {
            final Class<?> implClazz = b.impl.getClass();
            Contexts.Id id = implClazz.getAnnotation(Contexts.Id.class);
            if (id == null) {
                return false;
            }
            for (String v : id.value()) {
                for (Object c : context) {
                    if (v.equals(c)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        @Override
        public int compare(Bind<?> o1, Bind<?> o2) {
            boolean p1 = isPrefered(o1);
            boolean p2 = isPrefered(o2);
            if (p1 != p2) {
                return p1 ? -1 : 1;
            }
            if (o1.priority != o2.priority) {
                return o1.priority - o2.priority;
            }
            return o1.clazz.getName().compareTo(o2.clazz.getName());
        }
    } // end of BindCompare
}
