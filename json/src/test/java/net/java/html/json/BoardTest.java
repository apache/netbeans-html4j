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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "Board", properties = {
    @Property(name = "rows", type = Row.class, array = true)
})
public class BoardTest {
    
    @Model(className = "Row", properties = {
        @Property(name = "column", type = Column.class, array = true)
    })
    static class RowModel {
    }
    
    @Model(className = "Column", properties = {
        @Property(name = "black", type = boolean.class)
    })
    static class ColumnModel {
    }

    @Test public void deepClone() {
        Board orig = new Board(new Row(new Column(true)));
        assertTrue(orig.getRows().get(0).getColumn().get(0).isBlack(), "Really true");
        
        Board clone = orig.clone();
        assertTrue(clone.getRows().get(0).getColumn().get(0).isBlack(), "Clone also true");
        
        clone.getRows().get(0).getColumn().get(0).setBlack(false);
        
        assertFalse(clone.getRows().get(0).getColumn().get(0).isBlack(), "Clone also is not false");
        assertTrue(orig.getRows().get(0).getColumn().get(0).isBlack(), "Orig still true");
    }
    
}
