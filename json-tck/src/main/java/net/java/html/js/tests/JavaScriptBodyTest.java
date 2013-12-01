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
package net.java.html.js.tests;

import org.apidesign.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JavaScriptBodyTest {
    @KOTest public void sumTwoNumbers() {
        int res = Bodies.sum(5, 3);
        assert res == 8 : "Expecting 8: " + res;
    }
    
    @KOTest public void accessJsObject() {
        Object o = Bodies.instance(10);
        int ten = Bodies.readX(o);
        assert ten == 10 : "Expecting ten: " + ten;
    }
    
    @KOTest public void callbackToRunnable() {
        R run = new R();
        Bodies.callback(run);
        assert run.cnt == 1 : "Can call even private implementation classes: " + run.cnt;
    }

    private static class R implements Runnable {
        int cnt;

        @Override
        public void run() {
            cnt++;
        }
    }
    
}
