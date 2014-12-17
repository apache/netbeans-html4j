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
package net.java.html.js.tests;

import java.io.StringReader;
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach
 */
public class JavaScriptBodyTest {
    @KOTest public void sumTwoNumbers() {
        int res = Bodies.sum(5, 3);
        assert res == 8 : "Expecting 8: " + res;
    }

    @KOTest public void sumFromCallback() {
        int res = Bodies.sumJS(5, 3);
        assert res == 8 : "Expecting 8: " + res;
    }
    
    @KOTest public void accessJsObject() {
        Object o = Bodies.instance(10);
        int ten = Bodies.readIntX(o);
        assert ten == 10 : "Expecting ten: " + ten;
    }

    @KOTest public void callWithNoReturnType() {
        Object o = Bodies.instance(10);
        Bodies.incrementX(o);
        int ten = Bodies.readIntX(o);
        assert ten == 11 : "Expecting eleven: " + ten;
    }
    
    @KOTest public void callbackToRunnable() {
        R run = new R();
        Bodies.callback(run);
        assert run.cnt == 1 : "Can call even private implementation classes: " + run.cnt;
    }
    
    private R asyncRun;
    @KOTest public void asyncCallbackToRunnable() throws InterruptedException {
        if (asyncRun == null) {
            asyncRun = new R();
            Bodies.asyncCallback(asyncRun);
        }
        if (asyncRun.cnt == 0) {
            throw new InterruptedException();
        }
        assert asyncRun.cnt == 1 : "Even async callback must arrive once: " + asyncRun.cnt;
    }

    @KOTest public void asyncCallbackFlushed() throws InterruptedException {
        R r = new R();
        for (int i = 0; i < 10; i++) {
            Bodies.asyncCallback(r);
        }
        int fourtyTwo = Bodies.sum(35, 7);
        assert r.cnt == 10 : "Ten calls: " + r.cnt;
        assert fourtyTwo == 42 : "Meaning of the world expected: " + fourtyTwo;
    }
    
    @KOTest public void typeOfCharacter() {
        String charType = Bodies.typeof('a', false);
        assert "number".equals(charType) : "Expecting number type: " + charType;
    }
    @KOTest public void typeOfBoolean() {
        String booleanType = Bodies.typeof(true, false);
        assert "boolean".equals(booleanType) : "Expecting boolean type: " + booleanType;
    }

    @KOTest public void typeOfPrimitiveBoolean() {
        String booleanType = Bodies.typeof(true);
        assert "boolean".equals(booleanType) || "number".equals(booleanType): 
            "Expecting boolean or at least number type: " + booleanType;
    }

    @KOTest public void typeOfInteger() {
        String intType = Bodies.typeof(1, false);
        assert "number".equals(intType) : "Expecting number type: " + intType;
    }

    @KOTest public void typeOfString() {
        String strType = Bodies.typeof("Ahoj", false);
        assert "string".equals(strType) : "Expecting string type: " + strType;
    }

    @KOTest public void typeOfDouble() {
        String doubleType = Bodies.typeof(0.33, false);
        assert "number".equals(doubleType) : "Expecting number type: " + doubleType;
    }
    
    @KOTest public void typeOfBooleanValueOf() {
        String booleanType = Bodies.typeof(true, true);
        assert "boolean".equals(booleanType) : "Expecting boolean type: " + booleanType;
    }

    @KOTest public void typeOfIntegerValueOf() {
        String intType = Bodies.typeof(1, true);
        assert "number".equals(intType) : "Expecting number type: " + intType;
    }

    @KOTest public void typeOfStringValueOf() {
        String strType = Bodies.typeof("Ahoj", true);
        assert "string".equals(strType) : "Expecting string type: " + strType;
    }

    @KOTest public void typeOfDoubleValueOf() {
        String doubleType = Bodies.typeof(0.33, true);
        assert "number".equals(doubleType) : "Expecting number type: " + doubleType;
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

    @KOTest public void encodingBackslashString() {
        Object p = "{\"firstName\":\"/*\\n * Copyright (c) 2013\",\"lastName\":null,\"sex\":\"MALE\",\"address\":{\"street\":null}}";
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
    
    @KOTest public void callbackWithParameters() throws InterruptedException {
        Sum s = new Sum();
        int res = Bodies.sumIndirect(s, 40, 2);
        assert res == 42 : "Expecting 42";
    }
    
    @KOTest public void selectFromStringJavaArray() {
        String[] arr = { "Ahoj", "Wo\nrld" };
        Object res = Bodies.select(arr, 1);
        assert "Wo\nrld".equals(res) : "Expecting World, but was: " + res;
    }

    @KOTest public void selectFromObjectJavaArray() {
        Object[] arr = { new Object(), new Object() };
        Object res = Bodies.select(arr, 1);
        assert arr[1].equals(res) : "Expecting " + arr[1] + ", but was: " + res;
    }

    @KOTest public void lengthOfJavaArray() {
        String[] arr = { "Ahoj", "World" };
        int res = Bodies.length(arr);
        assert res == 2 : "Expecting 2, but was: " + res;
    }

    @KOTest public void isJavaArray() {
        String[] arr = { "Ahoj", "World" };
        boolean is = Bodies.isArray(arr);
        assert is: "Expecting it to be an array: " + is;
    }

    @KOTest public void javaArrayInOutIsCopied() {
        String[] arr = { "Ahoj", "Wo\nrld" };
        Object res = Bodies.id(arr);
        assert res != null : "Non-null is returned";
        assert res instanceof Object[] : "Returned an array: " + res;
        assert !(res instanceof String[]) : "Not returned a string array: " + res;
        
        Object[] ret = (Object[]) res;
        assert arr.length == ret.length : "Same length: " + ret.length;
        assert arr[0].equals(ret[0]) : "Same first elem";
        assert arr[1].equals(ret[1]) : "Same 2nd elem";
    }

    @KOTest public void modifyJavaArrayHasNoEffect() {
        String[] arr = { "Ah\noj", "World" };
        String value = Bodies.modify(arr, 0, "H\tello");
        assert "H\tello".equals(value) : "Inside JS the value is changed: " + value;
        assert "Ah\noj".equals(arr[0]) : "From a Java point of view it remains: " + arr[0];
    }

    @KOTest
    public void callbackWithArray() {
        class A implements Callable<String[]> {
            @Override
            public String[] call() throws Exception {
                return new String[] { "He\nllo" };
            }
        }
        Callable<String[]> a = new A();
        Object b = Bodies.callbackAndPush(a, "Worl\nd!");
        assert b instanceof Object[] : "Returns an array: " + b;
        Object[] arr = (Object[]) b;
        String str = Arrays.toString(arr);
        assert arr.length == 2 : "Size is two " + str;
        assert "He\nllo".equals(arr[0]) : "Hello expected: " + arr[0];
        assert "Worl\nd!".equals(arr[1]) : "World! expected: " + arr[1];
    }
    
    @KOTest public void sumVector() {
        double[] arr = { 1.0, 2.0, 3.0 };
        double res = Bodies.sumVector(arr);
        assert 6.0 == res : "Expecting six: " + res;
    }

    @KOTest public void sumMatrix() {
        double[][] arr = { { 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0, 1.0 } };
        double res = Bodies.sumMatrix(arr);
        assert 6.0 == res : "Expecting six: " + res;
    }

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
    
    @KOTest public void sumArray() {
        int r = Bodies.sumArr(new Sum());
        assert r == 6 : "Sum is six: " + r;
    }
    
    @KOTest public void staticCallback() {
        int r = Bodies.staticCallback();
        assert r == 42 : "Expecting 42: " + r;
    }
    
    @KOTest public void asyncCallFromAJSCallbackNeedToFinishBeforeReturnToJS() {
        int r = Bodies.incAsync();
        assert r == 42 : "Expecting 42: " + r;
    }
    
    @KOTest public void iterateArray() {
        String[] arr = { "Ahoj", "Hi", "Ciao" };
        Object[] ret = Bodies.forIn(arr);
        assert ret.length == 3 : "Three elements returned: " + ret.length;
        assert ret != arr : "Different arrays";
        assert ret[0].equals("Ahoj") : "Expecting Ahoj: " + ret[0];
        assert ret[1].equals("Hi") : "Expecting Hi: " + ret[1];
        assert ret[2].equals("Ciao") : "Expecting Ciao: " + ret[2];
    }
    
    @KOTest public void primitiveTypes() {
        String all = Bodies.primitiveTypes(new Sum());
        assert "All:1 34567.08.0".equals(all) : "Valid return type: " + all;
    }
    
    @KOTest public void problematicString() {
        String orig = Bodies.problematicString();
        String js = Bodies.problematicCallback();
        if (orig.equals(js)) {
            return;
        }
        int len = Math.min(orig.length(), js.length());
        for (int i = 0; i < len; i++) {
            if (orig.charAt(i) != js.charAt(i)) {
                assert false : "Difference at position " + i + 
                    "\norig: " +
                    orig.substring(i - 5, Math.min(i + 10, orig.length())) +
                    "\n  js: " +
                    js.substring(i - 5, Math.min(i + 10, js.length()));
            }
        }
        assert false : "The JS string is different: " + js;
    }
    
    Later l;
    @KOTest public void callLater() throws Exception{
        final Fn.Presenter p = Fn.activePresenter();
        if (p == null) {
            return;
        }
        if (l == null) {
            p.loadScript(new StringReader(
                "if (typeof window === 'undefined') window = {};"
            ));
            l = new Later();
            l.register();
            p.loadScript(new StringReader(
                "window.later();"
            ));
        }
        if (l.call != 42) {
            throw new InterruptedException();
        }
        assert l.call == 42 : "Method was called: " + l.call;
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
