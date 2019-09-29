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
package com.dukescript.presenters.renderer;

import com.sun.jna.Pointer;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

final class AWT extends Show {
    @Override
    public void show(URI page) throws IOException {
        try {
            LOG.log(Level.FINE, "Trying Desktop.browse on {0} {2} by {1}", new Object[]{
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.vendor"),
                System.getProperty("java.vm.version"),});
            java.awt.Desktop.getDesktop().browse(page);
            LOG.log(Level.FINE, "Desktop.browse successfully finished");
            System.in.read();
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.FINE, "Desktop.browse not supported: {0}", ex.getMessage());
            throw new IOException(ex);
        }
    }

    @Override
    public JSC jsc() {
        return null;
    }

    @Override
    public Pointer jsContext() {
        return null;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
