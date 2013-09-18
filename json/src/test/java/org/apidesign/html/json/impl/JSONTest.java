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

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JSONTest {
    
    public JSONTest() {
    }

    @Test public void longToStringValue() {
        assertEquals(JSON.stringValue(Long.valueOf(1)), "1");
    }
    
    @Test public void booleanIsSortOfNumber() {
        assertEquals(JSON.numberValue(Boolean.TRUE), Integer.valueOf(1));
        assertEquals(JSON.numberValue(Boolean.FALSE), Integer.valueOf(0));
    }
    
    @Test public void numberToChar() {
        assertEquals(JSON.charValue(65), Character.valueOf('A'));
    }
    @Test public void booleanToChar() {
        assertEquals(JSON.charValue(false), Character.valueOf((char)0));
        assertEquals(JSON.charValue(true), Character.valueOf((char)1));
    }
    @Test public void stringToChar() {
        assertEquals(JSON.charValue("Ahoj"), Character.valueOf('A'));
    }
    @Test public void stringToBoolean() {
        assertEquals(JSON.boolValue("false"), Boolean.FALSE);
        assertEquals(JSON.boolValue("True"), Boolean.TRUE);
    }
    @Test public void numberToBoolean() {
        assertEquals(JSON.boolValue(0), Boolean.FALSE);
        assertEquals(JSON.boolValue(1), Boolean.TRUE);
    }
}
