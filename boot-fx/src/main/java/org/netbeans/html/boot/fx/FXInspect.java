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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;

/**
 *
 * @author Jaroslav Tulach
 */
final class FXInspect implements Runnable {
    static final Logger LOG = Logger.getLogger(FXInspect.class.getName());
    
    
    private final WebEngine engine;
    private final ObjectInputStream input;
    private Dbgr dbg;
    
    private FXInspect(WebEngine engine, int port) throws IOException {
        this.engine = engine;
        
        Socket socket = new Socket(InetAddress.getByName(null), port);
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
        initializeDebugger(output);
    }
    
    static boolean initialize(WebEngine engine) {
        final int inspectPort = Integer.getInteger("netbeans.inspect.port", -1); // NOI18N
        if (inspectPort != -1) {
            try {
                FXInspect inspector = new FXInspect(engine, inspectPort);
                Thread t = new Thread(inspector, "FX<->NetBeans Inspector");
                t.start();
                return true;
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot connect to NetBeans IDE to port " + inspectPort, ex); // NOI18N
            }
        }
        return false;
    }
    
    private void initializeDebugger(final ObjectOutputStream output) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dbg = new Dbgr(engine, new Callback<String,Void>() {
                    @Override
                    public Void call(String message) {
                        try {
                            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                            output.writeInt(bytes.length);
                            output.write(bytes);
                            output.flush();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        }
                        return null;
                    }
                });
            }
        });
    }

    @Override
    public void run() {
        try {
            while (true) {
                int length = input.readInt();
                byte[] bytes = new byte[length];
                input.readFully(bytes);
                final String message = new String(bytes, StandardCharsets.UTF_8);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        dbg.sendMessage(message);
                    }
                });
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }
}
