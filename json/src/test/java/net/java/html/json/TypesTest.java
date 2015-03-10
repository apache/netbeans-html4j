/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package net.java.html.json;

import net.java.html.BrwsrCtx;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.java.html.json.MapModelTest.One;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach
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
    @Property(name = "boolX", type = boolean.class),
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
        byte byteX, 
        short shortX, long longX, float floatX, 
        boolean boolX,
        char charX,
        double doubleX,
        String StringX, Types myModel) {
        
        myModel.setIntX(intX);
        myModel.setDoubleX(doubleX);
        myModel.setStringX(StringX);
        
        myModel.setByteX(byteX);
        myModel.setShortX(shortX);
        myModel.setLongX(longX);
        myModel.setFloatX(floatX);
        myModel.setBoolX(boolX);
        myModel.setCharX(charX);
    }
    
    @Test public void canParseEventAttributes() {
        Types t = Models.bind(new Types(), c);
        t.setIntX(33);
        t.setDoubleX(180.5);
        t.setStringX("Ahoj");
        t.setCharX('A');
        t.setByteX((byte)3);
        t.setShortX((short)10);
        t.setLongX(66);
        t.setFloatX(99f);
        t.setBoolX(true);
        
        assertValidJSON(t.toString());
        
        Object json = Models.toRaw(t);
        
        Types copy = Models.bind(new Types(), c);
        Map copyMap = (Map) Models.toRaw(copy);
        One o = (One) copyMap.get("readFromEvent");
        o.fb.call(null, json);
        
        assertEquals(copy.getIntX(), 33);
        assertEquals(copy.getDoubleX(), 180.5);
        assertEquals(copy.getStringX(), "Ahoj");
        assertEquals(copy.getByteX(), (byte)3);
        assertEquals(copy.getShortX(), (short)10);
        assertEquals(copy.getLongX(), 66L);
        assertEquals(copy.getFloatX(), 99f);
        assertTrue(copy.isBoolX());
        assertEquals(copy.getCharX(), 'A');
    }
    
    private static void assertValidJSON(String text) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine eng = sem.getEngineByMimeType("text/javascript");
        try {
            eng.eval("var obj = " + text + ";");
        } catch (ScriptException ex) {
            fail("Cannot parse " + text, ex);
        }
    }
}
