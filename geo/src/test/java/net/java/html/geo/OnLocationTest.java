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
package net.java.html.geo;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/** Testing correctness of the generated code.
 */
public class OnLocationTest {
    static int cnt;
    static @OnLocation void onLocation(Position p) {
        assertNotNull(p, "Position object provided");
        cnt++;
    }

    @Test public void createOneTimeQueryStatic() {
        net.java.html.geo.Position.Handle h = OnLocationHandle.createQuery();
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }
    
    @Test public void onLocationHandleCallback() throws Throwable {
        net.java.html.geo.Position.Handle h = OnLocationHandle.createQuery();
        cnt = 0;
        h.onLocation(new Position(0L, null));
        assertEquals(cnt, 1, "The callback has been made");
    }

    @Test public void createRepeatableWatchStatic() {
        net.java.html.geo.Position.Handle h = OnLocationHandle.createQuery();
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }

    int instCnt;
    Throwable instT;
    @OnLocation(onError = "someError") void instance(Position p) throws Error {
        assertNotNull(p, "Some position passed in");
        instCnt++;
    }
    void someError(Throwable t) throws Exception {
        instT = t;
        instCnt++;
    }
    
    @Test public void createOneTimeQueryInstance() {
        OnLocationTest t = new OnLocationTest();
        
        net.java.html.geo.Position.Handle h = InstanceHandle.createQuery(t);
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }
    
    @Test public void onInstanceCallback() throws Throwable {
        OnLocationTest t = new OnLocationTest();
        net.java.html.geo.Position.Handle h = InstanceHandle.createWatch(t);
        h.onLocation(new Position(0L, null));
        assertEquals(t.instCnt, 1, "One callback made");
    }

    @Test public void onInstanceError() throws Throwable {
        net.java.html.geo.Position.Handle h = InstanceHandle.createWatch(this);
        InterruptedException e = new InterruptedException();
        h.onError(e);
        assertEquals(instCnt, 1, "One callback made");
        assertEquals(instT, e, "The same exception passed in");
    }

    @Test public void createRepeatableWatch() {
        OnLocationTest t = new OnLocationTest();
        
        net.java.html.geo.Position.Handle h = InstanceHandle.createWatch(t);
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }
    
    @OnLocation(onError = "errParam") void withParam(Position pos, int param) {
        instCnt = param;
    }
    
    void errParam(Exception ex, int param) {
        instCnt = param;
    }
}
