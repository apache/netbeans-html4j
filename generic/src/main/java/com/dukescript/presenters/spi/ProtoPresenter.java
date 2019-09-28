package com.dukescript.presenters.spi;

import java.io.Flushable;
import org.netbeans.html.boot.spi.Fn;

/*
 * #%L
 * DukeScript Generic Presenter - a library from the "DukeScript Presenters" project.
 *
 * Dukehoff GmbH designates this particular file as subject to the "Classpath"
 * exception as provided in the README.md file that accompanies this code.
 * %%
 * Copyright (C) 2015 - 2019 Dukehoff GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/** The <em>prototypical</em> presenter. An implementation of a {@link Fn.Presenter} based on
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
