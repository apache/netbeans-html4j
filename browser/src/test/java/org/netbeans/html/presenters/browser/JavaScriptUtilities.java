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
package org.netbeans.html.presenters.browser;

import net.java.html.js.JavaScriptBody;

final class JavaScriptUtilities {
    private JavaScriptUtilities() {
    }

    @JavaScriptBody(args = {  }, body =
          """
          var h;if (!!window && !!window.location && !!window.location.href)
            h = window.location.href;
          else   h = null;return h;
          """
    )
    static native String findBaseURL();

    @JavaScriptBody(args = {"value"}, body = "document.getElementById('loaded').innerHTML = value;")
    static native void setLoaded(String value);

    @JavaScriptBody(args = {"ms"}, body = "window.setTimeout(function() { window.close(); }, ms);")
    static native void closeSoon(int ms);

}
