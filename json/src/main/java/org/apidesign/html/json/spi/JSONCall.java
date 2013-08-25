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

import java.io.IOException;
import java.io.OutputStream;
import net.java.html.BrwsrCtx;
import org.apidesign.html.json.impl.JSON;
import org.apidesign.html.json.impl.RcvrJSON;

/** Description of a JSON call request that is supposed to be processed
 * by {@link Transfer#loadJSON(org.apidesign.html.json.spi.JSONCall)} implementors.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JSONCall {
    private final RcvrJSON whenDone;
    private final String urlBefore;
    private final String urlAfter;
    private final String method;
    private final Object data;
    private final BrwsrCtx ctx;

    JSONCall(BrwsrCtx ctx, RcvrJSON whenDone, String urlBefore, String urlAfter, String method, Object data) {
        this.ctx = ctx;
        this.whenDone = whenDone;
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
        JSON.runInBrowser(ctx, new Runnable() {
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
