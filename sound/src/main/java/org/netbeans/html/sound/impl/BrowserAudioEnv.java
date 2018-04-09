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
package org.netbeans.html.sound.impl;

import net.java.html.js.JavaScriptBody;
import org.netbeans.html.sound.spi.AudioEnvironment;

/** The default audio provider that delegates to HTML5 Audio tag
 * it is used if no other {@link AudioEnvironment} is found.
 *
 * @author Jaroslav Tulach
 */
public final class BrowserAudioEnv implements AudioEnvironment<Object> {
    public static final AudioEnvironment<?> DEFAULT = new BrowserAudioEnv();
    
    private BrowserAudioEnv() {
    }
    
    @Override
    @JavaScriptBody(args = { "src" }, body = ""
        + "if (typeof Audio !== 'object') return null;"
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
