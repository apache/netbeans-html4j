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

import java.util.List;
import org.apidesign.html.json.impl.PropertyBindingAccessor;

/** Describes a property when one is asked to 
 * bind it 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class PropertyBinding {
    private final List<String> params;
    
    private PropertyBinding(List<String> p) {
        this.params = p;
    }

    static {
        new PropertyBindingAccessor() {
            @Override
            protected PropertyBinding newBinding(List<String> params) {
                return new PropertyBinding(params);
            }
        };
    }

    public String getPropertyName() {
        return params.get(0);
    }
    
    public String getGetterName() {
        final String g = params.get(1);
        int end = g.indexOf("__");
        if (end == -1) {
            end = g.length();
        }
        return g.substring(0, end);
    }

    public String getSetterName() {
        final String g = params.get(2);
        if (g == null) {
            return null;
        }
        int end = g.indexOf("__");
        if (end == -1) {
            end = g.length();
        }
        return g.substring(0, end);
    }
}
