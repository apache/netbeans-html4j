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
package net.java.html.boot;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apidesign.html.boot.impl.FnUtils;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.boot.impl.FindResources;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class BrowserBuilder {
    private static final Logger LOG = Logger.getLogger(BrowserBuilder.class.getName());
    
    private String resource;
    private Class<?> clazz;
    private Class[] browserClass;
    private Runnable onLoad;
    private String methodName;
    private String[] methodArgs;
    
    private BrowserBuilder() {
    }
    
    public static BrowserBuilder newBrowser() {
        return new BrowserBuilder();
    }
    
    public BrowserBuilder loadPage(String resource) {
        this.resource = resource;
        return this;
    }
    
    public BrowserBuilder loadClass(Class<?> mainClass) {
        this.clazz = mainClass;
        return this;
    }

    // invoked on browser thread
    public BrowserBuilder invoke(String methodName, String... args) {
        this.methodName = methodName;
        this.methodArgs = args;
        return this;
    }
    
    public void showAndWait() {
        if (resource == null) {
            throw new IllegalStateException("Need to specify resource via loadPage method");
        }
        
        FImpl impl = new FImpl(clazz.getClassLoader());
        URL url = null;
        MalformedURLException mal = null;
        try {
            url = new URL(resource);
        } catch (MalformedURLException ex) {
            mal = ex;
        }
        if (url == null) {
            url = clazz.getResource(resource);
        }
        if (url == null) {
            IllegalStateException ise = new IllegalStateException("Can't find resouce: " + resource + " relative to " + clazz);
            if (mal != null) {
                ise.initCause(mal);
            }
            throw ise;
        }

        for (Fn.Presenter dfnr : ServiceLoader.load(Fn.Presenter.class)) {
            final ClassLoader loader = FnUtils.newLoader(impl, dfnr, clazz.getClassLoader().getParent());

            class OnPageLoad implements Runnable {
                @Override
                public void run() {
                    try {
                        Class<?> newClazz = Class.forName(clazz.getName(), true, loader);
                        if (browserClass != null) {
                            browserClass[0] = newClazz;
                        }
                        if (onLoad != null) {
                            onLoad.run();
                        }
                        INIT: if (methodName != null) {
                            Exception firstError = null;
                            if (methodArgs.length == 0) {
                                try {
                                    Method m = newClazz.getMethod(methodName);
                                    m.invoke(null);
                                    break INIT;
                                } catch (Exception ex) {
                                    firstError = ex;
                                }
                            }
                            try {
                                Method m = newClazz.getMethod(methodName, String[].class);
                                m.invoke(m, (Object) methodArgs);
                            } catch (Exception ex) {
                                if (firstError != null) {
                                    LOG.log(Level.SEVERE, "Can't call " + methodName, firstError);
                                }
                                LOG.log(Level.SEVERE, "Can't call " + methodName + " with args " + Arrays.toString(methodArgs), ex);
                            }
                        }
                    } catch (ClassNotFoundException ex) {
                        LOG.log(Level.SEVERE, "Can't load " + clazz.getName(), ex);
                    }
                }
            }
            dfnr.displayPage(url, new OnPageLoad());
            return;
        }
        throw new IllegalStateException("Can't find any Fn.Definer");
    }

    private static final class FImpl implements FindResources {
        final ClassLoader l;

        public FImpl(ClassLoader l) {
            this.l = l;
        }

        @Override
        public void findResources(String path, Collection<? super URL> results, boolean oneIsEnough) {
            if (oneIsEnough) {
                URL u = l.getResource(path);
                if (u != null) {
                    results.add(u);
                }
            } else {
                try {
                    Enumeration<URL> en = l.getResources(path);
                    while (en.hasMoreElements()) {
                        results.add(en.nextElement());
                    }
                } catch (IOException ex) {
                    // no results
                }
            }
        }
        
    }
}
