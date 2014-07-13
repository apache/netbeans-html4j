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
package org.netbeans.html.json.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apidesign.html.json.spi.Proto;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JSONList<T> extends ArrayList<T> {
    private final Proto proto;
    private final String name;
    private final String[] deps;
    private final int index;

    public JSONList(Proto proto, String name, int changeIndex, String... deps) {
        this.proto = proto;
        this.name = name;
        this.deps = deps;
        this.index = changeIndex;
    }
    
    public void init(Object values) {
        int len;
        if (values == null || (len = Array.getLength(values)) == 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            Object data = Array.get(values, i);
            super.add((T)data);
        }
    }
    public static <T> void init(Collection<T> to, Object values) {
        int len;
        if (values == null || (len = Array.getLength(values)) == 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            Object data = Array.get(values, i);
            to.add((T)data);
        }
    }
    
    @Override
    public boolean add(T e) {
        boolean ret = super.add(e);
        notifyChange();
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean ret = super.addAll(c);
        notifyChange();
        return ret;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean ret = super.addAll(index, c);
        notifyChange();
        return ret;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = super.remove(o);
        notifyChange();
        return ret;
    }

    @Override
    public void clear() {
        super.clear();
        notifyChange();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = super.removeAll(c);
        notifyChange();
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = super.retainAll(c);
        notifyChange();
        return ret;
    }

    @Override
    public T set(int index, T element) {
        T ret = super.set(index, element);
        notifyChange();
        return ret;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        notifyChange();
    }

    @Override
    public T remove(int index) {
        T ret = super.remove(index);
        notifyChange();
        return ret;
    }

    @Override
    public String toString() {
        Iterator<T> it = iterator();
        if (!it.hasNext()) {
            return "[]";
        }
        String sep = "";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (it.hasNext()) {
            T t = it.next();
            sb.append(sep);
            sb.append(JSON.toJSON(t));
            sep = ",";
        }
        sb.append(']');
        return sb.toString();
    }

    private void notifyChange() {
        proto.getContext().execute(new Runnable() {
            @Override
            public void run() {
                Bindings m = PropertyBindingAccessor.getBindings(proto, false);
                if (m != null) {
                    m.valueHasMutated(name, null, null);
                    for (String dependant : deps) {
                        m.valueHasMutated(dependant, null, null);
                    }
                    if (index >= 0) {
                        PropertyBindingAccessor.notifyProtoChange(proto, index);
                    }
                }
            }
        });
    }

    @Override
    public JSONList clone() {
        throw new UnsupportedOperationException();
    }

    static final Object koData(Collection<?> c, Bindings m) {
        Object[] arr = c.toArray(new Object[c.size()]);
        for (int i = 0; i < arr.length; i++) {
            Object r = JSON.find(arr[i], m);
            if (r != null) {
                arr[i] = r;
            }
        }
        return m.wrapArray(arr);
    }

    final Object koData() {
        return koData(this, PropertyBindingAccessor.getBindings(proto, true));
    }
}
