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
package org.netbeans.html.boot.impl;

import java.io.Closeable;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KeepAliveTest implements Fn.Presenter, Fn.KeepAlive, FindResources {
    private Class<?> jsMethods;
    @Test public void keepAliveIsSetToFalse() throws Exception {
        Closeable c = Fn.activate(this);
        Number ret = (Number)jsMethods.getMethod("checkAllowGC", Object.class).invoke(null, this);
        c.close();
        assertEquals(ret.intValue(), 0, "keepAlive is set to false");
    }    

    @Test public void keepAliveIsTheDefault() throws Exception {
        Closeable c = Fn.activate(this);
        Number ret = (Number)jsMethods.getMethod("plus", int.class, int.class).invoke(null, 40, 2);
        c.close();
        assertEquals(ret.intValue(), 1, "keepAlive returns true when the presenter is invoked");
    }    

    @BeforeMethod
    public void initClass() throws ClassNotFoundException {
        ClassLoader loader = FnUtils.newLoader(this, this, KeepAliveTest.class.getClassLoader().getParent());
        jsMethods = loader.loadClass(JsMethods.class.getName());
    }

    @Override
    public Fn defineFn(String code, String[] names, final boolean[] keepAlive) {
        return new Fn(this) {
            @Override
            public Object invoke(Object thiz, Object... args) throws Exception {
                boolean res = true;
                if (keepAlive != null) {
                    for (int i = 0; i < keepAlive.length; i++) {
                        res &= keepAlive[i];
                    }
                }
                return res ? 1 : 0;
            }
        };
    }
    
    @Override
    public Fn defineFn(String code, String... names) {
        throw new UnsupportedOperationException("Never called!");
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadScript(Reader code) throws Exception {
    }

    @Override
    public void findResources(String path, Collection<? super URL> results, boolean oneIsEnough) {
        URL u = ClassLoader.getSystemClassLoader().getResource(path);
        if (u != null) {
            results.add(u);
        }
    }
}
