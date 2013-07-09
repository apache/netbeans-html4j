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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apidesign.html.geo.impl.JsG;

/** Class that represents a geolocation position provided as a callback
 * to {@link Handle#onLocation(net.java.html.geo.Position)} method. The
 * class getters mimic closely the structure of the position object as
 * specified by <a href="http://www.w3.org/TR/2012/PR­geolocation­API­20120510/">
 * W3C's Geolocation API</a>.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Position {
    static final Logger LOG = Logger.getLogger(Position.class.getName());
    private final long timestamp;
    private final Coordinates coords;

    Position(Object position) {
        Object obj = JsG.get(position, "timestamp");
        timestamp = obj instanceof Number ? ((Number)obj).longValue() : 0L;
        coords = new Coordinates(JsG.get(position, "coords"));
    }
    
    /** The actual location of the position.
     * @return non-null coordinates
     */
    public Coordinates getCoords() {
        return coords;
    }
    
    /** The time when the position has been recorded.
     * @return time in milliseconds since era (e.g. Jan 1, 1970).
     */
    public long getTimestamp() {
        return timestamp;
    }

    /** Actual location of a {@link Position}. 
     *  Mimics closely <a href="http://www.w3.org/TR/2012/PR­geolocation­API­20120510/">
     * W3C's Geolocation API</a>.
     */
    public static final class Coordinates {
        private final Object data;

        Coordinates(Object data) {
            this.data = data;
        }
        
        public double getLatitude() {
            return ((Number)JsG.get(data, "latitude")).doubleValue(); // NOI18N
        }

        public double getLongitude() {
            return ((Number)JsG.get(data, "longitude")).doubleValue(); // NOI18N
        }

        public double getAccuracy() {
            return ((Number)JsG.get(data, "accuracy")).doubleValue(); // NOI18N
        }
        
        public Double getAltitude() {
            return (Double)JsG.get(data, "altitude"); // NOI18N
        }
        
        public Double getAltitudeAccuracy() {
            return (Double)JsG.get(data, "altitudeAccuracy"); // NOI18N
        }
        
        public Double getHeading() {
            return (Double)JsG.get(data, "heading"); // NOI18N
        }
        
        public Double getSpeed() {
            return (Double)JsG.get(data, "speed"); // NOI18N
        }
    } // end of Coordinates

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
        volatile JsH handle;

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
        public final void setHighAccuracy(boolean enable) {
            this.enableHighAccuracy = enable;
        }

        /** The amount of milliseconds to wait for a result.
         * By default infinity.
         * @param timeout time in milliseconds to wait for a result.
         */
        public final void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        /** Sets maximum age of cached results which are acceptable to be
         * returned. By default maximum age is set to zero.
         * @param age time in milliseconds of acceptable cached results
         */
        public final void setMaximumAge(long age) {
            this.maximumAge = age;
        }

        /** Initializes the <em>query</em> or <em>watch</em> request(s) and
         * returns immediately. Has no effect if the query has already been
         * started.
         */
        public final void start() {
            if (handle != null) {
                return;
            }
            handle = new JsH();
            handle.start();
        }

        /** Stops all pending requests. After this call no further callbacks
         * can be obtained. Does nothing if no query or watch was in progress.
         */
        public final void stop() {
            JsH h = handle;
            if (h == null) {
                return;
            }
            handle = null;
            h.stop();
        }

        private final class JsH extends JsG {
            long watch;
            
            @Override
            public void onLocation(Object position) {
                if (handle != this) {
                    return;
                }
                if (oneTime) {
                    stop();
                }
                try {
                    Handle.this.onLocation(new Position(position));
                } catch (Throwable ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onError(Object error) {
                if (handle != this) {
                    return;
                }
                if (oneTime) {
                    stop();
                }
                try {
                    Handle.this.onError(new Exception());
                } catch (Throwable ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            final void start() {
                watch = start(oneTime, enableHighAccuracy, timeout, maximumAge);
            }

            protected final void stop() {
                super.stop(watch);
            }
        }
    }
}
