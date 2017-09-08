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

import java.io.IOException;
import java.io.OutputStream;
import net.java.html.BrwsrCtx;
import org.netbeans.html.json.impl.RcvrJSON;

/** Description of a JSON call request that is supposed to be processed
 * by {@link Transfer#loadJSON(org.netbeans.html.json.spi.JSONCall)} implementors.
 *
 * @author Jaroslav Tulach
 */
public final class JSONCall {
    private final RcvrJSON whenDone;
    private final String headers;
    private final String urlBefore;
    private final String urlAfter;
    private final String method;
    private final Object data;
    private final BrwsrCtx ctx;

    JSONCall(
        BrwsrCtx ctx, RcvrJSON whenDone,
        String headers, String urlBefore, String urlAfter,
        String method, Object data
    ) {
        this.ctx = ctx;
        this.whenDone = whenDone;
        this.headers = headers;
        this.urlBefore = urlBefore;
        this.urlAfter = urlAfter;
        this.method = method;
        this.data = data;
    }

    /** Do we have some data to send? Can the {@link #writeData(java.io.OutputStream)} method be
     * called?
     *
     * @return true, if the call has some data to send
     */
    public boolean isDoOutput() {
        return this.data != null;
    }

    public void writeData(OutputStream os) throws IOException {
        if (this.data == null) {
            throw new IOException("No data!");
        }
        os.write(this.data.toString().getBytes("UTF-8"));
        os.flush();
    }

    /** Additional headers to be included in the request.
     * Usually multiline string to be appended into the header.
     *
     * @return <code>null</code> or string with prepared (HTTP) request headers
     * @since 1.2
     */
    public String getHeaders() {
        return headers;
    }

    public String getMethod() {
        return method;
    }

    public boolean isJSONP() {
        return urlAfter != null;
    }

    public String composeURL(String jsonpCallback) {
        if ((urlAfter == null) != (jsonpCallback == null)) {
            throw new IllegalStateException();
        }
        if (urlAfter != null) {
            return urlBefore + jsonpCallback + urlAfter;
        } else {
            return urlBefore;
        }
    }

    public void notifySuccess(Object result) {
        if (result == null) {
            dispatch(RcvrJSON.MsgEvnt.createOpen());
        } else {
            dispatch(RcvrJSON.MsgEvnt.createMessage(result));
        }
    }

    public void notifyError(Throwable error) {
        if (error == null) {
            dispatch(RcvrJSON.MsgEvnt.createClose());
        } else {
            dispatch(RcvrJSON.MsgEvnt.createError(error));
        }
    }

    private void dispatch(final RcvrJSON.MsgEvnt ev) {
        ctx.execute(new Runnable() {
            @Override
            public void run() {
                ev.dispatch(whenDone);
            }
        });
    }

    public String getMessage() {
        return this.data.toString();
    }
}
