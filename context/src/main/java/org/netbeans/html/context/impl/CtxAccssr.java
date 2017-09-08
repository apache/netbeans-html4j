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
package org.netbeans.html.context.impl;

import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts.Builder;

/** Internal communication between API (e.g. {@link BrwsrCtx}), SPI
 * (e.g. {@link Builder}) and the implementation package.
 *
 * @author Jaroslav Tulach
 */
public abstract class CtxAccssr {
    private static CtxAccssr DEFAULT;
    static {
        // run initializers
        BrwsrCtx.findDefault(CtxAccssr.class);
    }
    
    protected CtxAccssr() {
        if (DEFAULT != null) throw new IllegalStateException();
        DEFAULT = this;
    }
    
    protected abstract BrwsrCtx newContext(CtxImpl impl);
    protected abstract CtxImpl find(BrwsrCtx context);
    
    static CtxAccssr getDefault() {
        return DEFAULT;
    }
}
