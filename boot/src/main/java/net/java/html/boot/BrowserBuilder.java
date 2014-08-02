/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package net.java.html.boot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.BrwsrCtx;
import net.java.html.js.JavaScriptBody;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.context.spi.Contexts;
import org.netbeans.html.boot.impl.FindResources;
import org.netbeans.html.boot.impl.FnContext;
import org.netbeans.html.boot.impl.FnUtils;

/** Use this builder to launch your Java/HTML based application. Typical
 * usage in a main method of your application looks like this: 
 * <pre>
 * 
 * <b>public static void</b> <em>main</em>(String... args) {
 *     BrowserBuilder.{@link #newBrowser newBrowser()}.
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
 *   &lt;groupId&gt;org.netbeans.html&lt;/groupId&gt;
 *   &lt;artifactId&gt;net.java.html.boot.fx&lt;/artifactId&gt;
 *   &lt;scope&gt;runtime&lt;/scope&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Jaroslav Tulach
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
    private ClassLoader loader;
    
    private BrowserBuilder(Object[] context) {
        this.context = context;
    }

    /** Entry method to obtain a new browser builder. Follow by calling 
     * its instance methods like {@link #loadClass(java.lang.Class)} and
     * {@link #loadPage(java.lang.String)}.
     * 
     * @param context any instances that should be available to the builder -
     *   implementation dependant
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
    
    /** Allows one to specify a runnable that should be invoked when a load
     * of a page is finished. This method may be used in addition or instead
     * of {@link #loadClass(java.lang.Class)} and 
     * {@link #invoke(java.lang.String, java.lang.String...)} methods.
     * 
     * @param r the code to run when the page is loaded
     * @return this builder
     * @since 0.8.1
     */
    public BrowserBuilder loadFinished(Runnable r) {
        this.onLoad = r;
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

    /** Loader to use when searching for classes to initialize. 
     * If specified, this loader is going to be used to load {@link Fn.Presenter}
     * and {@link Contexts#fillInByProviders(java.lang.Class, org.apidesign.html.context.spi.Contexts.Builder) fill} {@link BrwsrCtx} in.
     * Specifying special classloader may be useful in modular systems, 
     * like OSGi, where one needs to load classes from many otherwise independent
     * modules.
     * 
     * @param l the loader to use (or <code>null</code>)
     * @return this builder
     * @since 0.9
     */
    public BrowserBuilder classloader(ClassLoader l) {
        this.loader = l;
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
        IOException mal = null;
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
        final Class<?> myCls;
        if (clazz != null) {
            myCls = clazz;
        } else if (onLoad != null) {
            myCls = onLoad.getClass();
        } else {
            throw new NullPointerException("loadClass, neither loadFinished was called!");
        }
        
        if (url == null) {
            url = myCls.getResource(resource);
        }
        if (url == null) {
            final ProtectionDomain pd = myCls.getProtectionDomain();
            if (pd != null && pd.getCodeSource() != null) {
                URL jar = pd.getCodeSource().getLocation();
                try {
                    url = new URL(jar, resource);
                } catch (MalformedURLException ex) {
                    ex.initCause(mal);
                    mal = ex;
                }
            }
        }
        if (url == null) {
            URL res = BrowserBuilder.class.getResource("html4j.txt");
            LOG.log(Level.FINE, "Found html4j {0}", res);
            if (res != null) try {
                URLConnection c = res.openConnection();
                LOG.log(Level.FINE, "testing : {0}", c);
                if (c instanceof JarURLConnection) {
                    JarURLConnection jc = (JarURLConnection)c;
                    URL base = jc.getJarFileURL();
                    for (int i = 0; i < 50; i++) {
                        URL u = new URL(base, resource);
                        try {
                            InputStream is = u.openStream();
                            is.close();
                            url = u;
                            LOG.log(Level.FINE, "found real url: {0}", url);
                            break;
                        } catch (FileNotFoundException ignore) {
                            LOG.log(Level.FINE, "Cannot open " + u, ignore);
                        }
                        base = new URL(base, "..");
                    }
                }
            } catch (IOException ex) {
                mal = ex;
            }
        }
        if (url == null) {
            IllegalStateException ise = new IllegalStateException("Can't find resouce: " + resource + " relative to " + myCls);
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

        if (dfnr == null && loader != null) for (Fn.Presenter o : ServiceLoader.load(Fn.Presenter.class, loader)) {
            dfnr = o;
            break;
        }
        
        if (dfnr == null) for (Fn.Presenter o : ServiceLoader.load(Fn.Presenter.class)) {
            dfnr = o;
            break;
        }
        
        if (dfnr == null) {
            throw new IllegalStateException("Can't find any Fn.Presenter");
        }
        
        final ClassLoader activeLoader;
        if (loader != null) {
            if (!FnUtils.isJavaScriptCapable(loader)) {
                throw new IllegalStateException("Loader cannot resolve @JavaScriptBody: " + loader);
            }
            activeLoader = loader;
        } else if (FnUtils.isJavaScriptCapable(myCls.getClassLoader())) {
            activeLoader = myCls.getClassLoader();
        } else {
            FImpl impl = new FImpl(myCls.getClassLoader());
            activeLoader = FnUtils.newLoader(impl, dfnr, myCls.getClassLoader().getParent());
        }
        
        final Fn.Presenter dP = dfnr;

        class OnPageLoad implements Runnable {
            @Override
            public void run() {
                try {
                    final Fn.Presenter aP = Fn.activePresenter();
                    final Fn.Presenter currentP = aP != null ? aP : dP;
                    
                    Thread.currentThread().setContextClassLoader(activeLoader);
                    final Class<?> newClazz = Class.forName(myCls.getName(), true, activeLoader);
                    if (browserClass != null) {
                        browserClass[0] = newClazz;
                    }
                    Contexts.Builder cb = Contexts.newBuilder();
                    if (!Contexts.fillInByProviders(newClazz, cb)) {
                        LOG.log(Level.WARNING, "Using empty technology for {0}", newClazz);
                    }
                    if (currentP instanceof Executor) {
                        cb.register(Executor.class, (Executor)currentP, 1000);
                    }
                    cb.register(Fn.Presenter.class, currentP, 1000);
                    BrwsrCtx c = cb.build();

                    class CallInitMethod implements Runnable {
                        @Override
                        public void run() {
                            Throwable firstError = null;
                            if (onLoad != null) {
                                try {
                                    FnContext.currentPresenter(currentP);
                                    onLoad.run();
                                } catch (Throwable ex) {
                                    firstError = ex;
                                } finally {
                                    FnContext.currentPresenter(null);
                                }
                            }
                            INIT: if (methodName != null) {
                                if (methodArgs.length == 0) {
                                    try {
                                        Method m = newClazz.getMethod(methodName);
                                        FnContext.currentPresenter(currentP);
                                        m.invoke(null);
                                        firstError = null;
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
                                    firstError = null;
                                } catch (Throwable ex) {
                                    LOG.log(Level.SEVERE, "Can't call " + methodName + " with args " + Arrays.toString(methodArgs), ex);
                                } finally {
                                    FnContext.currentPresenter(null);
                                }
                            }
                            if (firstError != null) {
                                LOG.log(Level.SEVERE, "Can't initialize the view", firstError);
                            }
                        }
                    }
                    
                    c.execute(new CallInitMethod());
                } catch (ClassNotFoundException ex) {
                    LOG.log(Level.SEVERE, "Can't load " + myCls.getName(), ex);
                }
            }
        }
        dfnr.displayPage(url, new OnPageLoad());
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
