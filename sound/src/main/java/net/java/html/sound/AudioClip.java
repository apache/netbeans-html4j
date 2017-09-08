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
package net.java.html.sound;

import java.util.ServiceLoader;
import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.sound.spi.AudioEnvironment;
import org.netbeans.html.sound.impl.BrowserAudioEnv;

/** Handle to an audio clip which can be {@link #play() played}, {@link #pause() paused}
 * and etc. Obtain new instance via {@link #create(java.lang.String) create} factory 
 * method and then use it when necessary.
 *
 * @author antonepple
 */
public abstract class AudioClip {
    private AudioClip() {
    }

    /** Creates new instance of an audio clip based on the provided URL.
     * If no suitable audio environment provider is found, the method 
     * returns a dummy instance that does nothing and only returns
     * false from its {@link #isSupported()} method.
     * <p>
     * The <code>src</code> can be absolute URL or it can be relative
     * to current {@link BrwsrCtx browser context} - e.g. usually to the
     * page that is just being displayed.
     * 
     * @param src the URL where to find the audio clip
     * @return the audio clip handle
     * @throws NullPointerException if src is <code>null</code>
     */
    public static AudioClip create(String src) {
        src.getClass();
        BrwsrCtx brwsrCtx = BrwsrCtx.findDefault(AudioClip.class);
        AudioEnvironment brwsrAE = Contexts.find(brwsrCtx, AudioEnvironment.class);
        if (brwsrAE != null) {
            Impl handle = create(brwsrAE, src);
            if (handle != null) {
                return handle;
            }
        }
        for (AudioEnvironment<?> ae : ServiceLoader.load(AudioEnvironment.class)) {
            Impl handle = create(ae, src);
            if (handle != null) {
                return handle;
            }
        }
        Impl handle = create(BrowserAudioEnv.DEFAULT, src);
        return handle != null ? handle : DummyClip.INSTANCE;
    }
    
    /** Plays the clip from begining to the end.
     */
    public abstract void play();

    /** Pauses playback of the clip
     */
    public abstract void pause();

    /**
     * Specifies the volume of the audio. Must be a number between 0.0 and 1.0:
     * <ul>
     *   <li>1.0 - highest volume</li>
     *   <li>0.5 - 50% volume</li>
     *   <li>0.0 - silent</li>
     * </ul>
     * 
     * @param volume for the playback
     */
    public abstract void setVolume(double volume);
    
    /** Check whether the audio clip is supported and can be played.
     * @return true if it is likely that after calling {@link #play()} 
     *   a sound will be produced
     */
    public abstract boolean isSupported();

//    public abstract void playFrom(int seconds);

    //
    // Implementation
    //
    
    private static <Audio> Impl<Audio> create(AudioEnvironment<Audio> env, String src) {
        Audio a = env.create(src);
        if (a != null) {
            return new Impl<Audio>(env, src, a);
        } else {
            return null;
        }
    }
    
    private static final class Impl<Audio> extends AudioClip {
        private final String src;
        private final Audio clip;
        private final AudioEnvironment<Audio> env;

        public Impl(AudioEnvironment<Audio> env, String src, Audio clip) {
            this.clip = clip;
            this.env = env;
            this.src = src;
        }

        @Override
        public void play() {
            env.play(clip);
        }

        @Override
        public void pause() {
            env.pause(clip);
        }

        @Override
        public void setVolume(double volume) {
            env.setVolume(clip, volume);
        }

        @Override
        public boolean isSupported() {
            return env.isSupported(clip);
        }

        @Override
        public int hashCode() {
            return 59 * src.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Impl) {
                return src.equals(((Impl)obj).src);
            }
            return false;
        }
    } // end of Impl
    
    private static final class DummyClip extends AudioClip {
        static AudioClip INSTANCE = new DummyClip();
        
        @Override
        public void play() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void setVolume(double volume) {
        }

        @Override
        public boolean isSupported() {
            return false;
        }
    } // end of DummyClip
}