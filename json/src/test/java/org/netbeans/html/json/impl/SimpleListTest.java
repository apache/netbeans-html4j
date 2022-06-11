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
package org.netbeans.html.json.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleListTest {

    public SimpleListTest() {
    }

    @DataProvider(name = "lists")
    public static Object[][] bothLists() {
        return new Object[][] {
            new Object[] { new ArrayList<Object>() },
            new Object[] { SimpleList.asList() },
        };
    }

    @Test(dataProvider = "lists")
    public void testListIterator(List<String> list) {
        list.add("Hi");
        list.add("Ahoj");
        list.add("Ciao");

        Collections.sort(list);

        ListIterator<String> it = list.listIterator(3);
        assertEquals(it.previous(), "Hi");
        assertEquals(it.previous(), "Ciao");
        it.remove();
        assertEquals(it.next(), "Hi");
        assertEquals(it.previous(), "Hi");
        assertEquals(it.previous(), "Ahoj");
        assertEquals(list.size(), 2);
    }

    @Test(dataProvider = "lists")
    public void toStringHashTest(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);
        assertEquals(list.toString(), "[3, 3.3, 4, 4.4]");
        assertEquals(list.hashCode(), 1374332816);
    }

    @Test(dataProvider = "lists")
    public void toStringHashSubListTest(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        list = list.subList(0, 4);

        assertEquals(list.toString(), "[3, 3.3, 4, 4.4]");
        assertEquals(list.hashCode(), 1374332816);
    }

    @Test(dataProvider = "lists")
    public void subListEqualsTest(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        assertEquals(list, list.subList(0, 4));
    }

    @Test(dataProvider = "lists")
    public void retainAll(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        list.retainAll(Collections.singleton(4L));

        assertEquals(list.size(), 1);
        assertEquals(list.get(0), 4L);
    }

    @Test(dataProvider = "lists")
    public void subListFromTwo(List<Number> list) {
        list.add(10);
        list.add(20);

        Number[] first = list.subList(0, 1).toArray(new Number[0]);
        assertEquals(1, first.length);
        assertEquals(10, first[0]);

        Number[] second = list.subList(1, 2).toArray(new Number[0]);
        assertEquals(1, second.length);
        assertEquals(20, second[0]);

        Number[] both = list.subList(0, 2).toArray(new Number[0]);
        assertEquals(2, both.length);
        assertEquals(10, both[0]);
        assertEquals(20, both[1]);
    }

    @Test(dataProvider = "lists")
    public void retainAllOnSubList(List<Number> list) {
        list.add(3);
        list.add(3.3f);
        list.add(4L);
        list.add(4.4);

        List<Number> subList = list.subList(1, 4);

        subList.retainAll(Collections.singleton(4L));

        assertEquals(subList.size(), 1);
        assertEquals(subList.get(0), 4L);

        assertEquals(list.size(), 2);
    }

}
