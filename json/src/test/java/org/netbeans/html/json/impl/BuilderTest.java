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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
package org.netbeans.html.json.impl;

import java.lang.reflect.Constructor;
import net.java.html.json.Model;
import net.java.html.json.Property;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

@Model(className="Builder", builder = "", properties = {
    @Property(name="bytes", type = byte.class, array = true),
    @Property(name="chars", type = char.class, array = true),
    @Property(name="shorts", type = short.class, array = true),
    @Property(name="ints", type = int.class, array = true),
    @Property(name="longs", type = long.class, array = true),
    @Property(name="floats", type = float.class, array = true),
    @Property(name="doubles", type = double.class, array = true),
    @Property(name="strings", type = String.class, array = true),
})
public class BuilderTest {
    @Test
    public void onlyDefaultClassLoader() {
        Constructor<?>[] arr = Builder.class.getConstructors();
        assertEquals(arr.length, 1, "One constructor");
        assertEquals(arr[0].getParameterTypes().length, 0, "No parameters");
    }

    @Test
    public void assignBytes() {
        Builder b = new Builder().bytes((byte)10, (byte)20, (byte)30);
        assertEquals(b.getBytes().size(), 3);
        assertEquals(b.getBytes().get(0).byteValue(), (byte)10);
    }
    @Test
    public void assignChars() {
        Builder b = new Builder().chars((char)10, (char)20, (char)30);
        assertEquals(b.getChars().size(), 3);
        assertEquals(b.getChars().get(0).charValue(), 10);
    }
    @Test
    public void assignShort() {
        Builder b = new Builder().shorts((short)10, (short)20, (short)30);
        assertEquals(b.getShorts().size(), 3);
        assertEquals(b.getShorts().get(0).intValue(), 10);
    }
    @Test
    public void assignInts() {
        Builder b = new Builder().ints(10, 20, 30);
        assertEquals(b.getInts().size(), 3);
        assertEquals(b.getInts().get(0).intValue(), 10);
    }
    @Test
    public void assignLongs() {
        Builder b = new Builder().longs(10, 20, 30);
        assertEquals(b.getLongs().size(), 3);
        assertEquals(b.getLongs().get(1).intValue(), 20);
    }
    @Test
    public void assignDouble() {
        Builder b = new Builder().doubles(10, 20, 30);
        assertEquals(b.getDoubles().size(), 3);
        assertEquals(b.getDoubles().get(0), 10.0);
    }
    @Test
    public void assignFloats() {
        Builder b = new Builder().floats(10, 20, 30);
        assertEquals(b.getFloats().size(), 3);
        assertEquals(b.getFloats().get(0), 10.0f);
    }
    @Test
    public void assignStrings() {
        Builder b = new Builder().strings("A", "AB", "ABC");
        assertEquals(b.getStrings().size(), 3);
        assertEquals(b.getStrings().get(1), "AB");
    }
}
