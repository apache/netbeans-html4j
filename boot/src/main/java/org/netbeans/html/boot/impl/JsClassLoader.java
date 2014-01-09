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
package org.netbeans.html.boot.impl;

import org.apidesign.html.boot.spi.Fn;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;

/** 
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
abstract class JsClassLoader extends ClassLoader {
    JsClassLoader(ClassLoader parent) {
        super(parent);
        setDefaultAssertionStatus(JsClassLoader.class.desiredAssertionStatus());
    }
    
    @Override
    protected abstract URL findResource(String name);
    
    @Override
    protected abstract Enumeration<URL> findResources(String name);

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("javafx")) {
            return Class.forName(name);
        }
        if (name.startsWith("netscape")) {
            return Class.forName(name);
        }
        if (name.startsWith("com.sun")) {
            return Class.forName(name);
        }
        if (name.equals(JsClassLoader.class.getName())) {
            return JsClassLoader.class;
        }
        if (name.equals(Fn.class.getName())) {
            return Fn.class;
        }
        if (name.equals(Fn.Presenter.class.getName())) {
            return Fn.Presenter.class;
        }
        if (name.equals(Fn.ToJavaScript.class.getName())) {
            return Fn.ToJavaScript.class;
        }
        if (name.equals(Fn.FromJavaScript.class.getName())) {
            return Fn.FromJavaScript.class;
        }
        if (name.equals(FnUtils.class.getName())) {
            return FnUtils.class;
        }
        if (
            name.equals("org.apidesign.html.boot.spi.Fn") ||
            name.equals("org.netbeans.html.boot.impl.FnUtils") ||
            name.equals("org.netbeans.html.boot.impl.FnContext")
        ) {
            return Class.forName(name);
        }
        URL u = findResource(name.replace('.', '/') + ".class");
        if (u != null) {
            InputStream is = null;
            try {
                is = u.openStream();
                byte[] arr = new byte[is.available()];
                int len = 0;
                while (len < arr.length) {
                    int read = is.read(arr, len, arr.length - len);
                    if (read == -1) {
                        throw new IOException("Can't read " + u);
                    }
                    len += read;
                }
                is.close();
                is = null;
                arr = FnUtils.transform(JsClassLoader.this, arr);
                if (arr != null) {
                    return defineClass(name, arr, 0, arr.length);
                }
            } catch (IOException ex) {
                throw new ClassNotFoundException("Can't load " + name, ex);
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException ex) {
                    throw new ClassNotFoundException(null, ex);
                }
            }
        }
        return super.findClass(name);
    }
    
    protected abstract Fn defineFn(String code, String... names);
    protected abstract void loadScript(Reader code) throws Exception;
}
