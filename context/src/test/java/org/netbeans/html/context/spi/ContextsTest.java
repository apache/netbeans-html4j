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
package org.netbeans.html.context.spi;

import javax.xml.ws.ServiceMode;
import net.java.html.BrwsrCtx;
import org.openide.util.lookup.ServiceProvider;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ContextsTest {
    
    public ContextsTest() {
    }

    @Test public void twoInstancesButOneCall() {
        class Two implements Runnable {
            int cnt;

            @Override
            public void run() {
                cnt++;
            }
        }
        class One implements Runnable {
            int cnt;

            @Override
            public void run() {
                cnt++;
            }
        }
        
        One one = new One();
        Two two = new Two();
        
        CountingProvider.onNew = two;
        CountingProvider.onFill = one;
        
        Contexts.Builder b = Contexts.newBuilder();
        Contexts.fillInByProviders(ContextsTest.class, b);

        assertEquals(two.cnt, 2, "Two instances created");
        assertEquals(one.cnt, 1, "But only one call to fill");
    }

    @ServiceProvider(service = Contexts.Provider.class)
    public static final class CountingProvider implements Contexts.Provider {
        static Runnable onNew;
        static Runnable onFill;

        public CountingProvider() {
            if (onNew != null) {
                onNew.run();
            }
        }
        
        @Override
        public void fillContext(Contexts.Builder context, Class<?> requestor) {
            if (onFill != null) {
                onFill.run();
                context.register(Runnable.class, onFill, 1);
            }
        }
    }
    
}
