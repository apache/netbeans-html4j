/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
import net.java.html.json.MapModelTest.One;
import org.apidesign.html.context.spi.Contexts;
import org.netbeans.html.json.impl.WrapperObject;
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
        Types t = Models.bind(new Types(), c);
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
        
        Types copy = Models.bind(new Types(), c);
        Map copyMap = (Map) WrapperObject.find(copy);
        One o = (One) copyMap.get("readFromEvent");
        o.fb.call(null, json);
        
        assertEquals(copy.getIntX(), 33);
        assertEquals(copy.getDoubleX(), 180.5);
        assertEquals(copy.getStringX(), "Ahoj");
//        assertEquals(copy.getCharX(), 'A');
    }
}
