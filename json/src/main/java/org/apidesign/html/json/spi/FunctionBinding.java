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

import net.java.html.json.Function;
import net.java.html.json.Model;
import org.apidesign.html.json.impl.PropertyBindingAccessor.FBData;

/** Describes a function provided by the {@link Model} and 
 * annotated by {@link Function} annotation.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class FunctionBinding {
    private final FBData<?> fb;
    
    FunctionBinding(FBData<?> fb) {
        this.fb = fb;
    }

    public String getFunctionName() {
        return fb.name;
    }
    
    /** Calls the function provided data associated with current element,
     * as well as information about the event that triggered the event.
     * 
     * @param data data associated with selected element
     * @param ev event (with additional properties) that triggered the event
     */
    public void call(Object data, Object ev) {
        try {
            fb.call(data, ev);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
