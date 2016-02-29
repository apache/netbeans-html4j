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
package org.netbeans.html.xhr4j;

import java.io.IOException;
import java.io.InputStream;
import net.java.html.json.OnReceive;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.Transfer;
import org.openide.util.lookup.ServiceProvider;

/** Implementation module with support for XHR via Java.
 * Handles {@link OnReceive} requests by using Java to connect to given
 * URL and then parsing it via JavaScript. Use this module if you have
 * problems with CORS - as the Java connection isn't restricted by CORS
 * rules.
 * 
 * Registers {@link Transfer} technology at position <code>50</code>.
 * The {@link Contexts.Id} of the technology is <b>xhr4j</b>.
 * 
 * @author Jaroslav Tulach
 * @since 1.3
 */
@Contexts.Id("xhr4j")
@ServiceProvider(service = Contexts.Provider.class)
public final class XmlHttpResourceContext
implements Contexts.Provider, Transfer {
    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        context.register(Transfer.class, this, 50);
    }

    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        LoadJSON.extractJSON(obj, props, values);
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        return LoadJSON.parse(LoadJSON.readStream(is));
    }

    @Override
    public void loadJSON(JSONCall call) {
        LoadJSON.loadJSON(call);
    }
}
