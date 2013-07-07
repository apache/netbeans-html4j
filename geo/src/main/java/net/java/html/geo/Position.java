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

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Position {
    Position() {
    }

    public Coordinates getCoords() {
        return null;
    }
    
    public long getTimestamp() {
        return 0L;
    }
    
    public static final class Coordinates {
        private double latitude;
        private double longitude;
        private double accuracy;

        private Double altitude;
        private Double altitudeAccuracy;
        private Double heading;
        private Double speed;
    }

    /**
     *
     */
    public static abstract class Handle {

        private final boolean oneTime;
        private boolean enableHighAccuracy;
        private long timeout;
        private long maximumAge;

        protected Handle(boolean oneTime) {
            super();
            this.oneTime = oneTime;
        }

        protected abstract void onLocation(Position p) throws Throwable;

        protected abstract void onError(Throwable t) throws Throwable;

        public void setHighAccuracy(boolean enable) {
            this.enableHighAccuracy = enable;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public void setMaximumAge(long age) {
            this.maximumAge = age;
        }

        public void start() {
        }

        public void stop() {
        }
    }
}
