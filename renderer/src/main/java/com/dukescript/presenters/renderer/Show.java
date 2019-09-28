package com.dukescript.presenters.renderer;

/*
 * #%L
 * Desktop Browser Renderer - a library from the "DukeScript Presenters" project.
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


import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.html.boot.spi.Fn;

/** Support for displaying browser.
 */
public abstract class Show {
    static final Logger LOG = Logger.getLogger(Show.class.getName());

    Show() {
    }
    
    /** Shows a page in a browser.
     * 
     * @param impl the name of implementation to use, can be <code>null</code>
     * @param page the page URL
     * @throws IOException if something goes wrong
     */
    public static void show(String impl, URI page) throws IOException {
        try {
            Class<?> c = Class.forName("com.dukescript.presenters.renderer." + impl);
            Show show = (Show) c.newInstance();
            show.show(page);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            if (impl == null) {
                impl = "xdg-open";
            }
            LOG.log(Level.INFO, "Trying command line execution of {0}", impl);
            String[] cmdArr = {
                impl, page.toString()
            };
            LOG.log(Level.INFO, "Launching {0}", Arrays.toString(cmdArr));
            final Process process = Runtime.getRuntime().exec(cmdArr);
            try {
                process.waitFor();
            } catch (InterruptedException ex1) {
                throw (InterruptedIOException) new InterruptedIOException().initCause(ex1);
            }
        }
    }
    
    /** Initializes native browser window.
     * 
     * @param presenter the presenter that will be using the returned value
     * @param onPageLoad callback when page finishes loading
     * @param onContext callback when {@link #jsContext()} becomes available
     * @param headless should the window appear on the monitor or not?
     *   useful for testing
     * @return object to query and control the browser window
     */
    public static Show open(Fn.Presenter presenter, Runnable onPageLoad, Runnable onContext, boolean headless) {
        boolean isMac = System.getProperty("os.name").contains("Mac");
        return isMac ?
            new Cocoa(presenter, onPageLoad, onContext, headless) :
            new GTK(presenter, onPageLoad, onContext, headless);
    }

    /** Loads a page into the browser window.
     * 
     * @param page the URL to load
     * @throws IOException if something goes wrong
     */
    public abstract void show(URI page) throws IOException;
    
    /** Access to JavaScriptCore API of the browser window.
     * @return JavaScriptCore instance or <code>null</code> if not supported
     *   for this browser
     */
    public abstract JSC jsc();

    /** Access to JavaScriptCore context.
     * @return the context or <code>null</code> if not supported or not 
     *   yet available
     */
    public abstract Pointer jsContext();
    
    /** Executes a runnable on "UI thread".
     * @param command runnable to execute
     */
    public abstract void execute(Runnable command);
}
