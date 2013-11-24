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
