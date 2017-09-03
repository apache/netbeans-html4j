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
