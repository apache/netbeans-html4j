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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;

/** Represents a property in a class defined with {@link Model} annotation.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface Property {
    /** Name of the property. Will be used to define proper getter and setter
     * in the associated class.
     * 
     * @return valid java identifier
     */
    String name();
    
    /** Type of the property. Can either be primitive type (like <code>int.class</code>,
     * <code>double.class</code>, etc.), {@link String}, {@link Enum enum} or complex model
     * class (defined by {@link Model} property).
     * 
     * @return the class of the property
     */
    Class<?> type();
    
    /** Is this property an array of the {@link #type()} or a single value?
     * If the property is an array, only its getter (returning mutable {@link List} of
     * the boxed {@link #type()}) is generated.
     * 
     * @return true, if this property is supposed to represent an array of values
     */
    boolean array() default false;

    /** Can the value of the property be mutated without restriction or not.
     * If a property is defined as <em>not mutable</em>, it defines
     * semi-immutable value that can only be changed in construction time
     * before the object is passed to underlying {@link Technology}. 
     * Attempts to modify the object later yield {@link IllegalStateException}.
     *
     * Technologies may decide to represent such non-mutable
     * property in more effective way - for
     * example Knockout Java Bindings technology (with {@link Contexts.Id id} "ko4j")
     * uses plain JavaScript value (number, string, array, boolean) rather
     * than classical observable.
     *
     * @return false if the value cannot change after its <em>first use</em>
     * @since 1.3
     */
    boolean mutable() default true;
}
