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
package org.netbeans.html.json.spi;

import java.io.IOException;
import java.io.InputStream;
import org.netbeans.html.context.spi.Contexts.Builder;
import org.netbeans.html.context.spi.Contexts.Id;

/** A {@link Builder service provider interface} responsible for 
 * conversion of JSON objects to Java ones and vice-versa.
 * Since introduction of {@link Id technology identifiers} one can choose between
 * different background implementations to handle the conversion and
 * communication requests. The known providers include
 * <code>org.netbeans.html:ko4j</code> module which registers 
 * a native browser implementation called <b>xhr</b>, and a
 * <code>org.netbeans.html:ko-ws-tyrus</code> module which registers 
 * Java based implementation named <b>tyrus</b>.
 *
 * @author Jaroslav Tulach
 */
public interface Transfer {
    /**
     * Called to inspect properties of an object (usually a JSON or JavaScript
     * wrapper).
     *
     * @param obj the object to inspect
     * @param props the names of properties to check on the object
     * <code>obj</code>
     * @param values array of the same length as <code>props</code> should be
     * filled by values of properties on the <code>obj</code>. If a property is
     * not defined, a <code>null</code> value should be stored in the array
     */
    public void extract(Object obj, String[] props, Object[] values);
    
    /** Reads content of a stream and creates its JSON representation.
     * The returned object is implementation dependant. It however needs
     * to be acceptable as first argument of {@link #extract(java.lang.Object, java.lang.String[], java.lang.Object[]) extract}
     * method. If the stream contains representation or a JSON array,
     * an Object[] should be returned - each of its members should, by itself
     * be acceptable argument to 
     * the {@link #extract(java.lang.Object, java.lang.String[], java.lang.Object[]) extract} method.
     * 
     * @param is input stream to read data from
     * @return an object representing the JSON data
     * @throws IOException if something goes wrong
     */
    public Object toJSON(InputStream is) throws IOException;
    
    /** Starts the JSON or JSONP query. 
     * 
     * @param call description of the call to make
     */
    public void loadJSON(JSONCall call);
    
}
