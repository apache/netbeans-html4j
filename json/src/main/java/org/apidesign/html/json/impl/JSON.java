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
package org.apidesign.html.json.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.java.html.BrwsrCtx;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JSON {
    /** represents null exception value */
    public static final Exception NULL = new NullPointerException();
    
    private JSON() {
    }

    static Technology<?> findTechnology(BrwsrCtx c) {
        Technology<?> t = Contexts.find(c, Technology.class);
        return t == null ? EmptyTech.EMPTY : t;
    }

    static Transfer findTransfer(BrwsrCtx c) {
        Transfer t = Contexts.find(c, Transfer.class);
        return t == null ? EmptyTech.EMPTY : t;
    }
    
    public static void runInBrowser(BrwsrCtx c, Runnable runnable) {
        findTechnology(c).runSafe(runnable);
    }
    
    public static void extract(BrwsrCtx c, Object value, String[] props, Object[] values) {
        Transfer t = findTransfer(c);
        t.extract(value, props, values);
    }
    
    private static Object getProperty(BrwsrCtx c, Object obj, String prop) {
        if (prop == null) return obj;
        
        String[] arr = { prop };
        Object[] val = { null };
        extract(c, obj, arr, val);
        return val[0];
    }

    public static Object toJSON(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Enum) {
            value = value.toString();
        }
        if (value instanceof String) {
            return '"' + 
                ((String)value).
                    replace("\"", "\\\"").
                    replace("\n", "\\n").
                    replace("\r", "\\r").
                    replace("\t", "\\t")
                + '"';
        }
        return value.toString();
    }

    public static String toString(BrwsrCtx c, Object obj, String prop) {
        obj = getProperty(c, obj, prop);
        return obj instanceof String ? (String)obj : null;
    }
    public static Number toNumber(BrwsrCtx c, Object obj, String prop) {
        obj = getProperty(c, obj, prop);
        if (!(obj instanceof Number)) {
            obj = Double.NaN;
        }
        return (Number)obj;
    }
    public static <M> M toModel(BrwsrCtx c, Class<M> aClass, Object data, Object object) {
        Technology<?> t = findTechnology(c);
        Object o = t.toModel(aClass, data);
        return aClass.cast(o);
    }

    public static <T> T extractValue(Class<T> type, Object val) {
        if (Number.class.isAssignableFrom(type)) {
            val = numberValue(val);
        }
        if (Boolean.class == type) {
            val = boolValue(val);
        }
        return type.cast(val);
    }
    
    public static String stringValue(Object val) {
        return (String)val;
    }

    public static Number numberValue(Object val) {
        if (val instanceof String) {
            try {
                return Double.valueOf((String)val);
            } catch (NumberFormatException ex) {
                return Double.NaN;
            }
        }
        return (Number)val;
    }

    public static Character charValue(Object val) {
        return (Character)val;
    }
    
    public static Exception excValue(Object val) {
        if (val == NULL) {
            return null;
        }
        if (val instanceof Exception) {
            return (Exception)val;
        }
        return new Exception(val.toString());
    }

    public static Boolean boolValue(Object val) {
        if (val instanceof String) {
            return Boolean.parseBoolean((String)val);
        }
        return Boolean.TRUE.equals(val);
    }
    
    public static void loadJSON(
        BrwsrCtx c, Runnable whenDone, Object[] result, 
        String urlBefore, String urlAfter
    ) {
        loadJSON(c, whenDone, result, urlBefore, urlAfter, null, null);
    }

    public static void loadJSON(
        BrwsrCtx c, Runnable whenDone, Object[] result,
        String urlBefore, String urlAfter, String method,
        Object data
    ) {
        JSONCall call = PropertyBindingAccessor.createCall(whenDone, result, urlBefore, urlAfter, method, data);
        Transfer t = findTransfer(c);
        t.loadJSON(call);
    }
    
    private static final Map<Class,FromJSON<?>> froms;
    static {
        Map<Class,FromJSON<?>> m = new HashMap<Class,FromJSON<?>>();
        froms = m;
    }
    public static void register(FromJSON<?> from) {
        froms.put(from.factoryFor(), from);
    }
    
    public static boolean isModel(Class<?> clazz) {
        return findFrom(clazz) != null; 
    }
    
    private static FromJSON<?> findFrom(Class<?> clazz) {
        for (int i = 0; i < 2; i++) {
            FromJSON<?> from = froms.get(clazz);
            if (from == null) {
                initClass(clazz);
            } else {
                return from;
            }
        }
        return null;
    }
    
    public static <Model> Model bindTo(Model model, BrwsrCtx c) {
        FromJSON<?> from = findFrom(model.getClass());
        if (from == null) {
            throw new IllegalArgumentException();
        }
        return (Model) from.cloneTo(model, c);
    }
    
    public static <T> T readStream(BrwsrCtx c, Class<T> modelClazz, InputStream data) 
    throws IOException {
        Transfer tr = findTransfer(c);
        return read(c, modelClazz, tr.toJSON((InputStream)data));
    }
    public static <T> T read(BrwsrCtx c, Class<T> modelClazz, Object data) {
        if (data == null) {
            return null;
        }
        if (modelClazz == String.class) {
            return modelClazz.cast(data.toString());
        }
        for (int i = 0; i < 2; i++) {
            FromJSON<?> from = froms.get(modelClazz);
            if (from == null) {
                initClass(modelClazz);
            } else {
                return modelClazz.cast(from.read(c, data));
            }
        }
        throw new NullPointerException();
    }
    static void initClass(Class<?> modelClazz) {
        try {
            // try to resolve the class
            ClassLoader l;
            try {
                l = modelClazz.getClassLoader();
            } catch (SecurityException ex) {
                l = null;
            }
            if (l != null) {
                Class.forName(modelClazz.getName(), true, l);
            }
            modelClazz.newInstance();
        } catch (Exception ex) {
            // ignore and try again
        }
    }
    
    private static final class EmptyTech implements Technology<Object>, Transfer {
        private static final EmptyTech EMPTY = new EmptyTech();

        @Override
        public Object wrapModel(Object model) {
            return model;
        }

        @Override
        public void valueHasMutated(Object data, String propertyName) {
        }

        @Override
        public void bind(PropertyBinding b, Object model, Object data) {
        }

        @Override
        public void expose(FunctionBinding fb, Object model, Object d) {
        }

        @Override
        public void applyBindings(Object data) {
        }

        @Override
        public Object wrapArray(Object[] arr) {
            return arr;
        }

        @Override
        public void extract(Object obj, String[] props, Object[] values) {
            for (int i = 0; i < values.length; i++) {
                values[i] = null;
            }
        }

        @Override
        public void loadJSON(JSONCall call) {
            call.notifyError(new UnsupportedOperationException());
        }

        @Override
        public <M> M toModel(Class<M> modelClass, Object data) {
            return modelClass.cast(data);
        }

        @Override
        public Object toJSON(InputStream is) throws IOException {
            throw new IOException("Not supported");
        }

        @Override
        public synchronized void runSafe(Runnable r) {
            r.run();
        }
    }
    
}
