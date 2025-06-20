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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

public class ResourceOrderTest {

    public ResourceOrderTest() {
    }

    @Test
    public void testLoadData() throws Exception {
        InputStream is = ResourceOrder.class.getResourceAsStream("ResourceOrder.class");

        int[] valueCount = { 0 };
        List<String> visibleValues = new ArrayList<>();
        List<String> hiddenValues = new ArrayList<>();

        ClassReader r = new ClassReader(is);
        r.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return new AnnotationVisitor(api) {
                    @Override
                    public void visit(String name, Object value) {
                        Assert.fail("No values at level one: " + name + " value: " + value);
                    }

                    @Override
                    public AnnotationVisitor visitAnnotation(String name, String desc) {
                        Assert.fail("visitAnnotation: " + name);
                        return null;
                    }

                    @Override
                    public AnnotationVisitor visitArray(String name) {
                        Assert.assertEquals(name, "value");
                        valueCount[0]++;
                        return new AnnotationVisitor(api) {
                            @Override
                            public void visitEnd() {
                            }

                            @Override
                            public AnnotationVisitor visitArray(String name) {
                                Assert.fail("visitArray: " + name);
                                return null;
                            }

                            @Override
                            public AnnotationVisitor visitAnnotation(String name, String desc) {
                                Assert.assertEquals(desc, "Lnet/java/html/js/JavaScriptResource;");
                                return new AnnotationVisitor(api) {
                                    @Override
                                    public void visitEnd() {
                                    }

                                    @Override
                                    public AnnotationVisitor visitArray(String name) {
                                        Assert.fail("visitArray: " + name);
                                        return null;
                                    }

                                    @Override
                                    public AnnotationVisitor visitAnnotation(String name, String desc) {
                                        Assert.fail("visitAnnotation: " + name + " desc: " + desc);
                                        return null;
                                    }

                                    @Override
                                    public void visitEnum(String name, String desc, String value) {
                                        Assert.fail("visitEnum " + name + " desc: " + desc + " value: " + value);
                                    }

                                    @Override
                                    public void visit(String name, Object value) {
                                        Assert.assertEquals(name, "value");
                                        Assert.assertTrue(value instanceof String, "It is a string: " + value);
                                        var values = visible ? visibleValues : hiddenValues;
                                        values.add((String) value);
                                    }
                                };
                            }

                            @Override
                            public void visitEnum(String name, String desc, String value) {
                                Assert.fail("visitEnum");
                            }

                            @Override
                            public void visit(String name, Object value) {
                                Assert.fail(name);
                            }
                        };
                    }

                    @Override
                    public void visitEnd() {
                    }
                };
            }
        }, 0);
        assertValues("Hidden values found", hiddenValues);
        assertTrue("No visible visible values after processing with Maven plugin: " + visibleValues, visibleValues.isEmpty());
    }

    private void assertValues(String prefix, List<String> values) {
        assertEquals(values.size(), 3, prefix + ": There are there elements: " + values);
        assertEquals(values.get(0), "initArray.js", prefix + " 0");
        assertEquals(values.get(1), "addHello.js", prefix + " 1");
        assertEquals(values.get(2), "addWorld.js", prefix + " 2");
    }
}
