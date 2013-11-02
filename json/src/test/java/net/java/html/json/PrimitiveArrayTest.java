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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className="ByteArray", properties = {
    @Property(name = "array", type = byte.class, array = true)
})
public class PrimitiveArrayTest {
    @Test public void generatedConstructorWithPrimitiveType() {
        byte[] arr = new byte[10];
        arr[3] = 10;
        ByteArray a = new ByteArray(arr);
        Assert.assertEquals(a.getArray().size(), 10, "Ten elements");
        Assert.assertEquals(a.getArray().get(3).byteValue(), 10, "Value ten");
    }
}
