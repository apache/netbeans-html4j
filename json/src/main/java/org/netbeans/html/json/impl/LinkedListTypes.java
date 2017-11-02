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

import org.netbeans.html.json.spi.Proto;

final class LinkedListTypes implements ModelTypes {

    private Item items;

    private static final class Item {

        final Item next;
        final Class<?> clazz;
        final Proto.Type<?>[] type = {null};

        Item(Item next, Class<?> clazz) {
            this.next = next;
            this.clazz = clazz;
        }
    }

    @Override
    public synchronized Proto.Type[] find(Class<?> clazz) {
        Item it = items;
        while (it != null) {
            if (it.clazz == clazz) {
                return it.type;
            }
            it = it.next;
        }
        it = new Item(items, clazz);
        items = it;
        return it.type;
    }

}
