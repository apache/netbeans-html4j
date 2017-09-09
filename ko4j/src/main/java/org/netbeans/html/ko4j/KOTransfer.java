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
package org.netbeans.html.ko4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import net.java.html.json.Models;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.Transfer;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 *
 * @author Jaroslav Tulach
 */
@Contexts.Id("xhr")
final class KOTransfer
implements Transfer {
    KOTransfer() {
    }

    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        if (obj instanceof JSObjToStr) {
            obj = ((JSObjToStr)obj).obj;
        }
        LoadJSON.extractJSON(obj, props, values);
    }

    @Override
    public void loadJSON(final JSONCall call) {
        if (call.isJSONP()) {
            String me = LoadJSON.createJSONP(call);
            LoadJSON.loadJSONP(call.composeURL(me), me);
        } else {
            String data = null;
            if (call.isDoOutput()) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    call.writeData(bos);
                    data = new String(bos.toByteArray(), "UTF-8");
                } catch (IOException ex) {
                    call.notifyError(ex);
                }
            }
            List<String> headerPairs = Models.asList();
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
                    headerPairs.add(h.substring(pos, tagEnd).trim());
                    headerPairs.add(h.substring(tagEnd + 1, Math.min(r, n)).trim());
                    pos = Math.max(r, n);
                }
            }
            LoadJSON.loadJSON(call.composeURL(null), call, call.getMethod(), data, headerPairs.toArray());
        }
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return LoadJSON.parse(sb.toString());
    }

    static void notifySuccess(Object done, Object str, Object data) {
        Object notifyObj;
        if (data instanceof Object[]) {
            Object[] arr = (Object[]) data;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new JSObjToStr(str, arr[i]);
            }
            notifyObj = arr;
        } else {
            notifyObj = new JSObjToStr(str, data);
        }
        ((JSONCall)done).notifySuccess(notifyObj);
    }

    static void notifyError(Object done, Object msg) {
        ((JSONCall)done).notifyError(new Exception(msg.toString()));
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
}
