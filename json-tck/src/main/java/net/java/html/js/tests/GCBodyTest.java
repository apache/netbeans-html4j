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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.html.json.tck.KOTest;
import static net.java.html.js.tests.JavaScriptBodyTest.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class GCBodyTest {
    Reference<?> ref;
    int[] arr;
    
    @KOTest public void callbackInterfaceCanDisappear() throws InterruptedException {
        if (ref != null) {
            assertGC(ref, "Can disappear!");
            return;
        }
        Sum s = new Sum();
        int res = Bodies.sumIndirect(s, 22, 20);
        assertEquals(res, 42, "Expecting 42");
        Reference<?> ref = new WeakReference<Object>(s);
        s = null;
        assertGC(ref, "Can disappear!");
    }
    
    private Object assignInst() {
        Object obj = Bodies.instance(0);
        Object s = new EmptyInstance();
        Bodies.setX(obj, s);
        assertEquals(s, Bodies.readX(obj));
        ref = new WeakReference<Object>(s);
        return obj;
}
    
    @KOTest public void holdObjectAndReleaseObject() throws InterruptedException {
        if (ref != null) {
            assertGC(ref, "Can disappear!");
            return;
        }
        
        Object obj = assignInst();
        assertNotNull(ref, "Reference assigned");
        
        assertGC(ref, "Can disappear as it is keepAlive false!");
        assertNotNull(obj, "Object is still present");
    }

    @KOTest public void strongReceiverBehavior() {
        Object v = new EmptyInstance();
        Receiver r = new Receiver(v);
        r.apply();
        assertEquals(v, r.value, "Value is as expected");
    }
    
    @KOTest public void gcReceiverBehavior() throws InterruptedException {
        Receiver r = new Receiver(new EmptyInstance());
        assertGC(r.ref, "The empty instance can be GCed even when referenced from JS");
        r.apply();
        assertEquals(r.value, null, "Setter called with null value");
    }

    private static Reference<?> sendRunnable(final int[] arr) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                arr[0]++;
            }
        };
        Bodies.asyncCallback(r);
        return new WeakReference<Object>(r);
    }
    
    private static class EmptyInstance {
    }
    
    @KOTest public void parametersNeedToRemainInAsyncMode() throws InterruptedException {
        if (ref != null) {
            if (arr[0] != 1) {
                throw new InterruptedException();
            }
            assertGC(ref, "Now the runnable can disappear");
            return;
        }
        arr = new int[] { 0 };
        ref = sendRunnable(arr);
        if (arr[0] == 1) {
            return;
        }
        assertNotGC(ref, false, "The runnable should not be GCed");
        throw new InterruptedException();
    }
    
    private static void assertGC(Reference<?> ref, String msg) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (isGone(ref)) return;
            long then = System.currentTimeMillis();
            int size = Bodies.gc(Math.pow(2.0, i));
            long took = System.currentTimeMillis() - then;
            if (took > 3000) {
                throw new InterruptedException(msg + " - giving up after " + took + " ms at size of " + size);
            }
            
            try {
                System.gc();
                System.runFinalization();
            } catch (Error err) {
                err.printStackTrace();
            }
        }
        throw new InterruptedException(msg);
    }

    private static boolean isGone(Reference<?> ref) {
        return ref.get() == null;
    }
    
    private static void assertNotGC(Reference<?> ref, boolean clearJS, String msg) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            if (ref.get() == null) {
                throw new IllegalStateException(msg);
            }
            if (clearJS) {
                Bodies.gc(Math.pow(2.0, i));
            }
            try {
                System.gc();
                System.runFinalization();
            } catch (Error err) {
                err.printStackTrace();
            }
        }
    }
}
