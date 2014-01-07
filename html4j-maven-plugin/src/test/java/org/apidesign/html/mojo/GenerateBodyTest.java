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
package org.apidesign.html.mojo;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.boot.spi.Fn;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class GenerateBodyTest implements Fn.Presenter {
    @JavaScriptBody(args = {}, body = "return true;")
    public static native boolean generateMe();
    
    @Test public void generateMeReturnsTrue() throws IOException {
        Closeable c = Fn.activate(this);
        try {
            assertTrue(generateMe(), "Body has been generated");
        } finally {
            c.close();
        }
    }
        
    @Override
    public Fn defineFn(String code, String... names) {
        return new Fn(this) {
            @Override
            public Object invoke(Object thiz, Object... args) throws Exception {
                return Boolean.TRUE;
            }
        };
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        throw new IllegalStateException();
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        throw new Exception();
    }
        
}
