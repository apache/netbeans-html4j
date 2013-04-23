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
import org.apidesign.html.json.spi.ContextBuilder;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;

/** Internal communication between API (e.g. {@link Context}), SPI
 * (e.g. {@link ContextBuilder}) and the implementation package.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class ContextAccessor {
    private static ContextAccessor DEFAULT;
    static {
        // run initializers
        Context.EMPTY.getClass();
    }
    
    protected ContextAccessor() {
        if (DEFAULT != null) throw new IllegalStateException();
        DEFAULT = this;
    }
    
    protected abstract Context newContext(Technology<?> t, Transfer r);
    protected abstract Technology<?> technology(Context c);
    protected abstract Transfer transfer(Context c);
    
    
    public static Context create(Technology<?> t, Transfer r) {
        return DEFAULT.newContext(t, r);
    }
    
    static Technology<?> findTechnology(Context c) {
        return DEFAULT.technology(c);
    }
    static Transfer findTransfer(Context c) {
        return DEFAULT.transfer(c);
    }
}
