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

    /** Rather than subclassing this class directly consider using {@link OnLocation}
     * annotation. Such annotation will generate a subclass for you automatically
     * with two static methods <code>createQuery</code> and <code>createWatch</code>
     * which can be used to obtain instance of this class.
     */
    public static abstract class Handle {

        private final boolean oneTime;
        private boolean enableHighAccuracy;
        private long timeout;
        private long maximumAge;

        /** Creates new instance of this handle.
         * 
         * @param oneTime <code>true</code> if the handle represents one time 
         *   <em>query</em>. <code>false</code> if it represents a <em>watch</em>
         */
        protected Handle(boolean oneTime) {
            super();
            this.oneTime = oneTime;
        }

        /** Callback from the implementation when a (new) position has been
         * received and identified
         * @param p the position
         * @throws Throwable if an exception is thrown, it will be logged by the system
         */
        protected abstract void onLocation(Position p) throws Throwable;

        /** Callback when an error occurs.
         * @param ex the exception describing what went wrong
         * @throws Throwable if an exception is thrown, it will be logged by the system
         */
        protected abstract void onError(Exception ex) throws Throwable;

        /** Turns on high accuracy mode as specified by the 
         * <a href="http://www.w3.org/TR/2012/PR­geolocation­API­20120510/">
         * W3C's Geolocation API</a>. By default the mode is disabled.
         * @param enable <code>true</code> or <code>false</code>
         */
        public void setHighAccuracy(boolean enable) {
            this.enableHighAccuracy = enable;
        }

        /** The amount of milliseconds to wait for a result.
         * By default infinity.
         * @param timeout time in milliseconds to wait for a result.
         */
        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        /** Sets maximum age of cached results which are acceptable to be
         * returned. By default maximum age is set to zero.
         * @param age time in milliseconds of acceptable cached results
         */
        public void setMaximumAge(long age) {
            this.maximumAge = age;
        }

        /** Initializes the <em>query</em> or <em>watch</em> request(s) and
         * returns immediately.
         */
        public void start() {
        }

        /** Stops all pending requests. After this call no further callbacks
         * can be obtained.
         */
        public void stop() {
        }
    }
}
