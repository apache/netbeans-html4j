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
package org.netbeans.html.presenters.browser;

import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;
import org.testng.ITest;
import org.testng.annotations.Test;

public class KOClose implements ITest {

    private final Fn.Presenter presenter;
    private final String prefix;
    private final Fn updateName;

    public KOClose(Fn updateName, String prefix, Fn.Presenter presenter) {
        this.updateName = updateName;
        this.prefix = prefix;
        this.presenter = presenter;
    }

    @Test(dependsOnGroups = "BrowserTest")
    public void closeWindow() {
        ((Executor)this.presenter).execute(() -> {
            try {
                updateName.invoke(null, getTestName(), "5s");
            } catch (Exception ex) {
            }
            JavaScriptUtilities.closeSoon(5000);
        });
    }

    @Override
    public String getTestName() {
        return prefix + ": Closing window";
    }
}
