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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class TitleTest {
    private static Runnable whenInitialized;
    
    public TitleTest() {
    }

    @JavaScriptBody(args = { "a", "b"  }, body = "return a + b;")
    private static native int plus(int a, int b);
    
    @Test public void checkReload() throws Throwable {
        final Throwable[] arr = { null };
        
        final BrowserBuilder bb = BrowserBuilder.newBrowser().loadClass(TitleTest.class).
                loadPage("empty.html").
                invoke("initialized");
        
        class ShowBrowser implements Runnable {
            @Override
            public void run() {
                bb.showAndWait();
            }
        }
        
        class WhenInitialized implements Runnable {
            CountDownLatch cdl = new CountDownLatch(1);
            AbstractFXPresenter p;
            BrwsrCtx ctx;
            
            @Override
            public void run() {
                try {
                    whenInitialized = null;
                    doCheckReload();
                    p = (AbstractFXPresenter) Fn.activePresenter();
                    assertNotNull(p, "Presenter is defined");
                    ctx = BrwsrCtx.findDefault(WhenInitialized.class);
                } catch (Throwable ex) {
                    arr[0] = ex;
                } finally {
                    cdl.countDown();
                }
            }
        }
        WhenInitialized when = new WhenInitialized();
        whenInitialized = when;
        Executors.newSingleThreadExecutor().submit(new ShowBrowser());
        when.cdl.await();
        if (arr[0] != null) throw arr[0];
        
        Stage s = FXBrwsr.findStage();
        assertEquals(s.getTitle(), "FX Presenter Harness");
        
        final CountDownLatch propChange = new CountDownLatch(1);
        s.titleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                propChange.countDown();
            }
        });
        
        when.ctx.execute(new Runnable() {
            @Override
            public void run() {
                changeTitle("New title");
            }
        });

        propChange.await(5, TimeUnit.SECONDS);
        assertEquals(s.getTitle(), "New title");
    }
    
    final void doCheckReload() throws Exception {
        int res = plus(30, 12);
        assertEquals(res, 42, "Meaning of world computed");
    }
    
    public static synchronized void initialized() throws Exception {
        whenInitialized.run();
    }
    
    @JavaScriptBody(args = { "s" }, body = 
        "document.getElementsByTagName('title')[0].innerHTML = s;"
    )
    static native void changeTitle(String s);
}
