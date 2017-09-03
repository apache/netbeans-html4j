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
package org.netbeans.html.boot.fx;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

/**
 *
 * @author Jaroslav Tulach
 */
final class WatchDir implements Runnable {
    private final Path dir;
    private final WatchKey key;
    private final WatchService ws; 
    private final Thread watcher;
    private final WebEngine engine;
    
    WatchDir(WebEngine eng) throws URISyntaxException, IOException {
        dir = Paths.get(new URI(eng.getLocation())).getParent();
        engine = eng;
        ws = dir.getFileSystem().newWatchService();
        key = dir.register(ws, 
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        );
        watcher = new Thread(this, "Watching files in " + dir);
        watcher.setDaemon(true);
        watcher.setPriority(Thread.MIN_PRIORITY);
        watcher.start();
    }

    public void close() throws IOException {
        key.cancel();
        ws.close();
        watcher.interrupt();
    }

    @Override
    public void run() {
        if (Platform.isFxApplicationThread()) {
            engine.reload();
            return;
        }
        try {
            while (key.isValid()) {
                WatchKey changed;
                try {
                    changed = ws.take();
                    if (changed != key || changed.pollEvents().isEmpty()) {
                        continue;
                    }
                } catch (ClosedWatchServiceException ex) {
                    continue;
                }
                Platform.runLater(this);
                if (!key.reset()) {
                    break;
                }
            }
        } catch (InterruptedException ex) {
            FXInspect.LOG.log(Level.SEVERE, null, ex);
        }
    }
}
