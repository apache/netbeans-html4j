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
import org.apidesign.html.json.spi.Technology;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bindings<Data> {
    private final Data data;
    private final Technology<Data> bp;

    public Bindings(Data data, Technology<Data> bp) {
        this.data = data;
        this.bp = bp;
    }
    
    public static Bindings<?> apply(Context c, Object model, String[] propsAndGetters, String[] functions) {
        Technology<?> bp = ContextAccessor.findTechnology(c);
        return apply(bp, model, propsAndGetters, functions);
    }
    
    private static <Data> Bindings<Data> apply(
        Technology<Data> bp, Object model, 
        String[] propsAndGetters, String[] functions
    ) {
        Data d = bp.wrapModel(model);
        
        return new Bindings<>(d, bp);
    }
    
    
    public Object koData() {
        return this;
    }

    public void valueHasMutated(String firstName) {
        bp.valueHasMutated(data, firstName);
    }
}
