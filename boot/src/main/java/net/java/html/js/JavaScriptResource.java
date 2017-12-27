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
package net.java.html.js;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Include JavaScript libraries into your application easily.
 * Annotate a Java/JavaScript bridging class with this annotation and
 * once one of the class {@code @}{@link JavaScriptBody} annotated methods
 * is called, it is guaranteed the JavaScript interpreter pre-load
 * the content of here is specified resource. All other 
 * {@code @}{@link JavaScriptBody} methods can then access objects created
 * by precessing this {@link JavaScriptResource#value() java script resource}.
 * The list of
 * <a href="package-summary.html#library">use-cases</a> includes sample use
 * of this annotation.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(JavaScriptResource.Group.class)
public @interface JavaScriptResource {
    /** The JavaScript file to load in before associated class can execute.
     * @return relative path with respect to the annotated class
     */
    public String value();

    /** Represents a group of resources to load. When initializing element
     * annotated by {@code Group} annotation, load all resources, one by one, in the
     * order they appear in the {@link Group#value() array}.
     *
     * @since 1.6
     */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE)
    public static @interface Group {
        /** Multiple instances of {@link JavaScriptResource} to load.
         *
         * @return array of resources to load
         */
        JavaScriptResource[] value();
    }
}
