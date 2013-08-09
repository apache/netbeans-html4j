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
package net.java.html.json.tests;

import java.util.Arrays;
import java.util.List;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "Pair", properties = {
    @Property(name = "firstName", type = String.class),
    @Property(name = "lastName", type = String.class),
})
class PairModel {
    @ComputedProperty 
    static List<String> bothNames(String firstName, String lastName) {
        return Arrays.asList(firstName, lastName);
    }
    
    @Function 
    static void assignFirstName(Pair m, String data) {
        m.setFirstName(data);
    }
}
