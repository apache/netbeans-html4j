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
package org.netbeans.html.geo.spi;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class CoordImplTest extends GLProvider<Double, Object> {
    
    public CoordImplTest() {
    }
    @Test public void testGetLatitude() {
        CoordImpl<Double> c = new CoordImpl<Double>(50.5, this);
        assertEquals(c.getLatitude(), 50.5, 0.1, "Latitude returned as provided");
    }

    @Override
    protected Object start(Query c) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void stop(Object watch) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected double latitude(Double coords) {
        return coords;
    }

    @Override
    protected double longitude(Double coords) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected double accuracy(Double coords) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Double altitude(Double coords) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Double altitudeAccuracy(Double coords) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Double heading(Double coords) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Double speed(Double coords) {
        throw new UnsupportedOperationException();
    }
    
}
