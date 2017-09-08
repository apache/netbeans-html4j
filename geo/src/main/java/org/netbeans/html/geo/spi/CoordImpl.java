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

import net.java.html.geo.Position;

/**
 *
 * @author Jaroslav Tulach
 */
final class CoordImpl<Coords> extends Position.Coordinates {
    private final Coords data;
    private final GLProvider<Coords, ?> provider;

    CoordImpl(Coords data, GLProvider<Coords, ?> p) {
        this.data = data;
        this.provider = p;
    }

    @Override public double getLatitude() {
        return provider.latitude(data);
    }

    @Override public double getLongitude() {
        return provider.longitude(data);
    }

    @Override public double getAccuracy() {
        return provider.accuracy(data);
    }

    @Override public Double getAltitude() {
        return provider.altitude(data);
    }

    @Override public Double getAltitudeAccuracy() {
        return provider.altitudeAccuracy(data);
    }

    @Override public Double getHeading() {
        return provider.heading(data);
    }

    @Override public Double getSpeed() {
        return provider.speed(data);
    }
} // end of CoordImpl
