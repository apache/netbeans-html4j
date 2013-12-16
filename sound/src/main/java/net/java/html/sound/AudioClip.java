/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
     * If no suitable audio environment provider is found, the method 
     * returns a dummy instance that does nothing and only returns
     * false from its {@link #isSupported()} method.
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
        return DummyClip.INSTANCE;
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