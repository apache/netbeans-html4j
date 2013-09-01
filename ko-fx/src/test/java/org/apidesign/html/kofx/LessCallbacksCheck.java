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
package org.apidesign.html.kofx;

import java.io.PrintWriter;
import java.io.StringWriter;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.apidesign.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "LessCalls", properties = {
    @Property(name = "value", type = int.class)
})
public class LessCallbacksCheck {
    private static StringWriter sw;
    
    @ComputedProperty static int plusOne(int value) {
        if (sw == null) {
            sw = new StringWriter();
        }
        new Exception("Who calls me?").printStackTrace(
            new PrintWriter(sw)
        );
        return value + 1;
    }
    
    @KOTest public void dontCallForInitialValueBackToJavaVM() {
        LessCalls m = new LessCalls(10).applyBindings();
        assert m.getPlusOne() == 11 : "Expecting 11: " + m.getPlusOne();
        
        assert sw != null : "StringWriter should be initialized: " + sw;
        
        if (sw.toString().contains("$JsCallbacks$")) {
            assert false : "Don't call for initial value via JsCallbacks:\n" + sw;
        }
    }
}
