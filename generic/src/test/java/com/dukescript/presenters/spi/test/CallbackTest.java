package com.dukescript.presenters.spi.test;

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


import java.lang.reflect.Method;
import java.util.logging.Level;
import javax.script.ScriptException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;

public class CallbackTest {
    @Factory public static Object[] deadlockTests() throws Exception {
        return GenericTest.createTests(new CBP());
    }
    
    @AfterClass public static void countCallbacks() {
        assertEquals(Counter.callbacks, Counter.calls, "Every call to loadJS is prefixed with a callback");
    }
    
    
    private static final class CBP extends Testing {

        @Override
        protected void loadJS(String js) {
            dispatch(new Runnable () {
                @Override
                public void run() {
                    try {
                        Object res = eng.eval("if (this.counter) this.counter()");
                        LOG.log(Level.FINE, "counter res: {0}", res);
                    } catch (ScriptException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            });
            super.loadJS(js);
        }

        @Override void beforeTest(Class<?> testClass) throws Exception {
            Class<?> cntr = testClass.getClassLoader().loadClass(Counter.class.getName());
            Method rc = cntr.getMethod("registerCounter");
            rc.invoke(null);
        }
        
    }
}
