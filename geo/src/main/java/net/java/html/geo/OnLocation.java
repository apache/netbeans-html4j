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
package net.java.html.geo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Generates a handle which can configure, {@link Position.Handle#start() start}
 * and {@link Position.Handle#stop() stop} requests for obtaining current
 * location of the application/device/user. Put the {@link OnLocation} annotation
 * on top of a (non-private) method in your class which takes one argument
 * {@link Position}. Based on name of your method (unless a class name is
 * directly specified via {@link #className()} attribute) a handle class is
 * generated. For example if your method name is <code>callMeWhenYouKnowWhereYouAre</code>
 * a package private class <code>CallMeWhenYouKnowWhereYouAreHandle</code> will
 * be generated in the same package. One can use its <code>createQuery</code>
 * and <code>createWatch</code> static method to get one time/repeated time
 * instance of a {@link Position.Handle handle}. After configuring the
 * {@link Position.Handle handle} via its setter methods, one can 
 * {@link Position.Handle#start() start} the location request.
 * <p>
 * In case something goes wrong a method in the same class named {@link #onError()}
 * can be specified (should take one {@link Exception} parameter). 
 * <p>
 * The overall behavior of the system mimics <a href="http://www.w3.org/TR/geolocation-API/">
 * W3C's Geolocation API</a>.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OnLocation {
    /** Name of the {@link Position.Handle handle} class to generate. By
     * default the name is derived from the name of annotated method by
     * capitalizing the first character and appending <code>Handle</code>.
     * <p>
     * The generated class contains two static methods: <code>createQuery</code>
     * and <code>createWatch</code> which take no parameters if this method 
     * is static or one parameter (of this class) if this method is instance
     * one. Both static methods return {@link Position.Handle}.
     * 
     * @return string suitable for a new class in the same package
     */
    public String className() default "";
    
    /** Name of a method in this class which should be called in case of 
     * an error. The method has to be non-private and take {@link Exception} 
     * parameter. If this method is not specified, the exception is just
     * printed to console.
     * 
     * @return name of method in this class
     */
    public String onError() default "";
}
