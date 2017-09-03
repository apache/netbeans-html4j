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
package org.netbeans.html.geo.spi;

import net.java.html.BrwsrCtx;
import net.java.html.geo.Position;
import net.java.html.geo.Position.Handle;
import net.java.html.geo.Position.Coordinates;
import org.netbeans.html.context.spi.Contexts.Builder;
import org.netbeans.html.geo.impl.Accessor;
import org.openide.util.lookup.ServiceProvider;

/** SPI for those who wish to provide their own way of obtaining geolocation.
 * Subclass this class, implement its method and register it into the system.
 * You can either use {@link ServiceProvider} to register globally, or 
 * one can register into {@link BrwsrCtx} (via 
 * {@link Builder#register(java.lang.Class, java.lang.Object, int) context builder}).
 * <p>
 * There is default system provider (used as a fallback) based on 
 * <a href="http://www.w3.org/TR/geolocation-API/">
 * W3C's Geolocation</a> specification - if you are running inside a
 * browser that supports such standard and you are satisfied with its
 * behavior, you don't have to register anything.
 * <p>
 * The provider serves two purposes: 
 * <ol>
 *   <li>
 *     It handles a geolocation request and creates a "watch" to represent it -
 *     to do so implement the {@link #start(org.netbeans.html.geo.spi.GLProvider.Query) start} 
 *     method and the {@link #stop(java.lang.Object) stop} method.
 *   </li>
 *   <li>
 *     Once the location is found, the provider needs to 
 *     {@link #callback(org.netbeans.html.geo.spi.GLProvider.Query, long, java.lang.Object, java.lang.Exception)  call back}
 *     with appropriate location information which can be extracted
 *     later via {@link #latitude(java.lang.Object)} {@link #longitude(java.lang.Object)}, and
 *     other methods in this that also need to be implemented.
 *   </li>
 * </ol>
 * <p>
 * The provider is based on a 
 * <a href="http://wiki.apidesign.org/wiki/Singletonizer" target="_blank">singletonizer</a> 
 * pattern (applied twice)
 * and as such one is only required to subclass just the {@link GLProvider} 
 * and otherwise has freedom choosing what classes to use
 * to represent coordinates and watches. For example if it is enough to use
 * an array for coordinates and a long number for a watch, one can do:
 * <pre>
 * <b>public final class</b> MyGeoProvider extends {@link GLProvider}&lt;Double[], Long&gt; {
 *   <em>// somehow implement the methods</em>
 * }
 * </pre>
 *
 * @author Jaroslav Tulach
 * @param <Watch> your chosen type to represent one query (one time) or watch (repeated) request -
 *   this type is used in {@link #start(org.netbeans.html.geo.spi.GLProvider.Query) start}
 *   and {@link #stop(java.lang.Object) stop} methods.
 * 
 * @param <Coords> your chosen type to represent geolocation coordinates -
 *   use in many methods in this class like {@link #latitude(java.lang.Object)} and
 *   {@link #longitude(java.lang.Object)}.
 * 
 * @since 1.0
 */
public abstract class GLProvider<Coords,Watch> {
    static {
        Accessor initChannel = new Accessor(false) {
            @Override
            public <Watch> Watch start(GLProvider<?, Watch> p, Accessor peer, boolean oneTime, boolean enableHighAccuracy, long timeout, long maximumAge) {
                return p.start(new Query(peer, oneTime, enableHighAccuracy, timeout, maximumAge));
            }
            
            @Override
            public <Watch> void stop(GLProvider<?, Watch> p, Watch w) {
                p.stop(w);
            }
            
            @Override
            public void onError(Exception ex) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public void onLocation(Position position) {
                throw new UnsupportedOperationException();
            }
        };
    }
    /** Start obtaining geolocation.
     * When the client {@link Handle#start() requests location} (and
     * your provider is found) this method should initialize the request or 
     * return <code>null</code> to give chance to another provider.
     * 
     * @param c the query describing the request and
     *   to {@link #callback(org.netbeans.html.geo.spi.GLProvider.Query, long, java.lang.Object, java.lang.Exception)  use when location is found} -
     *    keep it, you'll need it later
     * 
     * @return an object representing the request (so it can be {@link #stop(java.lang.Object) stopped} later)
     *   or <code>null</code> if this provider was unable to start the request
     */
    protected abstract Watch start(Query c);
    
    /** Called when a geolocation request should be stopped.
     * 
     * @param watch the watch returned when {@link #start(org.netbeans.html.geo.spi.GLProvider.Query) starting}
     *   the request
     */
    protected abstract void stop(Watch watch);

    /** Invoke this method when your provider obtained requested location.
     * This single method is used for notification of success (when <code>ex</code>
     * argument is <code>null</code> and <code>position</code> is provided) or 
     * a failure (when <code>ex</code> argument is non-<code>null</code>).
     * A successful requests leads in a call to {@link Handle#onLocation(net.java.html.geo.Position)}
     * while an error report leads to a call to {@link Handle#onError(java.lang.Exception)}.
     * The actual call is sent to {@link BrwsrCtx#execute(java.lang.Runnable)} of
     * context recorded when the {@link Query} was created to guarantee it
     * happens on the right browser thread - however it may happen "later"
     * when this method has already finished.
     * 
     * @param c the query as provided when {@link #start(org.netbeans.html.geo.spi.GLProvider.Query) starting}
     *   the request
     * @param timestamp milliseconds since epoch when the location has been obtained
     * @param position your own, internal, representation of geolocation
     *   coordinates - will be passed back to other methods of this class
     *   like {@link #latitude(java.lang.Object)} and {@link #longitude(java.lang.Object)}.
     *   Can be <code>null</code> if <code>ex</code> is non-<code>null</code>
     * @param ex an exception to signal an error - should be <code>null</code>
     *   when one notifies the successfully obtained <code>position</code>
     */
    protected final void callback(
        final Query c,
        final long timestamp, final Coords position,
        final Exception ex
    ) {
        c.ctx.execute(new Runnable() {
            @Override
            public void run() {
                if (ex == null) {
                    c.peer.onLocation(new Position(timestamp, new CoordImpl<Coords>(position, GLProvider.this)));
                } else {
                    c.peer.onError(ex);
                }
            }
        });
    }

    /** Extracts value for {@link Coordinates#getLatitude()}.
     * @param coords your own internal representation of coordinates.
     * @return geographic coordinate specified in decimal degrees.
     */
    protected abstract double latitude(Coords coords);
    
    /** Extracts value for {@link Coordinates#getLatitude()}.
     * @param coords your own internal representation of coordinates.
     * @return geographic coordinate specified in decimal degrees.
     */
    protected abstract double longitude(Coords coords);
    
    /** Extracts value for {@link Coordinates#getLatitude()}.
     * The accuracy attribute denotes the accuracy level of the latitude 
     * and longitude coordinates.
     * 
     * @param coords your own internal representation of coordinates.
     * @return accuracy in meters
     */
    protected abstract double accuracy(Coords coords);
    
    /** Extracts value for {@link Coordinates#getAltitude()}.
     * Denotes the height of the position, specified in meters above the ellipsoid.
     * 
     * @param coords your own internal representation of coordinates.
     * @return value in meters, may return null, if the information is not available
     */
    protected abstract Double altitude(Coords coords);
    
    /** Extracts value for {@link Coordinates#getAltitudeAccuracy()} -
     * the altitude accuracy is specified in meters. 
     * 
     * @param coords your own internal representation of coordinates.
     * @return value in meters; may return null, if the information is not available
     */
    protected abstract Double altitudeAccuracy(Coords coords);
    
    /** Extracts value for {@link Coordinates#getHeading()}.
     * Denotes the magnitude of the horizontal component of the 
     * device's current velocity and is specified in meters per second.
     * 
     * @param coords your own internal representation of coordinates.
     * @return may return null, if the information is not available 
     */
    protected abstract Double heading(Coords coords);
    
    /** Extracts value for {@link Coordinates#getSpeed()}.
     * Denotes the magnitude of the horizontal component of the 
     * device's current velocity and is specified in meters per second.
     * 
     * @param coords your own internal representation of coordinates.
     * @return may return null, if the information is not available
     */
    protected abstract Double speed(Coords coords);
    
    /** Holds parameters describing the location query and is used by {@link GLProvider} to notify back
     * results of its findings.
     */
    public static final class Query {
        private final boolean oneTime;
        private final boolean enableHighAccuracy;
        private final long timeout;
        private final long maximumAge;
        private final BrwsrCtx ctx;
        final Accessor peer;

        Query(Accessor peer, boolean oneTime, boolean enableHighAccuracy, long timeout, long maximumAge) {
            this.peer = peer;
            this.oneTime = oneTime;
            this.enableHighAccuracy = enableHighAccuracy;
            this.timeout = timeout;
            this.maximumAge = maximumAge;
            ctx = BrwsrCtx.findDefault(Query.class);
        }
        
        /**
         * Is this one time or repeated request? Mimics value provided in
         * {@link Handle constructor}.
         *
         * @return true if this is one time request, false if the request is
         * permanent (up until {@link Handle#stop() } is called).
         */
        public final boolean isOneTime() {
            return oneTime;
        }

        /**
         * Turns on high accuracy mode as specified by the
         * <a href="http://www.w3.org/TR/2012/PR­geolocation­API­20120510/">
         * W3C's Geolocation API</a>. By default the mode is disabled. Mimics
         * value of {@link Handle#setHighAccuracy(boolean)}.
         *
         * @return enable <code>true</code> or <code>false</code>
         */
        public final boolean isHighAccuracy() {
            return this.enableHighAccuracy;
        }

        /**
         * The amount of milliseconds to wait for a result. Mimics value of
         * {@link Handle#setTimeout(long)}.
         *
         * @return time in milliseconds to wait for a result.
         */
        public final long getTimeout() {
            return this.timeout;
        }

        /**
         * Sets maximum age of cached results which are acceptable to be
         * returned. Mimics value of {@link Handle#setMaximumAge(long)}.
         *
         * @return time in milliseconds of acceptable cached results
         */
        public final long getMaximumAge() {
            return this.maximumAge;
        }
    }
}
