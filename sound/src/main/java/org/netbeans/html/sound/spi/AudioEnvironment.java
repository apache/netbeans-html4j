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
package org.netbeans.html.sound.spi;

import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts;

/** Basic interface for sound playback providers. Register your implementation
 * in a way {@link java.util.ServiceLoader} can find it - e.g. use
 * {@link org.openide.util.lookup.ServiceProvider} annotation. Possibly
 * one can register the provider into {@link Contexts#newBuilder()}, in
 * case the implementation is somehow associated 
 * with the actual {@link BrwsrCtx} (works since version 0.8.3).
 *
 * @author antonepple
 * @param <Audio> custom type representing the internal audio state
 */
public interface AudioEnvironment<Audio> {
    /** Checks if the provided URL can be a supported audio stream 
     * and if so, it create an object to represent it. The created object
     * will be used in future callbacks to other methods of this interface
     * (like {@link #play(java.lang.Object)}).
     * @param src the URL pointing to the media stream
     * @return an internal representation object or <code>null</code> if this
     *   environment does not know how to handle such stream
     */
    public Audio create(String src);

    /** Starts playback of the audio.
     * 
     * @param a the internal representation of the audio as created by {@link #create(java.lang.String)} method.
     */
    public void play(Audio a);

    /** Pauses playback of the audio.
     * 
     * @param a the internal representation of the audio as created by {@link #create(java.lang.String)} method.
     */
    public void pause(Audio a);

    /** Changes volume for the playback of the audio.
     * 
     * @param a the internal representation of the audio as created by {@link #create(java.lang.String)} method.
     * @param volume value between 0.0 and 1.0
     */
    public void setVolume(Audio a, double volume);

    /** Checks whether given audio is supported
     * 
     * @param a the internal representation of the audio as created by {@link #create(java.lang.String)} method.
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isSupported(Audio a);
}
