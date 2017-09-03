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
package net.java.html.boot.truffle;

import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;


/** Integration with Truffle and
 * <a href="http://www.oracle.com/technetwork/oracle-labs/program-languages/overview/">GraalVM</a>.
 *
 * @since 1.4
 */
public final class TrufflePresenters {
    private TrufflePresenters() {
    }

    /** Creates new instance of Truffle based presenter.
     *
     * @param executor the executor to run requests in
     * @return new instance of the presenter
     */
    public static Fn.Presenter create(Executor executor) {
        return new TrufflePresenter(executor, null);
    }
}
