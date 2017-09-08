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
import net.java.html.json.OnReceive;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.Transfer;
import org.openide.util.lookup.ServiceProvider;

/** Implementation module with support for XHR via Java.
 * Handles {@link OnReceive} requests by using Java to connect to given
 * URL and then parsing it via JavaScript. Use this module if you have
 * problems with CORS - as the Java connection isn't restricted by CORS
 * rules.
 * 
 * Registers {@link Transfer} technology at position <code>50</code>.
 * The {@link Contexts.Id} of the technology is <b>xhr4j</b>.
 * 
 * @author Jaroslav Tulach
 * @since 1.3
 */
@Contexts.Id("xhr4j")
@ServiceProvider(service = Contexts.Provider.class)
public final class XmlHttpResourceContext
implements Contexts.Provider, Transfer {
    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        context.register(Transfer.class, this, 50);
    }

    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        LoadJSON.extractJSON(obj, props, values);
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        return LoadJSON.parse(LoadJSON.readStream(is));
    }

    @Override
    public void loadJSON(JSONCall call) {
        LoadJSON.loadJSON(call);
    }
}
