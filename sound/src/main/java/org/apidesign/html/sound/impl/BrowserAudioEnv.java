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
package org.apidesign.html.sound.impl;

import net.java.html.js.JavaScriptBody;
import org.apidesign.html.sound.spi.AudioEnvironment;
import org.openide.util.lookup.ServiceProvider;

/** Registers an audio provider that delegates to HTML5 Audio tag.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = AudioEnvironment.class, position = 100)
public final class BrowserAudioEnv implements AudioEnvironment<Object> {
    @Override
    @JavaScriptBody(args = { "src" }, body = ""
        + "if (!Audio) return null;"
        + "return new Audio(src);")
    public Object create(String src) {
        // null if not running in browser
        return null;
    }

    @Override @JavaScriptBody(args = { "a" }, body = "a.play();")
    public void play(Object a) {
    }

    @Override @JavaScriptBody(args = { "a" }, body = "a.pause();")
    public void pause(Object a) {
    }

    @Override @JavaScriptBody(args = { "a", "volume" }, body = "a.setVolume(volume);")
    public void setVolume(Object a, double volume) {
    }

    @Override
    @JavaScriptBody(args = "a", body = "return true;")
    public boolean isSupported(Object a) {
        return false;
    }
}
