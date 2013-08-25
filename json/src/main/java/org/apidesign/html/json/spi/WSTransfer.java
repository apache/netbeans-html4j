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
package org.apidesign.html.json.spi;

import net.java.html.BrwsrCtx;
import org.apidesign.html.context.spi.Contexts;

/** Interface for providers of WebSocket protocol. Register into a 
 * {@link BrwsrCtx context} in your own {@link Contexts.Provider}
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
