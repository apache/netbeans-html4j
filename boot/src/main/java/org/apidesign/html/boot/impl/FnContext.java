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
package org.apidesign.html.boot.impl;

import org.apidesign.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class FnContext {
    private FnContext() {
    }
    
    private static final ThreadLocal<Fn.Presenter> CURRENT = new ThreadLocal<Fn.Presenter>();

    public static Fn.Presenter currentPresenter(Fn.Presenter p) {
        Fn.Presenter prev = CURRENT.get();
        CURRENT.set(p);
        return prev;
    }

    public static Fn.Presenter currentPresenter() {
        Fn.Presenter p = CURRENT.get();
        if (p == null) {
            throw new IllegalStateException("No current WebView context around!");
        }
        return p;
    }
    
}
