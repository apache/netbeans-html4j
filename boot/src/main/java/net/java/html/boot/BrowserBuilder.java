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

import java.io.File;
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
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.boot.impl.FnUtils;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.boot.impl.FindResources;
import org.apidesign.html.boot.impl.FnContext;

/** Use this builder to launch your Java/HTML based application. Typical
 * usage in a main method of your application looks like this: 
 * <pre>
 * 
 * <b>public static void</b> <em>main</em>(String... args) {
 *     BrowserBuilder.{@link #newBrowser}.
 *          {@link #loadClass(java.lang.Class) loadClass(YourMain.class)}.
 *          {@link #loadPage(java.lang.String) loadPage("index.html")}.
 *          {@link #invoke(java.lang.String, java.lang.String[]) invoke("initialized", args)}.
 *          {@link #showAndWait()};
 *     System.exit(0);
 * }
 * </pre>
 * The above will load <code>YourMain</code> class via
 * a special classloader, it will locate an <code>index.html</code> (relative
 * to <code>YourMain</code> class) and show it in a browser window. When the
 * initialization is over, a <b>public static</b> method <em>initialized</em>
 * in <code>YourMain</code> will be called with provided string parameters.
 * <p>
 * This module provides only API for building browsers. To use it properly one
 * also needs an implementation on the classpath of one's application. For example
 * use: <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;org.apidesign.html&lt;/groupId&gt;
 *   &lt;artifactId&gt;boot-fx&lt;/artifactId&gt;
 *   &lt;scope&gt;runtime&lt;/scope&gt;
 * &lt;/dependency&gt;
 * </pre>
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
    private final Object[] context;
    
    private BrowserBuilder(Object[] context) {
        this.context = context;
    }

    /** Entry method to obtain a new browser builder. Follow by calling 
     * its instance methods like {@link #loadClass(java.lang.Class)} and
     * {@link #loadPage(java.lang.String)}.
     * 
     * @param context any instances that should be available to the builder -
     *   implemenation dependant
     * @return new browser builder
     */
    public static BrowserBuilder newBrowser(Object... context) {
        return new BrowserBuilder(context);
    }
    
    /** The class to load when the browser is initialized. This class
     * is loaded by a special classloader (that supports {@link JavaScriptBody}
     * and co.). 
     * 
     * @param mainClass the class to load and resolve when the browser is ready
     * @return this builder
     */
    public BrowserBuilder loadClass(Class<?> mainClass) {
        this.clazz = mainClass;
        return this;
    }

    /** Page to load into the browser. If the <code>page</code> represents
     * a {@link URL} known to the Java system, the URL is passed to the browser. 
     * If system property <code>browser.rootdir</code> is specified, then a
     * file <code>page</code> relative to this directory is used as the URL.
     * If no such file exists, the system seeks for the 
     * resource via {@link Class#getResource(java.lang.String)}
     * method (relative to the {@link #loadClass(java.lang.Class) specified class}). 
     * If such resource is not found, a file relative to the location JAR
     * that contains the {@link #loadClass(java.lang.Class) main class} is 
     * searched for.
     * 
     * @param page the location (relative, absolute, or URL) of a page to load
     * @return this browser
     */
    public BrowserBuilder loadPage(String page) {
        this.resource = page;
        return this;
    }
    
    /** Specifies callback method to notify the application that the browser is ready.
     * There should be a <b>public static</b> method in the class specified
     * by {@link #loadClass(java.lang.Class)} which takes an array of {@link String}
     * argument. The method is called on the browser dispatch thread one
     * the browser finishes loading of the {@link #loadPage(java.lang.String) HTML page}.
     * 
     * @param methodName name of a method to seek for
     * @param args parameters to pass to the method
     * @return this builder
     */
    public BrowserBuilder invoke(String methodName, String... args) {
        this.methodName = methodName;
        this.methodArgs = args;
        return this;
    }

    /** Shows the browser, loads specified page in and executes the 
     * {@link #invoke(java.lang.String, java.lang.String[]) initialization method}.
     * The method returns when the browser is closed.
     * 
     * @throws NullPointerException if some of essential parameters (like {@link #loadPage(java.lang.String) page} or
     *    {@link #loadClass(java.lang.Class) class} have not been specified
     */
    public void showAndWait() {
        if (resource == null) {
            throw new NullPointerException("Need to specify resource via loadPage method");
        }
        
        URL url = null;
        MalformedURLException mal = null;
        try {
            String baseURL = System.getProperty("browser.rootdir");
            if (baseURL != null) {
                url = new File(baseURL, resource).toURI().toURL();
            } else {
                url = new URL(resource);
            }
        } catch (MalformedURLException ex) {
            mal = ex;
        }
        if (url == null) {
            url = clazz.getResource(resource);
        }
        if (url == null) {
            URL jar = clazz.getProtectionDomain().getCodeSource().getLocation();
            try {
                url = new URL(jar, resource);
            } catch (MalformedURLException ex) {
                ex.initCause(mal);
                mal = ex;
            }
        }
        if (url == null) {
            IllegalStateException ise = new IllegalStateException("Can't find resouce: " + resource + " relative to " + clazz);
            if (mal != null) {
                ise.initCause(mal);
            }
            throw ise;
        }
        
        Fn.Presenter dfnr = null;
        for (Object o : context) {
            if (o instanceof Fn.Presenter) {
                dfnr = (Fn.Presenter)o;
                break;
            }
        }

        if (dfnr == null) for (Fn.Presenter o : ServiceLoader.load(Fn.Presenter.class)) {
            dfnr = o;
            break;
        }
        
        if (dfnr == null) {
            throw new IllegalStateException("Can't find any Fn.Presenter");
        }
        
        final ClassLoader loader;
        if (FnUtils.isJavaScriptCapable(clazz.getClassLoader())) {
            loader = clazz.getClassLoader();
        } else {
            FImpl impl = new FImpl(clazz.getClassLoader());
            loader = FnUtils.newLoader(impl, dfnr, clazz.getClassLoader().getParent());
        }

        final Fn.Presenter currentP = dfnr;
        class OnPageLoad implements Runnable {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(loader);
                    Class<?> newClazz = Class.forName(clazz.getName(), true, loader);
                    if (browserClass != null) {
                        browserClass[0] = newClazz;
                    }
                    if (onLoad != null) {
                        onLoad.run();
                    }
                    INIT: if (methodName != null) {
                        Throwable firstError = null;
                        if (methodArgs.length == 0) {
                            try {
                                Method m = newClazz.getMethod(methodName);
                                FnContext.currentPresenter(currentP);
                                m.invoke(null);
                                break INIT;
                            } catch (Throwable ex) {
                                firstError = ex;
                            } finally {
                                FnContext.currentPresenter(null);
                            }
                        }
                        try {
                            Method m = newClazz.getMethod(methodName, String[].class);
                            FnContext.currentPresenter(currentP);
                            m.invoke(m, (Object) methodArgs);
                        } catch (Throwable ex) {
                            if (firstError != null) {
                                LOG.log(Level.SEVERE, "Can't call " + methodName, firstError);
                            }
                            LOG.log(Level.SEVERE, "Can't call " + methodName + " with args " + Arrays.toString(methodArgs), ex);
                        } finally {
                            FnContext.currentPresenter(null);
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
