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
