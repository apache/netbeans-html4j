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

import net.java.html.BrwsrCtx;
import java.util.Map;
import net.java.html.json.MapModelTest.One;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.impl.WrapperObject;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "Types", properties = {
    @Property(name = "intX", type = int.class),
    @Property(name = "byteX", type = byte.class),
    @Property(name = "shortX", type = short.class),
    @Property(name = "longX", type = long.class),
    @Property(name = "floatX", type = float.class),
    @Property(name = "doubleX", type = double.class),
    @Property(name = "charX", type = char.class),
    @Property(name = "StringX", type = String.class),
})
public class TypesTest {
    private MapModelTest.MapTechnology t;
    private BrwsrCtx c;

    @BeforeMethod public void initTechnology() {
        t = new MapModelTest.MapTechnology();
        c = Contexts.newBuilder().register(Technology.class, t, 1).
            register(Transfer.class, t, 1).build();
    }
    @Function static void readFromEvent(int intX, 
        /*
        byte byteX, 
        short shortX, long longX, float floatX, 
        char charX
        */
        double doubleX,
        String StringX, Types myModel) {
        
        myModel.setIntX(intX);
        myModel.setDoubleX(doubleX);
        myModel.setStringX(StringX);
        
        /*
        myModel.setByte(byteX);
        myModel.setShort(shortX);
        myModel.setLong(longX);
        myModel.setFloat(floatX);
        */
    }
    
    @Test public void canParseEventAttributes() {
        Types t = new Types(c);
        t.setIntX(33);
        t.setDoubleX(180.5);
        t.setStringX("Ahoj");
        
        /*
        t.setCharX('A');
        t.setByteX((byte)3);
        t.setShortX((short)10);
        t.setLongX(66);
        t.setFloatX(99f);
        */
        
        Object json = WrapperObject.find(t);
        
        Types copy = new Types(c);
        Map copyMap = (Map) WrapperObject.find(copy);
        One o = (One) copyMap.get("readFromEvent");
        o.fb.call(null, json);
        
        assertEquals(copy.getIntX(), 33);
        assertEquals(copy.getDoubleX(), 180.5);
        assertEquals(copy.getStringX(), "Ahoj");
//        assertEquals(copy.getCharX(), 'A');
    }
}
