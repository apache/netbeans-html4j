/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.java.html.json;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
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
