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

import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className="BooleanArray", builder = "put", properties = {
    @Property(name = "array", type = boolean.class, array = true)
})
public class BooleanArrayTest {
    @ComputedProperty static int length(List<Boolean> array) {
        return array.size();
    }

    @ComputedProperty static List<Integer> lengthAsList(List<Boolean> array) {
        return Collections.nCopies(1, array.size());
    }

    @ComputedProperty static List<String> lengthTextList(List<Boolean> array) {
        return Collections.nCopies(1, "" + array.size());
    }
    
    @Test public void generatedConstructorWithPrimitiveType() {
        boolean[] arr = new boolean[10];
        arr[3] = true;
        BooleanArray a = new BooleanArray().putArray(arr);
        Assert.assertEquals(a.getArray().size(), 10, "Ten elements");
        Assert.assertEquals(a.getArray().get(3).booleanValue(), true, "Value ten");
        Assert.assertEquals(a.getLength(), 10, "Derived property is OK too");
        Assert.assertEquals(a.getLengthTextList().get(0), "10", "Derived string list property is OK");
        Assert.assertEquals((int)a.getLengthAsList().get(0), 10, "Derived Integer list property is OK");
    }
}
