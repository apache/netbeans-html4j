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
package org.netbeans.html.kofx;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import net.java.html.js.JavaScriptBody;
import netscape.javascript.JSObject;
import org.apidesign.html.json.spi.JSONCall;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LoadJSON implements Runnable {
    private static final Logger LOG = FXContext.LOG;
    private static final Executor REQ = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    });

    private final JSONCall call;
    private final URL base;
    private Throwable error;
    private Object json;


    private LoadJSON(JSONCall call) {
        this.call = call;
        URL b;
        try {
            b = new URL(findBaseURL());
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, "Can't find base url for " + call.composeURL("dummy"), ex);
            b = null;
        }
        this.base = b;
    }

    public static void loadJSON(JSONCall call) {
        assert !"WebSocket".equals(call.getMethod());
        REQ.execute(new LoadJSON((call)));
    }

    @Override
    public void run() {
        if (Platform.isFxApplicationThread()) {
            if (error != null) {
                call.notifyError(error);
            } else {
                call.notifySuccess(json);
            }
            return;
        }
        final String url;
        if (call.isJSONP()) {
            url = call.composeURL("dummy");
        } else {
            url = call.composeURL(null);
        }
        try {
            final URL u = new URL(base, url.replace(" ", "%20"));
            URLConnection conn = u.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection huc = (HttpURLConnection) conn;
                if (call.getMethod() != null) {
                    huc.setRequestMethod(call.getMethod());
                }
                if (call.isDoOutput()) {
                    huc.setDoOutput(true);
                    final OutputStream os = huc.getOutputStream();
                    call.writeData(os);
                    os.flush();
                }
            }
            final PushbackInputStream is = new PushbackInputStream(
                conn.getInputStream(), 1
            );
            boolean array = false;
            boolean string = false;
            if (call.isJSONP()) {
                for (;;) {
                    int ch = is.read();
                    if (ch == -1) {
                        break;
                    }
                    if (ch == '[') {
                        is.unread(ch);
                        array = true;
                        break;
                    }
                    if (ch == '{') {
                        is.unread(ch);
                        break;
                    }
                }
            } else {
                int ch = is.read();
                if (ch == -1) {
                    string = true;
                } else {
                    array = ch == '[';
                    is.unread(ch);
                    if (!array && ch != '{') {
                        string = true;
                    }
                }
            }
            try {
                if (string) {
                    throw new JSONException("");
                }
                Reader r = new InputStreamReader(is, "UTF-8");

                JSONTokener tok = new JSONTokener(r);
                Object obj;
                obj = array ? new JSONArray(tok) : new JSONObject(tok);
                json = convertToArray(obj);
            } catch (JSONException ex) {
                Reader r = new InputStreamReader(is, "UTF-8");
                StringBuilder sb = new StringBuilder();
                for (;;) {
                    int ch = r.read();
                    if (ch == -1) {
                        break;
                    }
                    sb.append((char)ch);
                }
                json = sb.toString();
            }
        } catch (IOException ex) {
            error = ex;
        } finally {
            Platform.runLater(this);
        }
    }

    static Object convertToArray(Object o) throws JSONException {
        if (o instanceof JSONArray) {
            JSONArray ja = (JSONArray)o;
            Object[] arr = new Object[ja.length()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = convertToArray(ja.get(i));
            }
            return arr;
        } else if (o instanceof JSONObject) {
            JSONObject obj = (JSONObject)o;
            Iterator it = obj.keys();
            while (it.hasNext()) {
                String key = (String)it.next();
                obj.put(key, convertToArray(obj.get(key)));
            }
            return obj;
        } else {
            return o;
        }
    }
    
    public static void extractJSON(Object jsonObject, String[] props, Object[] values) {
        if (jsonObject instanceof JSONObject) {
            JSONObject obj = (JSONObject)jsonObject;
            for (int i = 0; i < props.length; i++) {
                try {
                    values[i] = obj.has(props[i]) ? obj.get(props[i]) : null;
                } catch (JSONException ex) {
                    LoadJSON.LOG.log(Level.SEVERE, "Can't read " + props[i] + " from " + jsonObject, ex);
                }
            }
        }
        if (jsonObject instanceof JSObject) {
            JSObject obj = (JSObject)jsonObject;
            for (int i = 0; i < props.length; i++) {
                Object val = obj.getMember(props[i]);
                values[i] = isDefined(val) ? val : null;
            }
        }
    }
    
    public static Object parse(InputStream is) throws IOException {
        try {
            InputStreamReader r = new InputStreamReader(is, "UTF-8");
            JSONTokener t = new JSONTokener(r);
            return new JSONObject(t);
        } catch (JSONException ex) {
            throw new IOException(ex);
        }
    }

    @JavaScriptBody(args = {  }, body = 
          "var h;"
        + "if (!!window && !!window.location && !!window.location.href)\n"
        + "  h = window.location.href;\n"
        + "else "
        + "  h = null;"
        + "return h;\n"
    )
    private static native String findBaseURL();
    
    private static boolean isDefined(Object val) {
        return !"undefined".equals(val);
    }
}
