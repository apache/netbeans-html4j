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
package org.apidesign.html.boot.impl;

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
        if (name.equals(FnUtils.class.getName())) {
            return FnUtils.class;
        }
        if (
            name.equals("org.apidesign.html.boot.spi.Fn") ||
            name.equals("org.apidesign.html.boot.impl.FnUtils") ||
            name.equals("org.apidesign.html.boot.impl.FnContext")
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
                arr = FnUtils.transform(arr, JsClassLoader.this);
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
