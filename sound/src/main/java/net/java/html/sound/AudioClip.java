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
package net.java.html.sound;

import java.util.ServiceLoader;
import org.apidesign.html.sound.spi.AudioEnvironment;

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
     * 
     * @param src the URL where to find the audio clip
     * @return the audio clip handle
     * @throws NullPointerException if src is <code>null</code>
     */
    public static AudioClip create(String src) {
        src.getClass();
        for (AudioEnvironment<?> ae : ServiceLoader.load(AudioEnvironment.class)) {
            Impl handle = create(ae, src);
            if (handle != null) {
                return handle;
            }
        }
        throw new IllegalStateException();
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
}