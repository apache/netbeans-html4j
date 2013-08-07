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
package org.apidesign.html.context.impl;

import net.java.html.BrwsrCtx;

/** Internal communication between API (e.g. {@link BrwsrCtx}), SPI
 * (e.g. {@link ContextBuilder}) and the implementation package.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class CtxAccssr {
    private static CtxAccssr DEFAULT;
    static {
        // run initializers
        try {
            BrwsrCtx.EMPTY.getClass();
        } catch (NullPointerException ex) {
            // ignore
        }
    }
    
    protected CtxAccssr() {
        if (DEFAULT != null) throw new IllegalStateException();
        DEFAULT = this;
    }
    
    protected abstract BrwsrCtx newContext(CtxImpl impl);
    protected abstract CtxImpl find(BrwsrCtx context);
    
    static CtxAccssr getDefault() {
        return DEFAULT;
    }
}
