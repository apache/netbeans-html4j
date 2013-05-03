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
package net.java.html.json;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public class ModelsTest {
    
    public ModelsTest() {
    }

    @Test public void peopleAreModel() {
        assertTrue(Models.isModel(People.class), "People are generated class");
    }
    
    @Test public void personIsModel() {
        assertTrue(Models.isModel(Person.class), "Person is generated class");
    }

    @Test public void implClassIsNotModel() {
        assertFalse(Models.isModel(PersonImpl.class), "Impl is not model");
    }

    @Test public void randomClassIsNotModel() {
        assertFalse(Models.isModel(StringBuilder.class), "JDK classes are not model");
    }
}