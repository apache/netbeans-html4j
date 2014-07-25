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
package org.netbeans.html.wstyrus;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;
import net.java.html.js.JavaScriptBody;
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
    private static final Logger LOG = Logger.getLogger(LoadJSON.class.getName());
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


    private LoadJSON(JSONCall call) {
        this.call = call;
        this.base = null;
    }

    public static void loadJSON(JSONCall call) {
        assert !"WebSocket".equals(call.getMethod());
        REQ.execute(new LoadJSON((call)));
    }

    @Override
    public void run() {
        final String url;
        Throwable error = null;
        Object json = null;
        
        if (call.isJSONP()) {
            url = call.composeURL("dummy");
        } else {
            url = call.composeURL(null);
        }
        try {
            final URL u = new URL(base, url.replace(" ", "%20"));
            URLConnection conn = u.openConnection();
            if (call.isDoOutput()) {
                conn.setDoOutput(true);
            }
            if (call.getMethod() != null && conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setRequestMethod(call.getMethod());
            }
            if (call.isDoOutput()) {
                final OutputStream os = conn.getOutputStream();
                call.writeData(os);
                os.flush();
            }
            final PushbackInputStream is = new PushbackInputStream(
                conn.getInputStream(), 1
            );
            boolean[] arrayOrString = { false, false };
            detectJSONType(call.isJSONP(), is, arrayOrString);
            try {
                if (arrayOrString[1]) {
                    throw new JSONException("");
                }
                JSONTokener tok = createTokener(is);
                Object obj;
                obj = arrayOrString[0] ? new JSONArray(tok) : new JSONObject(tok);
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
            if (error != null) {
                call.notifyError(error);
            } else {
                call.notifySuccess(json);
            }
        }
    }

    private static void detectJSONType(boolean skipAnything, final PushbackInputStream is, boolean[] arrayOrString) throws IOException {
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                arrayOrString[1] = true;
                break;
            }
            if (Character.isWhitespace(ch)) {
                continue;
            }

            if (ch == '[') {
                is.unread(ch);
                arrayOrString[0] = true;
                break;
            }
            if (ch == '{') {
                is.unread(ch);
                break;
            }
            if (!skipAnything) {
                is.unread(ch);
                arrayOrString[1] = true;
                break;
            }
        }
    }

    private static JSONTokener createTokener(InputStream is) throws IOException {
        Reader r = new InputStreamReader(is, "UTF-8");
        try {
            return new JSONTokener(r);
        } catch (LinkageError ex) {
            // phones may carry outdated version of JSONTokener
            StringBuilder sb = new StringBuilder();
            for (;;) {
                int ch = r.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char)ch);
            }
            return new JSONTokener(sb.toString());
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
            List<Object> collect = new ArrayList<Object>();
            while (it.hasNext()) {
                String key = (String)it.next();
                final Object val = obj.get(key);
                final Object newVal = convertToArray(val);
                if (val != newVal) {
                    collect.add(key);
                    collect.add(newVal);
                }
            }
            int size = collect.size();
            for (int i = 0; i < size; i += 2) {
                obj.put((String) collect.get(i), collect.get(i + 1));
            }
            return obj;
        } else if (o == JSONObject.NULL) {
            return null;
        } else {
            return o;
        }
    }
    
    public static void extractJSON(Object jsonObject, String[] props, Object[] values) {
        if (jsonObject instanceof JSONObject) {
            JSONObject obj = (JSONObject)jsonObject;
            for (int i = 0; i < props.length; i++) {
                Object val = obj.opt(props[i]);
                if (val == JSONObject.NULL) {
                    val = null;
                }
                values[i] = val;
            }
            return;
        }
        for (int i = 0; i < props.length; i++) {
            values[i] = getProperty(jsonObject, props[i]);
        }
    }
    
    @JavaScriptBody(args = {"object", "property"},
            body
            = "if (property === null) return object;\n"
            + "if (object === null) return null;\n"
            + "var p = object[property]; return p ? p : null;"
    )
    private static Object getProperty(Object object, String property) {
        return null;
    }
    
    public static Object parse(InputStream is) throws IOException {
        try {
            PushbackInputStream push = new PushbackInputStream(is, 1);
            boolean[] arrayOrString = { false, false };
            detectJSONType(false, push, arrayOrString);
            JSONTokener t = createTokener(push);
            Object obj = arrayOrString[0] ? new JSONArray(t) : new JSONObject(t);
            return convertToArray(obj);
        } catch (JSONException ex) {
            throw new IOException(ex);
        }
    }
}
