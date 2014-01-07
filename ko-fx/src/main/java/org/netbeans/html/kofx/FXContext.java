/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package org.netbeans.html.kofx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javafx.application.Platform;
import net.java.html.js.JavaScriptBody;
import netscape.javascript.JSObject;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.spi.WSTransfer;
import org.openide.util.lookup.ServiceProvider;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Registers {@link ContextProvider}, so {@link ServiceLoader} can find it.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class FXContext
implements Technology.BatchInit<JSObject>, Transfer, WSTransfer<LoadWS> {
    static final Logger LOG = Logger.getLogger(FXContext.class.getName());
    private static Boolean javaScriptEnabled;
    private final Fn.Presenter browserContext;

    public FXContext(Fn.Presenter browserContext) {
        this.browserContext = browserContext;
    }
    
    @JavaScriptBody(args = {}, body = "return true;")
    private static boolean isJavaScriptEnabledJs() {
        return false;
    }
    
    static boolean isJavaScriptEnabled() {
        if (javaScriptEnabled != null) {
            return javaScriptEnabled;
        }
        return javaScriptEnabled = isJavaScriptEnabledJs();
    }

    final boolean areWebSocketsSupported() {
        return LoadWS.isSupported();
    }


    @Override
    public JSObject wrapModel(Object model, PropertyBinding[] propArr, FunctionBinding[] funcArr) {
        String[] propNames = new String[propArr.length];
        boolean[] propReadOnly = new boolean[propArr.length];
        Object[] propValues = new Object[propArr.length];
        for (int i = 0; i < propNames.length; i++) {
            propNames[i] = propArr[i].getPropertyName();
            propReadOnly[i] = propArr[i].isReadOnly();
            propValues[i] = propArr[i].getValue();
        }
        String[] funcNames = new String[funcArr.length];
        for (int i = 0; i < funcNames.length; i++) {
            funcNames[i] = funcArr[i].getFunctionName();
        }
        
        return Knockout.wrapModel(model, 
            propNames, propReadOnly, Knockout.toArray(propValues), propArr, 
            funcNames, funcArr
        );
    }
    
    @Override
    public JSObject wrapModel(Object model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(PropertyBinding b, Object model, JSObject data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void valueHasMutated(JSObject data, String propertyName) {
        Knockout.valueHasMutated(data, propertyName);
    }

    @Override
    public void expose(FunctionBinding fb, Object model, JSObject d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyBindings(JSObject data) {
        Knockout.applyBindings(data);
    }

    @Override
    public Object wrapArray(Object[] arr) {
        return Knockout.toArray(arr);
    }

    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        LoadJSON.extractJSON(obj, props, values);
    }

    @Override
    public void loadJSON(final JSONCall call) {
        LoadJSON.loadJSON(call);
    }

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        if (data instanceof JSObject) {
            data = ((JSObject)data).getMember("ko-fx.model"); // NOI18N
        }
        return modelClass.cast(data);
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        return LoadJSON.parse(is);
    }

    @Override
    public void runSafe(final Runnable r) {
        class Wrap implements Runnable {
            @Override public void run() {
                try (Closeable c = Fn.activate(browserContext)) {
                    r.run();
                } catch (IOException ex) {
                    // cannot be thrown
                }
            }
        }
        Wrap w = new Wrap();
        
        if (Platform.isFxApplicationThread()) {
            w.run();
        } else {
            Platform.runLater(w);
        }
    }

    @Override
    public LoadWS open(String url, JSONCall onReply) {
        return new LoadWS(onReply, url);
    }

    @Override
    public void send(LoadWS socket, JSONCall data) {
        socket.send(data);
    }

    @Override
    public void close(LoadWS socket) {
        socket.close();
    }
    
    @ServiceProvider(service = Contexts.Provider.class)
    public static final class Prvdr implements Contexts.Provider {
        @Override
        public void fillContext(Contexts.Builder context, Class<?> requestor) {
            if (isJavaScriptEnabled()) {
                FXContext c = new FXContext(Fn.activePresenter());
                
                context.register(Technology.class, c, 100);
                context.register(Transfer.class, c, 100);
                if (c.areWebSocketsSupported()) {
                    context.register(WSTransfer.class, c, 100);
                }
            }
        }
    }
}
