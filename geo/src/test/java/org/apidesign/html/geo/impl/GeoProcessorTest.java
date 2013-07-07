/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.geo.impl;

import java.io.IOException;
import org.testng.annotations.Test;

/** Test whether the annotation processor detects errors correctly.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class GeoProcessorTest {
    
    public GeoProcessorTest() {
    }

    @Test public void onLocationMethodHasToTakePositionParameter() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation\n"
            + "  public static void cantCallMe() {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("one net.java.html.geo.Position argument");
    }
    
    @Test public void onLocationMethodCannotBePrivate() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation\n"
            + "  private static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("cannot be private");
    }
    
    @Test public void onErrorHasToExist() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation(onError=\"doesNotExist\")\n"
            + "  static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("not find doesNotExist");
    }

    @Test public void onErrorWouldHaveToBeStatic() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation(onError=\"notStatic\")\n"
            + "  static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "  void notStatic(Exception e) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("have to be static");
    }

    @Test public void onErrorMustAcceptExceptionArgument() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation(onError=\"notStatic\")\n"
            + "  static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "  static void notStatic(java.io.IOException e) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("take one Exception arg");
    }
    
}
