/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
