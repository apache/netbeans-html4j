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
package org.netbeans.html.xhr4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;
import net.java.html.js.JavaScriptBody;
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
            thread.setName("xhr4j daemon");
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
            String response = readStream(is);
            if (call.isJSONP()) {
                response = '(' + response;
            }
            json = new Result(response, arrayOrString[0], arrayOrString[1]);
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

    private static final class Result implements Callable<Object> {
        private final String response;
        private final boolean array;
        private final boolean plain;

        Result(String response, boolean array, boolean plain) {
            this.response = response;
            this.array = array;
            this.plain = plain;
        }

        @Override
        public Object call() throws Exception {
            if (plain) {
                return response;
            } else {
                if (array) {
                    Object r = parse(response);
                    Object[] arr = r instanceof Object[] ? (Object[])r : new Object[] { r };
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = new JSObjToStr(response, arr[i]);
                    }
                    return arr;
                } else {
                    return new JSObjToStr(response, parse(response));
                }
            }
        }
    }
    private static final class JSObjToStr {
        final String str;
        final Object obj;

        public JSObjToStr(Object str, Object obj) {
            this.str = str == null ? "" : str.toString();
            this.obj = obj;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    static String readStream(InputStream is) throws IOException, UnsupportedEncodingException {
        Reader r = new InputStreamReader(is, "UTF-8");
        StringBuilder sb = new StringBuilder();
        char[] arr = new char[4096];
        for (;;) {
            int len = r.read(arr);
            if (len == -1) {
                break;
            }
            sb.append(arr, 0, len);
        }
        return sb.toString();
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

    @JavaScriptBody(args = {"object", "property"}, body =
        "var ret;\n" + 
        "if (property === null) ret = object;\n" + 
        "else if (object === null) ret = null;\n" + 
        "else ret = object[property];\n" +
        "if (typeof ret !== 'undefined' && ret !== null) {\n" +
        "  if (typeof ko !== 'undefined' && ko['utils'] && ko['utils']['unwrapObservable']) {\n" +
        "    return ko['utils']['unwrapObservable'](ret);\n" +
        "  }\n" +
        "  return ret;\n" +
        "}\n" +
        "return null;\n"
    )
    private static Object getProperty(Object object, String property) {
        return null;
    }

    @JavaScriptBody(args = {"s"}, body = "return eval('(' + s + ')');")
    static Object parse(String s) {
        throw new IllegalStateException("No parser context for " + s);
    }

    static void extractJSON(Object js, String[] props, Object[] values) {
        if (js instanceof JSObjToStr) {
            js = ((JSObjToStr)js).obj;
        }
        for (int i = 0; i < props.length; i++) {
            values[i] = getProperty(js, props[i]);
        }
    }

}
