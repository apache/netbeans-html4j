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
package org.netbeans.html.boot.fx;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Redirects JavaScript's messages to Java's {@link Logger}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class FXConsole {
    static final Logger LOG = Logger.getLogger(FXConsole.class.getName());
    
    private FXConsole() {
    }

    static void register(WebEngine eng) {
        JSObject fn = (JSObject) eng.executeScript(""
            + "(function(attr, l, c) {"
            + "  window.console[attr] = function(msg) { c.log(l, msg); };"
            + "})"
        );
        FXConsole c = new FXConsole();
        c.registerImpl(fn, "log", Level.INFO);
        c.registerImpl(fn, "info", Level.INFO);
        c.registerImpl(fn, "warn", Level.WARNING);
        c.registerImpl(fn, "error", Level.SEVERE);
    }
    
    private void registerImpl(JSObject eng, String attr, Level l) {
        eng.call("call", null, attr, l, this);
    }
    
    public void log(Level l, String msg) {
        LOG.log(l, msg);
    }
}
