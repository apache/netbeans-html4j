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
package org.apidesign.html.json.spi;

import net.java.html.json.Context;
import org.apidesign.html.json.impl.ContextAccessor;

/** Support for providers of new {@link Context}. Providers of different
 * technologies should be of particular interest in this class. End users
 * designing their application with existing technologies should rather
 * point their attention to {@link Context} and co.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ContextBuilder {
    private Technology<?> t;
    
    private ContextBuilder() {
    }
    
    /** Creates new, empty builder for creation of {@link Context}. At the
     * end call the {@link #build()} method to generate the context.
     * 
     * @return new instance of the builder
     */
    public static ContextBuilder create() {
        return new ContextBuilder();
    }
    
    /** Provides technology for the context
     * @param technology
     * @return this
     */
    public ContextBuilder withTechnology(Technology<?> technology) {
        this.t = technology;
        return this;
    }
    
    /** Generates context based on values previously inserted into
     * this builder.
     * 
     * @return new, immutable instance of {@link Context}
     */
    public Context build() {
        return ContextAccessor.create(t);
    }
}
