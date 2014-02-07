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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package org.apidesign.html.sound.spi;

/** Basic interface for sound playback providers. Register your implementation
 * in a way {@link java.util.ServiceLoader} can find it - e.g. use
 * {@link org.openide.util.lookup.ServiceProvider} annotation.
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
     * @param a
     * @return <code>true</code> or <code>false</code>
     */
    public boolean isSupported(Audio a);
}
