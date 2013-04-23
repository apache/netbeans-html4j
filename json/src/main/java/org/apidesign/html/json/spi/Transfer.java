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

package org.apidesign.html.json.spi;

/** A {@link ContextBuilder service provider interface} responsible for 
 * conversion of JSON objects to Java ones and vice-versa.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public interface Transfer {
    /**
     * Called to inspect properties of an object (usually a JSON or JavaScript
     * wrapper).
     *
     * @param obj the object to inspect
     * @param props the names of properties to check on the object
     * <code>obj</code>
     * @param values array of the same length as <code>props</code> should be
     * filled by values of properties on the <code>obj</code>. If a property is
     * not defined, a <code>null</code> value should be stored in the array
     */
    public void extract(Object obj, String[] props, Object[] values);
    
}
