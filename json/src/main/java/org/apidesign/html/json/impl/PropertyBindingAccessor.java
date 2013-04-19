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

import java.util.List;
import org.apidesign.html.json.spi.PropertyBinding;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class PropertyBindingAccessor {
    private static PropertyBindingAccessor DEFAULT;

    protected PropertyBindingAccessor() {
        if (DEFAULT != null) throw new IllegalStateException();
        DEFAULT = this;
    }
    
    static {
        try {
            // run initializers
            Class.forName(PropertyBinding.class.getName(), 
                true, PropertyBinding.class.getClassLoader());
        } catch (Exception ex) {
            // OK
            throw new IllegalStateException(ex);
        }
    }

    protected abstract PropertyBinding newBinding(List<String> params);
    
    public static PropertyBinding create(
        List<String> params
    ) {
        return DEFAULT.newBinding(params);
    }
}
