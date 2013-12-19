/**
 * HTML via Java(tm) Language Bindings Copyright (C) 2013 Jaroslav Tulach
 * <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. apidesign.org designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org in the License file that
 * accompanied this code.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. Look for COPYING file in the top folder. If not, see
 * http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */

package net.java.html.js.tests;

import net.java.html.js.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Factorial {
    int minusOne(int i) {
        return i - 1;
    }

    @JavaScriptBody(args = { "i" }, javacall = true,body = 
        "if (i <= 1) return 1;\n"
      + "var im1 = this.@net.java.html.js.tests.Factorial::minusOne(I)(i);\n"
      + "return this.@net.java.html.js.tests.Factorial::factorial(I)(im1) * i;"
    )
    native int factorial(int n);
}
