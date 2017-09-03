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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
        "return ret ? (typeof ko === 'undefined' ? ret : ko.utils.unwrapObservable(ret)) : null;"
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
