/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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

import java.util.Collection;
import java.util.List;
import net.java.html.BrwsrCtx;
import org.netbeans.html.json.impl.Bindings;
import org.netbeans.html.json.impl.JSON;
import org.netbeans.html.json.impl.JSONList;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 0.7
 */
public final class Proto {
    private final Object obj;
    private final Type type;
    private final net.java.html.BrwsrCtx context;
    private boolean locked;
    private org.netbeans.html.json.impl.Bindings ko;

    Proto(Object obj, Type type, BrwsrCtx context) {
        this.obj = obj;
        this.type = type;
        this.context = context;
    }

    public BrwsrCtx getContext() {
        return context;
    }
    
    public void acquireLock() throws IllegalStateException {
        if (locked) throw new IllegalStateException();
        locked = true;
    }
    
    public void checkLock() throws IllegalStateException {
        if (locked) throw new IllegalStateException();
    }
    
    public void releaseLock() {
        locked = false;
    }
    
    public void valueHasMutated(String propName) {
        if (ko != null) {
            ko.valueHasMutated(propName);
        }
    }
    
    public void applyBindings() {
        initBindings().applyBindings();
    }
    
    public void runInBrowser(Runnable run) {
        JSON.runInBrowser(context, run);
    }

    public void initTo(Collection<?> to, Object array) {
        if (ko != null) {
            throw new IllegalStateException();
        }
        if (to instanceof JSONList) {
           ((JSONList)to).init(array);
        } else {
            JSONList.init(to, array);
        }
    }
    
    
    // XXX: Don't expose internal type
    public Bindings initBindings() {
        if (ko == null) {
            Bindings b = Bindings.apply(context, obj);
            PropertyBinding[] pb = new PropertyBinding[type.propertyNames.length];
            for (int i = 0; i < pb.length; i++) {
                pb[i] = b.registerProperty(
                    type.propertyNames[i], i, obj, type, type.propertyReadOnly[i]
                );
            }
            FunctionBinding[] fb = new FunctionBinding[type.functions.length];
            for (int i = 0; i < fb.length; i++) {
                fb[i] = FunctionBinding.registerFunction(
                    type.functions[i], i, obj, type
                );
            }
            ko = b;
            b.finish(obj, pb, fb);
        }
        return ko;
    }

    // XXX: Don't expose internal type
    public Bindings getBindings() {
        return ko;
    }
    
    // XXX: Can be hidden too
    public void onChange(int index) {
        type.onChange(obj, index);
    }

    public String toString(Object data, String propName) {
        return JSON.toString(context, data, propName);
    }
    public Number toNumber(Object data, String propName) {
        return JSON.toNumber(context, data, propName);
    }
    
    public <T> T toModel(Class<T> type, Object data, String propName) {
        return JSON.toModel(context, type, data, propName);
    }

    public <T> List<T> createList(String propName, int onChange, String... dependingProps) {
        return new JSONList<T>(this, propName, onChange, dependingProps);
    }
    
    public <T> void cloneList(Collection<T> to, BrwsrCtx ctx, Collection<T> from) {
        Boolean isModel = null;
        for (T t : from) {
            if (isModel == null) {
                isModel = JSON.isModel(t.getClass());
            }
            if (isModel) {
                to.add(JSON.bindTo(t, ctx));
            } else {
                to.add(t);
            }
        }
    }
    
    /** Functionality used by the code generated by annotation
     * processor for the {@link net.java.html.json.Model} annotation.
     * 
     * @param <Model> the generated class
     * @since 0.7
     */
    public static abstract class Type<Model> {
        private final Class<Model> clazz;
        private final String[] propertyNames;
        private final boolean[] propertyReadOnly;
        private final String[] functions;

        protected Type(
            Class<Model> clazz, Class<?> modelFor, int properties, int functions
        ) {
            assert getClass().getName().endsWith("$Html4JavaType");
            assert getClass().getDeclaringClass() == clazz;
            this.clazz = clazz;
            this.propertyNames = new String[properties];
            this.propertyReadOnly = new boolean[properties];
            this.functions = new String[functions];
            JSON.register(clazz, this);
        }
        
        protected final void registerProperty(String name, int index, boolean readOnly) {
            assert propertyNames[index] == null;
            propertyNames[index] = name;
            propertyReadOnly[index] = readOnly;
        }
        
        protected final void registerFunction(String name, int index) {
            assert functions[index] == null;
            functions[index] = name;
        }
        
        public Proto protoFor(Object obj, BrwsrCtx context) {
            return new Proto(obj, this, context);
        }
        
        // XXX: should be protected
        public abstract void setValue(Model model, int index, Object value);
        public abstract Object getValue(Model model, int index);
        public abstract void call(Model model, int index, Object data, Object event);
        public abstract Model cloneTo(Object model, BrwsrCtx ctx);
        public abstract Model read(BrwsrCtx c, Object json);
        public abstract void onChange(Model model, int index);
        public abstract Proto protoFor(Object object);
        
        //
        // Various support methods the generated classes use
        //
        
        /** Compares two objects that can be converted to integers.
         * @return true if they are the same
         */
        public final boolean isSame(int a, int b) {
            return a == b;
        }

        /** Compares two objects that can be converted to (floating point)
         * numbers.
         * @return  true if they are the same
         */
        public final boolean isSame(double a, double b) {
            return a == b;
        }

        /** Compares two objects for being the same - e.g. either <code>==</code>
         * or <code>equals</code>.
         * @return true if they are equals
         */ 
        public final boolean isSame(Object a, Object b) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            return a.equals(b);
        }

        /** Cumulative hash function. Adds hashcode of the object to the
         * previous value.
         * @param o the object (or <code>null</code>)
         * @param h the previous value of the hash
         * @return new hash - the old one xor the object's one
         */
        public final int hashPlus(Object o, int h) {
            return o == null ? h : h ^ o.hashCode();
        }
        
        /** Converts an object to its JSON value.
         * 
         * @param obj the object to convert
         * @return JSON representation of the object
         */
        public final String toJSON(Object obj) {
            return JSON.toJSON(obj);
        }
        
        /** Converts the value to string.
         * 
         * @param val the value
         * @return the converted value
         */
        public final String stringValue(Object val) {
            return JSON.stringValue(val);
        }

        /** Converts the value to number.
         * 
         * @param val the value
         * @return the converted value
         */
        public final Number numberValue(Object val) {
            return JSON.numberValue(val);
        }

        /** Converts the value to character.
         * 
         * @param val the value
         * @return the converted value
         */
        public final Character charValue(Object val) {
            return JSON.charValue(val);
        }

        /** Converts the value to boolean.
         * 
         * @param val the value
         * @return the converted value
         */
        public final Boolean boolValue(Object val) {
            return JSON.boolValue(val);
        }
        
        /** Extracts value of specific type from given object.
         * 
         * @param <T> the type of object one is interested in
         * @param type the type
         * @param val the object to convert to type
         * @return the converted value
         */
        public final <T> T extractValue(Class<T> type, Object val) {
            if (Number.class.isAssignableFrom(type)) {
                val = numberValue(val);
            }
            if (Boolean.class == type) {
                val = boolValue(val);
            }
            if (String.class == type) {
                val = stringValue(val);
            }
            if (Character.class == type) {
                val = charValue(val);
            }
            if (Integer.class == type) {
                val = val instanceof Number ? ((Number) val).intValue() : 0;
            }
            if (Long.class == type) {
                val = val instanceof Number ? ((Number) val).longValue() : 0;
            }
            if (Short.class == type) {
                val = val instanceof Number ? ((Number) val).shortValue() : 0;
            }
            if (Byte.class == type) {
                val = val instanceof Number ? ((Number) val).byteValue() : 0;
            }
            if (Double.class == type) {
                val = val instanceof Number ? ((Number) val).doubleValue() : Double.NaN;
            }
            if (Float.class == type) {
                val = val instanceof Number ? ((Number) val).floatValue() : Float.NaN;
            }
            return type.cast(val);
        }

    }
}
