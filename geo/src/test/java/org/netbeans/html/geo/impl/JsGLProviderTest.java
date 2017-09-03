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
package org.netbeans.html.geo.impl;

import net.java.html.geo.Position;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JsGLProviderTest extends Position.Handle {
    public JsGLProviderTest() {
        super(true);
    }

    @Test public void checkWhetherWeCanInstantiate() {
        assertNotNull(new JsGLProvider());
    }

    @Test public void canCallIsSupported() {
        assertFalse(isSupported(), "Well, it is not, as we are not in a browser context");
    }

    @Override
    protected void onLocation(Position p) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onError(Exception ex) throws Throwable {
        throw new UnsupportedOperationException();
    }
    
}
