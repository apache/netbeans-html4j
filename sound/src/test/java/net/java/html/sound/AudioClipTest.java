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

import net.java.html.js.JavaScriptBody;
import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.netbeans.html.boot.spi.Fn;

@RunWith(BrowserRunner.class)
public class AudioClipTest {

    public AudioClipTest() {
    }

    @Test
    public void testPlayNonExistingClip() {
        var clip = AudioClip.create("non-existing.mp3");
        clip.play();
    }

    @Test
    public void testAudioSystemIsSupported() {
        var clip = AudioClip.create("non-existing.mp3");
        if (assumeAudioObject()) {
            assertTrue("Playing should be supported on modern browsers", clip.isSupported());
        }
    }

    @JavaScriptBody(args = {  }, body = """
    try {
        var audio = new Audio("any.mp3");
        return null;
    } catch (err) {
        return "Error constructing new Audio: " + err.toString();
    }
    """)
    private static String createAudioObjectOrFail() {
        return "Not running in a browser at all!";
    }

    private static boolean assumeAudioObject() {
        var errMsg = createAudioObjectOrFail();
        var p = Fn.activePresenter().getClass().getName();
        if (errMsg == null) {
            System.err.println("Audio found for " + p);
            return true;
        } else {
            // it is expected that ScriptPresenter doesn't have Audio
            Assume.assumeTrue("No Audio for " + p + ":" + errMsg, p.contains("ScriptPresenter"));
            return false;
        }
    }
}
