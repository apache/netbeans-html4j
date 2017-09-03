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
package org.netbeans.html.json.tck;

import net.java.html.js.tests.GCBodyTest;
import net.java.html.js.tests.JavaScriptBodyTest;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/** Entry point for those who want to verify that their implementation of
 * {@link Presenter} is good enough to support existing Java/JavaScript 
 * communication use-cases. Subclass this class, get list of {@link #testClasses() classes}
 * find methods annotated by {@link KOTest} annotation and execute them.
 * <p>
 *
 * @author Jaroslav Tulach
 * @since 0.7
 */
public abstract class JavaScriptTCK {
    /** Gives you list of classes included in the TCK. Their test methods
     * are annotated by {@link KOTest} annotation. The methods are public
     * instance methods that take no arguments. The method should be 
     * invoke in a presenter context {@link Fn#activate(org.netbeans.html.boot.spi.Fn.Presenter)}.
     * 
     * @return classes with methods annotated by {@link KOTest} annotation
     */
    protected static Class<?>[] testClasses() {
        return new Class[] { 
            JavaScriptBodyTest.class, GCBodyTest.class
        };
    }
    
}
