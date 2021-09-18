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
package org.netbeans.html.geo.impl;

import java.io.IOException;
import org.testng.annotations.Test;

/** Test whether the annotation processor detects errors correctly.
 *
 * @author Jaroslav Tulach
 */
public class GeoProcessorTest {
    
    public GeoProcessorTest() {
    }

    @Test public void onLocationMethodHasToTakePositionParameter() throws IOException {
        Compile res = Compile.create("", """
                                         package x;
                                         class UseOnLocation {
                                           @net.java.html.geo.OnLocation
                                           public static void cantCallMe() {}
                                         }
                                         """);
        res.assertErrors();
        res.assertError("first argument must be net.java.html.geo.Position");
    }
    
    @Test public void onLocationMethodCannotBePrivate() throws IOException {
        Compile res = Compile.create("", """
                                         package x;
                                         class UseOnLocation {
                                           @net.java.html.geo.OnLocation
                                           private static void cantCallMe(net.java.html.geo.Position p) {}
                                         }
                                         """);
        res.assertErrors();
        res.assertError("cannot be private");
    }
    
    @Test public void onErrorHasToExist() throws IOException {
        Compile res = Compile.create("", """
                                         package x;
                                         class UseOnLocation {
                                           @net.java.html.geo.OnLocation(onError="doesNotExist")
                                           static void cantCallMe(net.java.html.geo.Position p) {}
                                         }
                                         """);
        res.assertErrors();
        res.assertError("not find doesNotExist");
    }

    @Test public void onErrorWouldHaveToBeStatic() throws IOException {
        Compile res = Compile.create("", """
                                         package x;
                                         class UseOnLocation {
                                           @net.java.html.geo.OnLocation(onError="notStatic")
                                           static void cantCallMe(net.java.html.geo.Position p) {}
                                           void notStatic(Exception e) {}
                                         }
                                         """);
        res.assertErrors();
        res.assertError("have to be static");
    }

    @Test public void onErrorMustAcceptExceptionArgument() throws IOException {
        Compile res = Compile.create("", """
                                         package x;
                                         class UseOnLocation {
                                           @net.java.html.geo.OnLocation(onError="notStatic")
                                           static void cantCallMe(net.java.html.geo.Position p) {}
                                           static void notStatic(java.io.IOException e) {}
                                         }
                                         """);
        res.assertErrors();
        res.assertError("Error method first argument needs to be Exception");
    }
    
}
