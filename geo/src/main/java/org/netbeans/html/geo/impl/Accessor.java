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
import org.netbeans.html.geo.spi.GLProvider;

/** Connection between API and SPI parts of the module.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class Accessor {
    public static Accessor SPI;
    static {
        JsGLProvider initGLProviderClass = new JsGLProvider();
    }
    
    protected Accessor(boolean api) {
        if (!api) {
            assert SPI == null;
            SPI = this;
        }
    }
    
    public abstract <Watch> Watch start(
        GLProvider<?, Watch> p, Accessor peer,
        boolean oneTime, boolean enableHighAccuracy,
        long timeout, long maximumAge
    );
    
    public abstract <Watch> void stop(GLProvider<?, Watch> p, Watch w);
    
    public abstract void onError(Exception ex);

    public abstract void onLocation(Position position);
}
