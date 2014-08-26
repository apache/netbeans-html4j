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
package org.netbeans.html.json.spi;

import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts.Provider;

/** Interface for providers of WebSocket protocol. Register into a 
 * {@link BrwsrCtx context} in your own {@link Provider}
 *
 * @author Jaroslav Tulach
 * @param <WebSocket> internal implementation type representing the socket
 * @since 0.5
 */
public interface WSTransfer<WebSocket> {
    /** Initializes a web socket. The <code>callback</code> object should 
     * have mostly empty values: {@link JSONCall#isDoOutput()} should be 
     * <code>false</code> and thus there should be no {@link JSONCall#getMessage()}.
     * The method of connection {@link JSONCall#getMethod()} is "WebSocket".
     * Once the connection is open call {@link JSONCall#notifySuccess(java.lang.Object) notifySuccess(null)}.
     * When the server sends some data then, pass them to 
     * {@link JSONCall#notifySuccess(java.lang.Object) notifySuccess} method
     * as well. If there is an error call {@link JSONCall#notifyError(java.lang.Throwable)}.
     * 
     * @param url the URL to connect to
     * @param callback a way to provide results back to the client
     * @return your object representing the established web socket
     */
    public WebSocket open(String url, JSONCall callback);

    /** Sends data to the server. The most important value
     * of the <code>data</code> parameter is {@link JSONCall#getMessage()},
     * rest can be ignored.
     * 
     * @param socket internal representation of the socket
     * @param data the message to be sent
     */
    public void send(WebSocket socket, JSONCall data);

    /** A request to close the socket.
     * @param socket internal representation of the socket
     */
    public void close(WebSocket socket);
}
