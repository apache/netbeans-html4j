/**
 * Back 2 Browser Bytecode Translator Copyright (C) 2012 Jaroslav Tulach
 * <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. Look for COPYING file in the top folder. If not, see
 * http://opensource.org/licenses/GPL-2.0.
 */
package net.java.html.sound.api;

import java.util.ServiceLoader;
import net.java.html.sound.spi.AudioEnvironment;

/**
 *
 * @author antonepple
 */
public final class AudioClip {

    private Object cached;
    private int cacheHash;
    private final String src;
    private final AudioEnvironment audioEnvironment;

    private AudioClip(String src) {
        this.src = src;
        ServiceLoader<AudioEnvironment> loader = ServiceLoader.load(AudioEnvironment.class);
        audioEnvironment = loader.iterator().next();
    }

    public static AudioClip create(String src){
        return new AudioClip(src);
    }
    
    public void play() {
        Object nativeClip = audioEnvironment.play(this, cached);
        cache(nativeClip);
    }

    public void pause() {
        Object nativeClip = audioEnvironment.pause(this, cached);
        cache(nativeClip);
    }

    public void stop() {
        Object nativeClip = audioEnvironment.stop(this, cached);
        cache(nativeClip);
    }
    
    public void setVolume(int volume) {
        Object nativeClip = audioEnvironment.setVolume(this, volume, cached);
        cache(nativeClip);
    }
    
    public void playFrom(int seconds){
        Object nativeClip = audioEnvironment.playFrom(this, seconds, cached);
        cache(nativeClip);
    }

    void cache(Object toCache) {
        cacheHash = hashCode();
        this.cached = toCache;
    }

    private boolean isCached() {
        return cacheHash == hashCode();
    }

    Object getCached() {
        return isCached() ? cached : null;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.src != null ? this.src.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AudioClip other = (AudioClip) obj;
        if ((this.src == null) ? (other.src != null) : !this.src.equals(other.src)) {
            return false;
        }
        return true;
    }
}