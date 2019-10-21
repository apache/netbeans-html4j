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
package org.netbeans.html.presenters.spi;

import java.io.Flushable;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/** The <em>prototypical</em> presenter. An implementation of a {@link Presenter} based on
 * top of textual protocol transferred between JVM and JavaScript engines. Use
 * {@link ProtoPresenterBuilder#newBuilder()} to construct instance of this
 * interface.
 */
public interface ProtoPresenter extends Fn.Presenter, Fn.KeepAlive, Flushable {
    /** Dispatches callback from JavaScript back into appropriate
     * Java implementation. User of {@link ProtoPresenterBuilder} is expected
     * to register {@link ProtoPresenterBuilder#preparator} and setup a JavaScript
     * call to this method.
     *
     * @param method the type of call to make
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     * @param a4 fourth argument
     * @return returned string
     * @throws Exception if something goes wrong
     */
    String js2java(String method, String a1, String a2, String a3, String a4) throws Exception;

    /** Looks for additional data stored in the presenter. Data
     * can be registered via {@link ProtoPresenterBuilder#register} method.
     *
     * @param <T> the type of data to search for
     * @param type exact type of the data
     * @return found data or null
     */
    <T> T lookup(Class<T> type);
}
