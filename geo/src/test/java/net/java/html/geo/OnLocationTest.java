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
        h.onLocation(new Position(null));
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
        h.onLocation(new Position(null));
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
}
