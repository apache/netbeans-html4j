package com.dukescript.presenters.spi.test;

/*
 * #%L
 * DukeScript Generic Presenter - a library from the "DukeScript Presenters" project.
 * 
 * Dukehoff GmbH designates this particular file as subject to the "Classpath"
 * exception as provided in the README.md file that accompanies this code.
 * %%
 * Copyright (C) 2015 - 2019 Dukehoff GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import net.java.html.js.JavaScriptBody;

public final class Counter {
    static int calls;
    static int callbacks;

    static int count() {
        return ++callbacks;
    }
    
    public static final void registerCounter() {
        if (rCounter()) {
            callbacks = 0;
        }
    }

    @JavaScriptBody(args = {}, javacall = true, body
            = "if (!this.counter) {\n"
            + "  this.counter = function() { return @com.dukescript.presenters.spi.test.Counter::count()(); };\n"
            + "  return true;\n"
            + "} else {\n"
            + "  return false;\n"
            + "}\n"
    )
    private static native boolean rCounter();
    
}
