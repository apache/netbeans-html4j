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


import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITest;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public final class Case implements ITest, IHookable, Runnable {
    private final Fn.Presenter p;
    private CountDownLatch finished;
    private final Method m;
    private Object result;
    private Object inst;
    private int cnt;

    Case(Fn.Presenter p, Method m) {
        this.p = p;
        this.m = m;
    }

    @Override
    public String getTestName() {
        return m.getDeclaringClass().getSimpleName() + "." + m.getName();
    }

    @Test
    public void executeTest() throws Exception {
        for (;;) {
            if (p instanceof Executor) {
                finished = new CountDownLatch(1);
                ((Executor)p).execute(this);
                finished.await();
            } else {
                run();
            }
            if (result instanceof InterruptedException && cnt++ < 100) {
                Thread.sleep(100);
                result = null;
                continue;
            }
            break;
        }
        if (result instanceof Exception) {
            throw (Exception) result;
        } else if (result instanceof Error) {
            throw (Error) result;
        }
    }
    
    @Override
    public void run() {
        Closeable c = Fn.activate(p);
        try {
            if (p instanceof Testing) {
                Testing tp = (Testing) p;
                tp.beforeTest(m.getDeclaringClass());
            }
            if (inst == null) {
                inst = m.getDeclaringClass().newInstance();
            }
            result = m.invoke(inst);
            if (result == null) {
                result = this;
            }
        } catch (InvocationTargetException ex) {
            Throwable r = ex.getTargetException();
            result = r;
        } catch (Exception ex) {
            result = ex;
        } finally {
            try {
                c.close();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            if (finished != null) {
                finished.countDown();
            }
        }
    }

    @Override
    public void run(IHookCallBack ihcb, ITestResult itr) {
        ihcb.runTestMethod(itr);
    }
    
}
