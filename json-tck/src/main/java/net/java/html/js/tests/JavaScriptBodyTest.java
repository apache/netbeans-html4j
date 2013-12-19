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

import java.util.concurrent.Callable;
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

    @KOTest public void callWithNoReturnType() {
        Object o = Bodies.instance(10);
        Bodies.incrementX(o);
        int ten = Bodies.readX(o);
        assert ten == 11 : "Expecting eleven: " + ten;
    }
    
    @KOTest public void callbackToRunnable() {
        R run = new R();
        Bodies.callback(run);
        assert run.cnt == 1 : "Can call even private implementation classes: " + run.cnt;
    }

    @KOTest public void computeInARunnable() {
        final int[] sum = new int[2];
        class First implements Runnable {
            @Override public void run() {
                sum[0] = Bodies.sum(22, 20);
                sum[1] = Bodies.sum(32, 10);
            }
        }
        Bodies.callback(new First());
        assert sum[0] == 42 : "Computed OK " + sum[0];
        assert sum[1] == 42 : "Computed OK too: " + sum[1];
    }
    
    @KOTest public void doubleCallbackToRunnable() {
        final R run = new R();
        final R r2 = new R();
        class First implements Runnable {
            @Override public void run() {
                Bodies.callback(run);
                Bodies.callback(r2);
            }
        }
        Bodies.callback(new First());
        assert run.cnt == 1 : "Can call even private implementation classes: " + run.cnt;
        assert r2.cnt == 1 : "Can call even private implementation classes: " + r2.cnt;
    }
    
    @KOTest public void identity() {
        Object p = new Object();
        Object r = Bodies.id(p);
        assert r == p : "The object is the same";
    }

    @KOTest public void encodingString() {
        Object p = "Ji\n\"Hi\"\nHon";
        Object r = Bodies.id(p);
        assert p.equals(r) : "The object is the same: " + p + " != " + r;
    }

    @KOTest public void nullIsNull() {
        Object p = null;
        Object r = Bodies.id(p);
        assert r == p : "The null is the same";
    }
    
    @KOTest public void callbackWithResult() {
        Callable<Boolean> c = new C();
        Object b = Bodies.callback(c);
        assert b == Boolean.TRUE : "Should return true";
    }
    
    @KOTest public void callbackWithParameters() {
        int res = Bodies.sumIndirect(new Sum());
        assert res == 42 : "Expecting 42";
    }
    
    @KOTest public void selectFromJavaArray() {
        String[] arr = { "Ahoj", "World" };
        Object res = Bodies.select(arr, 1);
        assert "World".equals(res) : "Expecting World, but was: " + res;
    }

    @KOTest public void lengthOfJavaArray() {
        String[] arr = { "Ahoj", "World" };
        int res = Bodies.length(arr);
        assert res == 2 : "Expecting 2, but was: " + res;
    }

    @KOTest public void javaArrayInOut() {
        String[] arr = { "Ahoj", "World" };
        Object res = Bodies.id(arr);
        assert res == arr : "Expecting same array, but was: " + res;
    }

//  Modifying an array is a complex operation in the bridge:    
//    
//    @KOTest public void modifyJavaArray() {
//        String[] arr = { "Ahoj", "World" };
//        Bodies.modify(arr, 0, "Hello");
//        assert "Hello".equals(arr[0]) : "Expecting World, but was: " + arr[0];
//    }

    @KOTest public void truth() {
        assert Bodies.truth() : "True is true";
    }
    
    @KOTest public void factorial2() {
        assert new Factorial().factorial(2) == 2;
    }
    
    @KOTest public void factorial3() {
        assert new Factorial().factorial(3) == 6;
    }
    
    @KOTest public void factorial4() {
        assert new Factorial().factorial(4) == 24;
    }
    
    @KOTest public void factorial5() {
        assert new Factorial().factorial(5) == 120;
    }
    
    @KOTest public void factorial6() {
        assert new Factorial().factorial(6) == 720;
    }
    
    private static class R implements Runnable {
        int cnt;
        private final Thread initThread;
        
        public R() {
            initThread = Thread.currentThread();
        }

        @Override
        public void run() {
            assert initThread == Thread.currentThread() : "Expecting to run in " + initThread + " but running in " + Thread.currentThread();
            cnt++;
        }
    }
    
    private static class C implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            return Boolean.TRUE;
        }
    }
}
