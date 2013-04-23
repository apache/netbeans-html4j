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
package net.java.html.json;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/** Represents a property in a class defined with {@link Model} annotation.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
     * the boxed {@link #type()}).
     * 
     * @return true, if this is supposed to be an array of values.
     */
    boolean array() default false;
}
