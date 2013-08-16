/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
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
 * The overall behavior of the system mimics <a href="http://www.w3.org/TR/2012/PR­geolocation­API­20120510/">
 * W3C's Geolocation API</a>.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
