/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package net.java.html;

import java.util.ServiceLoader;
import java.util.logging.Logger;
import org.netbeans.html.context.impl.CtxAccssr;
import org.netbeans.html.context.impl.CtxImpl;
import org.apidesign.html.context.spi.Contexts;

/** Represents context where the <code>net.java.html.json.Model</code>
 * and other objects
 * operate in. The context is usually a particular HTML page in a browser.
 * The context is also associated with the actual HTML technology
 * in the HTML page - there is likely to be different context for 
 * <a href="http://knockoutjs.com">knockout.js</a> and different one
 * for <a href="http://angularjs.org">angular</a>.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class BrwsrCtx {
    private static final Logger LOG = Logger.getLogger(BrwsrCtx.class.getName());
    private final CtxImpl impl;
    private BrwsrCtx(CtxImpl impl) {
        this.impl = impl;
    }
    static {
        new CtxAccssr() {
            @Override
            protected BrwsrCtx newContext(CtxImpl impl) {
                return new BrwsrCtx(impl);
            }

            @Override
            protected CtxImpl find(BrwsrCtx context) {
                return context.impl;
            }
        };
    }
    /** Dummy context without binding to any real browser or technology. 
     * Useful for simple unit testing of behavior of various business logic
     * code.
     */
    public static final BrwsrCtx EMPTY = Contexts.newBuilder().build();
    
    /** Seeks for the default context that is associated with the requesting
     * class. If no suitable context is found, a warning message is
     * printed and {@link #EMPTY} context is returned.
     * 
     * @param requestor the class that makes the request
     * @return appropriate context for the request
     */
    public static BrwsrCtx findDefault(Class<?> requestor) {
        org.apidesign.html.context.spi.Contexts.Builder cb = Contexts.newBuilder();
        boolean found = false;
        
        ClassLoader l;
        try {
            l = requestor.getClassLoader();
        } catch (SecurityException ex) {
            l = null;
        }
        
        for (org.apidesign.html.context.spi.Contexts.Provider cp : ServiceLoader.load(
            org.apidesign.html.context.spi.Contexts.Provider.class, l
        )) {
            cp.fillContext(cb, requestor);
            found = true;
        }
        try {
            for (org.apidesign.html.context.spi.Contexts.Provider cp : ServiceLoader.load(org.apidesign.html.context.spi.Contexts.Provider.class, org.apidesign.html.context.spi.Contexts.Provider.class.getClassLoader())) {
                cp.fillContext(cb, requestor);
                found = true;
            }
        } catch (SecurityException ex) {
            if (!found) {
                throw ex;
            }
            // if we have some data from regular provides, go on
        }
        if (!found) {
            for (org.apidesign.html.context.spi.Contexts.Provider cp : ServiceLoader.load(org.apidesign.html.context.spi.Contexts.Provider.class)) {
                cp.fillContext(cb, requestor);
                found = true;
            }
        }
        if (!found) {
            LOG.warning("No browser context found. Returning empty technology!");
            return EMPTY;
        }
        return cb.build();
    }
    
}
