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
package net.java.html.geo;

import java.util.Collections;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.geo.impl.Accessor;
import org.netbeans.html.geo.impl.JsGLProvider;
import org.netbeans.html.geo.spi.GLProvider;

/** Class that represents a geolocation position provided as a callback
 * to {@link Handle#onLocation(net.java.html.geo.Position)} method. The
 * class getters mimic closely the structure of the position object as
 * specified by <a href="http://www.w3.org/TR/geolocation-API/">
 * W3C's Geolocation API</a>.
 *
 * @author Jaroslav Tulach
 */
public final class Position {
    static final Logger LOG = Logger.getLogger(Position.class.getName());
    private final long timestamp;
    private final Coordinates coords;

    public Position(long timestamp, Coordinates coords) {
        this.timestamp = timestamp;
        this.coords = coords;
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
     *  Mimics closely <a href="http://www.w3.org/TR/geolocation-API/">
     * W3C's Geolocation API</a>.
     */
    public static abstract class Coordinates {
        protected Coordinates() {
            if (!getClass().getName().equals("org.netbeans.html.geo.spi.CoordImpl")) {
                throw new IllegalStateException();
            }
        }
        
        /**
         * @return geographic coordinate specified in decimal degrees.
         */
        public abstract double getLatitude();

        /**
         * @return geographic coordinate specified in decimal degrees.
         */
        public abstract double getLongitude();

        /**
         * The accuracy attribute denotes the accuracy level of the latitude 
         * and longitude coordinates. It is specified in meters. 
         * The value of the accuracy attribute must be a non-negative number.
         * 
         * @return accuracy in meters
         */
        public abstract double getAccuracy();
        
        /** Denotes the height of the position, specified in meters above the ellipsoid. 
         * If the implementation cannot provide altitude information, 
         * the value of this attribute must be null.
         * @return value in meters, may return null, if the information is not available 
         */
        public abstract Double getAltitude();
        
        /**  The altitude accuracy is specified in meters. 
         * If the implementation cannot provide altitude information, 
         * the value of this attribute must be null. Otherwise, the value 
         * must be a non-negative real number.
         * @return value in meters; may return null, if the information is not available 
         */
        public abstract Double getAltitudeAccuracy();
        
        /** Denotes the direction of travel of the device and 
         * is specified in degrees 
         * counting clockwise relative to the true north. 
         * 
         * @return value from 0 to 360 - may return <code>null</code>, 
         *   if the information is not available 
         */
        public abstract Double getHeading();
        
        /** Denotes the magnitude of the horizontal component of the 
         * device's current velocity and is specified in meters per second.
         * 
         * @return may return null, if the information is not available 
         */
        public abstract Double getSpeed();
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
        volatile JsH<?> handle;

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
        
        /** Check whether the location API is supported.
         * @return true, if one can call {@link #start}.
         */
        public final boolean isSupported() {
            JsH<?> p = seekProviders(null, null);
            if (p != null) {
                p.stop();
                return true;
            }
            return false;
        }

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
         * started. If a problem appears while starting the system,
         * it is immediately reported via the {@link #onError(java.lang.Exception)}
         * callback. For example, if the {@link #isSupported()} method
         * returns <code>false</code> an IllegalStateException is created
         * and sent to the {@link #onError(java.lang.Exception) callback} method.
         */
        public final void start() {
            if (handle != null) {
                return;
            }
            
            Exception[] problem = { null };
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            JsH<?> h = seekProviders(sb, problem);
            sb.append("\n]");
            try {
                if (problem[0] != null) {
                    onError(problem[0]);
                    return;
                }
                if (h == null) {
                    onError(new IllegalStateException("geolocation API not supported. Among providers: " + sb));
                }
                synchronized (this) {
                    if (handle != null) {
                        onError(new IllegalStateException("Parallel request"));
                    }
                    handle = h;
                }
            } catch (Throwable thr) {
                LOG.log(Level.INFO, "Problems delivering onError report", thr);
            }
        }

        private JsH<?> seekProviders(StringBuilder sb, Exception[] problem) {
            BrwsrCtx ctx = BrwsrCtx.findDefault(getClass());
            JsH<?> h = seekProviders(Contexts.find(ctx, GLProvider.class), null, sb, problem);
            if (h == null) {
                h = seekProviders(null, ServiceLoader.load(GLProvider.class), sb, problem);
            }
            if (h == null) {
                h = seekProviders(new JsGLProvider(), null, sb, problem);
            }
            return h;
        }

        private JsH<?> seekProviders(
            GLProvider single, Iterable<GLProvider> set,
            StringBuilder sb, Exception[] problem
        ) {
            if (set == null) {
                if (single == null) {
                    return null;
                }
                set = Collections.singleton(single);
            }
            JsH<?> h = null;
            for (GLProvider<?,?> p : set) {
                if (sb != null) {
                    if (sb.length() > 1) {
                        sb.append(',');
                    }
                    sb.append("\n  ").append(p.getClass().getName());
                }
                try {
                    h = createHandle(p);
                } catch (Exception ex) {
                    LOG.log(Level.INFO, "Problems when starting " + p.getClass().getName(), ex);
                    if (problem != null && problem[0] == null) {
                        problem[0] = ex;
                    }
                }
                if (h != null) {
                    break;
                }
            }
            return h;
        }

        /** Stops all pending requests. After this call no further callbacks
         * can be obtained. Does nothing if no query or watch was in progress.
         */
        public final void stop() {
            JsH h;
            synchronized (this) {
                h = handle;
                if (h == null) {
                    return;
                }
                handle = null;
            }
            h.stop();
        }
        
        private <Watch> JsH<Watch> createHandle(GLProvider<?,Watch> p) {
            JsH<Watch> temp = new JsH<Watch>(p);
            return temp.watch == null ? null : temp;
        }

        private final class JsH<Watch> extends Accessor {
            private final Watch watch;
            private final GLProvider<?, Watch> provider;
            
            public JsH(GLProvider<?, Watch> p) {
                super(true);
                this.watch = Accessor.SPI.start(p, this, oneTime, enableHighAccuracy, timeout, maximumAge);
                this.provider = p;
            }
            
            @Override
            public void onLocation(Position position) {
                if (handle != this) {
                    return;
                }
                if (oneTime) {
                    stop();
                }
                try {
                    Handle.this.onLocation(position);
                } catch (Throwable ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onError(Exception err) {
                if (handle != this) {
                    return;
                }
                if (oneTime) {
                    stop();
                }
                try {
                    Handle.this.onError(err);
                } catch (Throwable ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            protected final void stop() {
                Accessor.SPI.stop(provider, watch);
            }

            @Override
            public <Watch> Watch start(
                GLProvider<?, Watch> p, Accessor peer,
                boolean oneTime, boolean enableHighAccuracy,
                long timeout, long maximumAge
            ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <Watch> void stop(GLProvider<?, Watch> p, Watch w) {
                throw new UnsupportedOperationException();
            }

        } // end of JsH
    }
}
