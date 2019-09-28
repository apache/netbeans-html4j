package com.dukescript.presenters.spi;

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


import com.dukescript.presenters.spi.ProtoPresenterBuilder;
import com.dukescript.presenters.spi.Level;
import com.dukescript.presenters.spi.Generic;
import java.net.URL;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueOfTest {
    private Generic p;
    @BeforeMethod public void initInstance() {
        p = new Generic(true, true, "type", "app") {
            @Override
            void log(Level level, String msg, Object... args) {
            }

            @Override
            void callbackFn(ProtoPresenterBuilder.OnPrepared onReady) {
            }

            @Override
            void loadJS(String js) {
            }

            @Override
            void dispatch(Runnable r) {
            }

            @Override
            public void displayPage(URL url, Runnable r) {
            }
        };
    }
    
    
    @Test public void parseSimpleArray() {
        Object res = p.valueOf("array:1:8:number:6");
        assertTrue(res instanceof Object[], "It is an array: " + res);
        Object[] arr = (Object[]) res;
        assertEquals(arr.length, 1, "One array item");
        assertEquals(arr[0], 6.0, "Value is six");
    }
}
