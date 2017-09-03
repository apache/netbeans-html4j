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
package org.netbeans.html.json.spi;

import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts.Provider;
import org.netbeans.html.context.spi.Contexts.Id;

/** Interface for providers of WebSocket protocol. Register into a 
 * {@link BrwsrCtx context} in your own {@link Provider}.
 * Since introduction of {@link Id technology identifiers} one can choose between
 * different background implementations to handle the conversion and
 * communication requests. The known providers include
 * <code>org.netbeans.html:ko4j</code> module which registers 
 * a native browser implementation called <b>websocket</b>, and a
 * <code>org.netbeans.html:ko-ws-tyrus</code> module which registers 
 * Java based implementation named <b>tyrus</b>.
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
