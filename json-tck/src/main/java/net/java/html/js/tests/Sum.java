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
package net.java.html.js.tests;

/**
 *
 * @author Jaroslav Tulach
 */
public final class Sum {
    public int sum(int a, int b) {
        return a + b;
    }
    
    public int sum(Object[] arr) {
        int s = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Number) {
                s += ((Number)arr[i]).intValue();
            }
        }
        return s;
    }

    public int sumNonNull(Object[] arr) {
        int s = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                s++;
            }
        }
        return s;
    }

    public boolean checkNonNull(Object obj) {
        return obj != null;
    }
    
    public String all(boolean z, byte b, short s, int i, long l, float f, double d, char ch, String str) {
        return "Ahoj" + z + b + s + i + l + f + d + ch + str;
    }
}
