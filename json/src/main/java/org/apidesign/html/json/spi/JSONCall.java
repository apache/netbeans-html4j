/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package org.apidesign.html.json.spi;

import java.io.IOException;
import java.io.OutputStream;
import net.java.html.BrwsrCtx;
import org.netbeans.html.json.impl.JSON;
import org.netbeans.html.json.impl.RcvrJSON;

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
