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

import net.java.html.json.Context;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.Transfer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JSON {

    private JSON() {
    }

    public static void extract(Context c, Object value, String[] props, Object[] values) {
        Transfer t = ContextAccessor.findTransfer(c);
        t.extract(value, props, values);
    }
    
    private static Object getProperty(Context c, Object obj, String prop) {
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

    public static String toString(Context c, Object obj, String prop) {
        obj = getProperty(c, obj, prop);
        return obj instanceof String ? (String)obj : null;
    }
    public static Number toNumber(Context c, Object obj, String prop) {
        obj = getProperty(c, obj, prop);
        if (!(obj instanceof Number)) {
            obj = Double.NaN;
        }
        return (Number)obj;
    }

    
    public static void loadJSON(
        Context c, Runnable whenDone, Object[] result, 
        String urlBefore, String urlAfter
    ) {
        JSONCall call = PropertyBindingAccessor.createCall(whenDone, result, urlBefore, urlAfter);
        Transfer t = ContextAccessor.findTransfer(c);
        t.loadJSON(call);
    }
}
