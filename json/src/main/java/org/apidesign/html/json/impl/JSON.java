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

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JSON {

    private JSON() {
    }

    public static void extract(Object value, String[] props, Object[] values) {
    }
    
    private static Object getProperty(Object obj, String prop) {
        if (prop == null) return obj;
        
        String[] arr = { prop };
        Object[] val = { null };
        extract(obj, arr, val);
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

    public static String toString(Object obj, String prop) {
        obj = getProperty(obj, prop);
        return obj instanceof String ? (String)obj : null;
    }
    
    public static String createJSONP(Object[] res, Runnable callback) {
        return null;
    }

    public static Object loadJSON(String url, Object[] res, Runnable callback, String jsonp) {
        return null;
    }
}
