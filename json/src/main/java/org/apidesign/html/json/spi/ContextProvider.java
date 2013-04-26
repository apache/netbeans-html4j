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
import org.openide.util.lookup.ServiceProvider;

/** Implementors of various {@link Technology technologies} should
 * register their implementation via {@link ServiceProvider} so 
 * {@link ServiceProvider} can find them, when their JARs are included
 * on the classpath of the running application.
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public interface ContextProvider {
    /** Identify the context suitable for provided requesting class. 
     * The provider should check if its own technology is available in current
     * scope (e.g. proper JDK, proper browser, etc.). The provider
     * can also find the right context depending on requestor's classloader, etc.
     * <p>
     * Providers should use {@link ContextBuilder} to construct appropriately
     * configured context.
     * 
     * @param requestor the application class requesting access the the HTML page
     * @return 
     */
    Context findContext(Class<?> requestor);
}
