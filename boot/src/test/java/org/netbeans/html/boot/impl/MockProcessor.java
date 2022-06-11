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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Processor.class)
public final class MockProcessor extends AbstractProcessor {
    private static final LinkedHashMap<BiConsumer<ProcessingEnvironment,RoundEnvironment>,Class<?>> PENDING = new LinkedHashMap<>();

    static void registerConsumer(Class<?> annotation, BiConsumer<ProcessingEnvironment,RoundEnvironment> consumer) {
        synchronized (PENDING) {
            PENDING.put(consumer, annotation);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (;;) {
            BiConsumer<ProcessingEnvironment, RoundEnvironment> c;
            synchronized (PENDING) {
                if (PENDING.isEmpty()) {
                    return true;
                }
                Iterator<Map.Entry<BiConsumer<ProcessingEnvironment, RoundEnvironment>, Class<?>>> it = PENDING.entrySet().iterator();
                c = it.next().getKey();
                it.remove();
            }
            c.accept(processingEnv, roundEnv);
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        synchronized (PENDING) {
            for (Class<?> c : PENDING.values()) {
                types.add(c.getName());
            }
        }
        return types;
    }
}
