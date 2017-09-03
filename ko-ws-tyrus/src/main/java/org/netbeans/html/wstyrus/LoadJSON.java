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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.netbeans.html.json.spi.JSONCall;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 *
 * @author Jaroslav Tulach
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
            String h = call.getHeaders();
            if (h != null) {
                int pos = 0;
                while (pos < h.length()) {
                    int tagEnd = h.indexOf(':', pos);
                    if (tagEnd == -1) {
                        break;
                    }
                    int r = h.indexOf('\r', tagEnd);
                    int n = h.indexOf('\n', tagEnd);
                    if (r == -1) {
                        r = h.length();
                    }
                    if (n == -1) {
                        n = h.length();
                    }
                    String key = h.substring(pos, tagEnd).trim();
                    String val = h.substring(tagEnd + 1, Math.min(r, n)).trim();
                    conn.setRequestProperty(key, val);;
                    pos = Math.max(r, n);
                }
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

    @JavaScriptBody(args = {"object", "property"}, body =
        "var ret;\n" + 
        "if (property === null) ret = object;\n" + 
        "else if (object === null) ret = null;\n" + 
        "else ret = object[property];\n" + 
        "return ret ? (typeof ko === 'undefined' ? ret : ko.utils.unwrapObservable(ret)) : null;"
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
