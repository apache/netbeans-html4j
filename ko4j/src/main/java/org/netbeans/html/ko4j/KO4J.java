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
package org.netbeans.html.ko4j;

import net.java.html.json.Model;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.context.spi.Contexts.Provider;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.spi.WSTransfer;
import org.openide.util.lookup.ServiceProvider;

/** Support for <a href="http://knockoutjs.com">knockout.js</a>
 * and its Java binding via {@link Model model classes}.
 * Registers {@link Provider}, so {@link java.util.ServiceLoader} can find it.
 *
 * @author Jaroslav Tulach
 * @since 0.7
 */
@ServiceProvider(service = Provider.class)
public final class KO4J implements Provider {
    private final Fn.Presenter presenter;
    private FXContext c;
    
    public KO4J() {
        this(null);
    }
    
    public KO4J(Fn.Presenter presenter) {
        this.presenter = presenter;
    }
    
    private FXContext getKO() {
        if (c == null) {
            c = new FXContext(presenter == null ? Fn.activePresenter() : presenter);
        }
        return c;
    }
    
    /** Return instance of the knockout.js for Java technology.
     * @return non-null instance
     */
    public Technology knockout() {
        return getKO();
    }
    
    /** Browser based implementation of transfer interface. Uses
     * browser method to convert string to JSON.
     * 
     * @return non-null instance
     */
    public Transfer transfer() {
        return getKO();
    }
    
    /** Returns browser based implementation of websocket transfer.
     * If available (for example JavaFX WebView on JDK7 does not have
     * this implementation).
     * 
     * @return an instance or <code>null</code>, if there is no
     *   <code>WebSocket</code> object in the browser
     */
    public WSTransfer<?> websockets() {
        return getKO().areWebSocketsSupported() ? getKO() : null;
    }

    /** Registers technologies at position 100:
     * <ul>
     *   <li>{@link #knockout()}</li>
     *   <li>{@link #transfer()}</li>
     *   <li>{@link #websockets()()} - if browser supports web sockets</li>
     * </ul>
     * @param context the context to register to
     * @param requestor the class requesting the registration
     */
    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        context.register(Technology.class, knockout(), 100);
        context.register(Transfer.class, transfer(), 100);
        context.register(WSTransfer.class, websockets(), 100);
    }
    
}
