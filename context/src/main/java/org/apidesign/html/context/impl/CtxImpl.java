/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.context.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.java.html.BrwsrCtx;

/** Implementation detail. Holds list of technologies for particular
 * {@link BrwsrCtx}.
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public final class CtxImpl {
    private final List<Bind<?>> techs;
    
    public CtxImpl() {
        techs = new ArrayList<Bind<?>>();
    }
    
    private CtxImpl(List<Bind<?>> techs) {
        this.techs = techs;
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
        Collections.sort(techs);
        CtxImpl impl = new CtxImpl(Collections.unmodifiableList(techs));
        return CtxAccssr.getDefault().newContext(impl);
    }

    public <Tech> void register(Class<Tech> type, Tech impl, int priority) {
        techs.add(new Bind<Tech>(type, impl, priority));
    }
    
    private static final class Bind<Tech> implements Comparable<Bind<?>> {
        private final Class<Tech> clazz;
        private final Tech impl;
        private final int priority;

        public Bind(Class<Tech> clazz, Tech impl, int priority) {
            this.clazz = clazz;
            this.impl = impl;
            this.priority = priority;
        }

        @Override
        public int compareTo(Bind<?> o) {
            if (priority != o.priority) {
                return priority - o.priority;
            }
            return clazz.getName().compareTo(o.clazz.getName());
        }
    }
}
