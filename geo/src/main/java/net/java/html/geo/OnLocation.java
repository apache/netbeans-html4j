/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
