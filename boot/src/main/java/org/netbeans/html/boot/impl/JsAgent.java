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
package org.netbeans.html.boot.impl;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;

/**
 *
 * @author Jaroslav Tulach
 */
public final class JsAgent implements ClassFileTransformer {
    public static void premain(String args, Instrumentation instr) {
        instr.addTransformer(new JsAgent());
    }
    
    public static void agentmain(String args, Instrumentation instr) {
        instr.addTransformer(new JsAgent());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if (containsJavaBodies(classfileBuffer)) {
                return FnUtils.transform(classfileBuffer, loader, true);
            } else {
                return classfileBuffer;
            }
        } catch (Error | Exception ex) {
            System.err.println("Error transforming " + className);
            ex.printStackTrace();
            return classfileBuffer;
        }
    }

    private static final byte[] PATTERN = new String("net/java/html/js/").getBytes(StandardCharsets.UTF_8);
    private static boolean containsJavaBodies(byte[] arr) {
        NOT_FOUND: for (var i = 0; i < arr.length - PATTERN.length; i++) {
            for (var j = 0; j < PATTERN.length; j++) {
                if (arr[i + j] != PATTERN[j]) {
                    continue NOT_FOUND;
                }
            }
            return true;
        }
        return false;
    }
}
