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
package net.java.html.json.tests;

import net.java.html.json.Models;
import org.apidesign.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OperationsTest {
    private JSONik js;
    private Person p;
    
    @KOTest public void syncOperation() {
        js = Models.bind(new JSONik(), Utils.newContext(OperationsTest.class));
        p = new Person("Sitar", "Gitar", Sex.MALE, null);
        js.applyBindings();
        js.setFetched(p);
        Person p = js.getFetched();
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar immediately: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE immediately: " + p.getSex();
    }
    
    
    @KOTest public void asyncOperation() throws InterruptedException {
        if (js == null) {
            try {
                // needs full JVM (not Bck2Brwsr VM) to run
                Class<?> thread = Class.forName("java.lang.Thread");
            } catch (ClassNotFoundException ex) {
                return;
            }
            
            
            js = Models.bind(new JSONik(), Utils.newContext(OperationsTest.class));
            p = new Person("Sitar", "Gitar", Sex.MALE, null);
            js.applyBindings();

            js.setFetched(null);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    js.assignFetched(p);
                }
            }).start();
        }
    
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
    }
    

    
}
