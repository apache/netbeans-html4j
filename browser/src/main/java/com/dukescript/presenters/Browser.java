package com.dukescript.presenters;

/*
 * #%L
 * DukeScript Presenter for any Browser - a library from the "DukeScript Presenters" project.
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


import com.dukescript.presenters.renderer.Show;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;
import com.dukescript.presenters.spi.ProtoPresenter;
import com.dukescript.presenters.spi.ProtoPresenterBuilder;
import org.openide.util.lookup.ServiceProvider;

/** Browser based {@link Presenter}. It starts local server and
 * launches browser that connects to it. The actual browser to
 * be launched can be influenced by value of 
 * <code>com.dukescript.presenters.browser</code> property.
 * It can have following values:
 * <ul>
 * <li><b>GTK</b> - use Gtk WebKit implementation. Requires presence of 
 *    appropriate native libraries</li>
 * <li><b>AWT</b> - use {@link java.awt.Desktop#browse(java.net.URI)} to
 *    launch a browser</li>
 * <li><b>NONE</b> - just launches the server, useful together with
 *    <code>com.dukescript.presenters.browserPort</code> property that
 *    can specify a fixed port to open the server at
 * </li>
 * <li>any other value is interpreted as a command which is then
 *    launched on a command line with one parameter - the URL to connect to</li>
 * </ul>
 * If the property is not specified the system tries <b>GTK</b> mode first, 
 * followed by <b>AWT</b> and then tries to execute <code>xdg-open</code>
 * (default LINUX command to launch a browser from a shell script).
 * <p>
 * To use this presenter specify following dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;com.dukescript.presenters&lt;/groupId&gt;
 *   &lt;artifactId&gt;browser&lt;/artifactId&gt;
 *   &lt;version&gt;<a target="blank" href="http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.dukescript.presenters%22%20AND%20a%3A%22browser%22">1.x</a>&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
@ServiceProvider(service = Presenter.class)
public final class Browser implements Fn.Presenter, Fn.KeepAlive, Flushable,
Executor, Closeable {
    static final Logger LOG = Logger.getLogger(Browser.class.getName());
    private final Map<String,Command> SESSIONS = new HashMap<String, Command>();
    private final String app;
    private HttpServer s;
    private Runnable onPageLoad;
    private Command current;
    private final Config config;
    
    public Browser() throws Exception {
        this(new Config());
    }

    public Browser(Config config) {
        this(findCalleeClassName(), config);
    }
    
    Browser(String app, Config config) {
        this.app = app;
        this.config = new Config(config);
    }

    @Override
    public final void execute(final Runnable r) {
        current.runSafe(r, true);
    }

    @Override
    public void close() throws IOException {
        s.shutdownNow();
    }

    HttpServer server() {
        return s;
    }

    static HttpServer findServer(Object obj) {
        Command c = null;
        if (obj instanceof Command) {
            c = (Command) obj;
        } else if (obj instanceof ProtoPresenter) {
            c = ((ProtoPresenter) obj).lookup(Command.class);
        }
        return c.browser.server();
    }

    /** Shows URL in a browser.
     * @param page the page to display in the browser
     * @throws IOException if something goes wrong
     */
    void show(URI page) throws IOException {
        String impl = config.getBrowser();
        if ("none".equalsIgnoreCase(impl)) { // NOI18N
            return;
        }
        if (impl != null) {
            Show.show(impl, page);
        } else {
            IOException one, two;
            try {
                String ui = System.getProperty("os.name").contains("Mac") ?
                    "Cocoa" : "GTK";
                Show.show(ui, page);
                return;
            } catch (IOException ex) {
                one = ex;
            }
            try {
                Show.show("AWT", page);
                return;
            } catch (IOException ex) {
                two = ex;
            }
            try {
                Show.show(impl, page);
            } catch (IOException ex) {
                two.initCause(one);
                ex.initCause(two);
                throw ex;
            }
        }
    }

    @Override
    public Fn defineFn(String string, String... strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadScript(Reader reader) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public Fn defineFn(String string, String[] strings, boolean[] blns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    private static HttpServer server(RootPage r, Config config) {
        int from = 8080;
        int to = 65535;
        int port = config.getPort();
        if (port != -1) {
            from = to = port;
        }
        HttpServer s = HttpServer.createSimpleServer(null, new PortRange(from, to));
        final ServerConfiguration conf = s.getServerConfiguration();
        conf.addHttpHandler(r, "/");
        return s;
    }
    
    private static URI pageURL(String protocol, HttpServer server, final String page) {
        NetworkListener listener = server.getListeners().iterator().next();
        int port = listener.getPort();
        try {
            return new URI(protocol + "://localhost:" + port + page);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public final void displayPage(URL page, Runnable onPageLoad) {
        try {
            this.onPageLoad = onPageLoad;
            s = server(new RootPage(page), config);
            s.start();
            show(pageURL("http", s, "/"));
        } catch (IOException ex) {
            Logger.getLogger(Browser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** Parameters to configure {@link Browser}.
     * Create an instance and pass it 
     * to {@link Browser#Browser(com.dukescript.presenters.Browser.Config) }
     * constructor.
     */
    public final static class Config {
        String browser;
        Integer port;

        public Config() {
        }

        private Config(Config copy) {
            this.browser = copy.browser;
            this.port = copy.port;
        }

        /** The command to use when invoking a browser. Possible values:
         * <ul>
         * <li>
         *   <b>GTK</b> - use Gtk WebKit implementation. Requires presence of appropriate native libraries
         * </li>
         * <li>
         *   <b>AWT</b> - use Desktop.browse(java.net.URI) to launch a browser
         * </li>
         * <li>
         *   <b>NONE</b> - just launches the server, useful together with {@link #port(int)} to specify a fixed port to open the server at
         * </li>
         * <li>
         * any other value is interpreted as a command which is then launched on a command line with one parameter - the URL to connect to
         * </li>
         * </ul>
         *
         * @param executable browser to execute
         * @return this instance
         */
        public Config command(String executable) {
            this.browser = executable;
            return this;
        }

        /** The port to start the server at.
         * By default a random port is selected.
         * @param port the port
         * @return this instance
         */
        public Config port(int port) {
            this.port = port;
            return this;
        }
        
        final String getBrowser() {
            if (browser != null) {
                return browser;
            }
            return System.getProperty("com.dukescript.presenters.browser"); // NOI18N
        }

        final int getPort() {
            if (port != null) {
                return port;
            }
            String port = System.getProperty("com.dukescript.presenters.browserPort"); // NOI18N
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
    }
    
    static void cors(Response r) {
        r.setCharacterEncoding("UTF-8");
        r.addHeader("Access-Control-Allow-Origin", "*");
        r.addHeader("Access-Control-Allow-Credentials", "true");
        r.addHeader("Access-Control-Allow-Headers", "Content-Type");
        r.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
    }

    private final class RootPage extends HttpHandler {
        private final URL page;

        public RootPage(URL page) {
            this.page = page;
        }
        
        @Override
        public void service(Request rqst, Response rspns) throws Exception {
            String path = rqst.getRequestURI();
            cors(rspns);
            if ("/".equals(path) || "index.html".equals(path)) {
                Reader is;
                String prefix = "http://" + rqst.getServerName() + ":" + rqst.getServerPort() + "/";
                Writer w = rspns.getWriter();
                rspns.setContentType("text/html");
                final Command cmd = new Command(Browser.this, prefix);
                try {
                    is = new InputStreamReader(page.openStream());
                } catch (IOException ex) {
                    w.write("<html><body>");
                    w.write("<h1>Browser</h1>");
                    w.write("<pre id='cmd'></pre>");
                    emitScript(w, prefix, cmd.id);
                    w.write("</body></html>");
                    w.close();
                    return;
                }
                SESSIONS.put(cmd.id, cmd);
                int state = 0;
                for (;;) {
                    int ch = is.read();
                    if (ch == -1) {
                        break;
                    }
                    char lower = Character.toLowerCase((char)ch);
                    switch (state) {
                        case 1000: break;
                        case 0: if (lower == '<') state = 1; break;
                        case 1: if (lower == 'b') state = 2;
                            else if (lower != ' ' && lower != '\n') state = 0;
                            break;
                        case 2: if (lower == 'o') state = 3; else state = 0; break;
                        case 3: if (lower == 'd') state = 4; else state = 0; break;
                        case 4: if (lower == 'y') state = 5; else state = 0; break;
                        case 5: if (lower == '>') state = 500;
                            else if (lower != ' ' && lower != '\n') state = 0;
                            break;
                    }
                    w.write((char)ch);
                    if (state == 500) {
                        emitScript(w, prefix, cmd.id);
                        state = 1000;
                    }
                }
                if (state != 1000) {
                    emitScript(w, prefix, cmd.id);
                }
                is.close();
                w.close();
            } else if (path.equals("/command.js")) {
                String id = rqst.getParameter("id");
                Command c = SESSIONS.get(id);
                if (c == null) {
                    rspns.getOutputBuffer().write("No command for " + id);
                    rspns.setStatus(HttpStatus.NOT_FOUND_404);
                    return;
                }
                c.service(rqst, rspns);
            } else {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                URL relative = new URL(page, path);
                InputStream is;
                try {
                    is = relative.openStream();
                } catch (FileNotFoundException ex) {
                    rspns.setStatus(HttpStatus.NOT_FOUND_404);
                    return;
                }
                OutputStream out = rspns.getOutputStream();
                for (;;) {
                    int b = is.read();
                    if (b == -1) {
                        break;
                    }
                    out.write(b);
                }
                out.close();
                is.close();
            }
        }

        private void emitScript(Writer w, String prefix, String id) throws IOException {
            w.write("  <script id='exec' type='text/javascript'>");
            w.write("\n"
                    + "function waitForCommand() {\n"
                    + "  try {\n"
                    + "    if (waitForCommand.seenError) {\n"
                    + "      console.warn('Disconnected from " + prefix + "');\n"
                    + "      return;\n"
                    + "    };\n"
                    + "    var request = new XMLHttpRequest();\n"
                    + "    request.open('GET', '" + prefix + "command.js?id=" + id + "', true);\n"
                    + "    request.setRequestHeader('Content-Type', 'text/plain; charset=utf-8');\n"
                    + "    request.onerror = function(ev) {\n"
                    + "      console.warn(ev);\n"
                    + "      waitForCommand.seenError = true;\n"
                    + "    };\n"
                    + "    request.onreadystatechange = function() {\n"
                    + "      if (this.readyState!==4) return;\n"
                    + "      try {\n"
                    + "        var cmd = document.getElementById('cmd');\n"
                    + "        if (cmd) cmd.innerHTML = this.responseText.substring(0,80);\n"
                    + "        (0 || eval)(this.responseText);\n"
                    + "      } catch (e) {\n"
                    + "        console.warn(e); \n"
                    + "      } finally {\n"
                    + "        waitForCommand();\n"
                    + "      }\n"
                    + "    };\n"
                    + "    request.send();\n"
                    + "  } catch (e) {\n"
                    + "    console.warn(e);\n"
                    + "    waitForCommand();\n"
                    + "  }\n"
                    + "}\n"
                    + "waitForCommand();\n"
            );
            w.write("  </script>\n");
        }
    }

    private static String findCalleeClassName() {
        StackTraceElement[] frames = new Exception().getStackTrace();
        for (StackTraceElement e : frames) {
            String cn = e.getClassName();
            if (cn.startsWith("com.dukescript.presenters.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("org.netbeans.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("net.java.html.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("java.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("javafx.")) { // NOI18N
                continue;
            }
            if (cn.startsWith("com.sun.")) { // NOI18N
                continue;
            }
            return cn;
        }
        return "org.netbeans.html"; // NOI18N
    }
    
    private static final class Command extends Object
    implements Executor, ThreadFactory {
        private final Queue<Object> exec;
        private final Browser browser;
        private final String id;
        private final String prefix;
        private final Executor RUN;
        private Thread RUNNER;
        private Response suspended;
        private boolean initialized;
        private final ProtoPresenter presenter;

        Command(Browser browser, String prefix) {
            this.RUN = Executors.newSingleThreadExecutor(this);
            this.id = UUID.randomUUID().toString();
            this.exec = new LinkedList<>();
            this.prefix = prefix;
            this.browser = browser;
            this.presenter = ProtoPresenterBuilder.newBuilder().
                preparator(this::callbackFn, true).
                loadJavaScript(this::loadJS, false).
                app(browser.app).
                dispatcher(this, true).
                displayer(this::displayPage).
                logger(this::log).
                type("Browser").
                register(this).
                build();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Processor for " + id);
            RUNNER = t;
            return t;
        }

        @Override
        public final void execute(final Runnable r) {
            runSafe(r, true);
        }
        
        final void runSafe(final Runnable r, final boolean context) {
            class Wrap implements Runnable {
                @Override
                public void run() {
                    if (context) {
                        Closeable c = Fn.activate(Command.this.presenter);
                        try {
                            r.run();
                        } finally {
                            try {
                                c.close();
                            } catch (IOException ex) {
                                // ignore
                            }
                        }
                    } else {
                        r.run();
                    }
                }
            }
            if (RUNNER == Thread.currentThread()) {
                if (context) {
                    Runnable w = new Wrap();
                    w.run();
                } else {
                    r.run();
                }
            } else {
                Runnable w = new Wrap();
                RUN.execute(w);
            }
        }
        
        final synchronized void add(Object obj) {
            if (suspended != null) {
                try {
                    suspended.getWriter().write(obj.toString());
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                suspended.resume();
                suspended = null;
                return;
            }
            exec.add(obj);
        }
        
        private synchronized Object take(Response rspns) {
            Object o = exec.poll();
            if (o != null) {
                return o;
            }
            suspended = rspns;
            rspns.suspend();
            return null;
        }
        
        void service(Request rqst, Response rspns) throws Exception {
            final String methodName = rqst.getParameter("name");
            Writer w = rspns.getWriter();
            if (methodName == null) {
                if (!initialized) {
                    initialized = true;
                    execute(browser.onPageLoad);
                }
                // send new request
                Object obj = take(rspns);
                if (obj == null) {
                    LOG.log(Level.FINE, "Suspending response {0}", rspns);
                    return;
                }
                final String s = obj.toString();
                w.write(s);
                LOG.log(Level.FINE, "Exec global: {0}", s);
            } else {
                List<String> args = new ArrayList<String>();
                for (;;) {
                    String p = rqst.getParameter("p" + args.size());
                    if (p == null) {
                        break;
                    }
                    args.add(p);
                }
                String res;
                try {
                    LOG.log(Level.FINE, "Call {0}", methodName + " with " + args);
                    res = presenter.js2java(methodName,
                        args.get(0), args.get(1), args.get(2), args.get(3)
                    );
                    LOG.log(Level.FINE, "Result: {0}", res);
                } catch (Exception ex) {
                    res = "error:" + ex.getMessage();
                }
                if (res != null) {
                    w.write(res);
                } else {
                    w.write("null");
                }
            }
            w.close();
        }

        void callbackFn(ProtoPresenterBuilder.OnPrepared onReady) {
            StringBuilder sb = new StringBuilder();
            sb.append("this.toBrwsrSrvr = function(name, a1, a2, a3, a4) {\n"
                + "var url = '").append(prefix).append("command.js?id=").append(id).append("&name=' + name;\n"
                + "url += '&p0=' + encodeURIComponent(a1);\n"
                + "url += '&p1=' + encodeURIComponent(a2);\n"
                + "url += '&p2=' + encodeURIComponent(a3);\n"
                + "url += '&p3=' + encodeURIComponent(a4);\n"
                + "var request = new XMLHttpRequest();\n"
                + "request.open('GET', url, false);\n"
                + "request.setRequestHeader('Content-Type', 'text/plain; charset=utf-8');\n"
                + "request.send();\n"
                + "return request.responseText;\n"
                + "};\n");
            add(sb);
            onReady.callbackIsPrepared("toBrwsrSrvr");
        }

        private static Level findLevel(int priority) {
            if (priority >= Level.SEVERE.intValue()) {
                return Level.SEVERE;
            }
            if (priority >= Level.WARNING.intValue()) {
                return Level.WARNING;
            }
            if (priority >= Level.INFO.intValue()) {
                return Level.INFO;
            }
            return Level.FINE;
        }
        
        void log(int priority, String msg, Object... args) {
            Level level = findLevel(priority);

            if (args.length == 1 && args[0] instanceof Throwable) {
                LOG.log(level, msg, (Throwable) args[0]);
            } else {
                LOG.log(level, msg, args);
            }
        }

        final void loadJS(String js) {
            add(js);
        }

        void dispatch(Runnable r) {
            runSafe(r, false);
        }


        public void displayPage(URL url, Runnable r) {
            throw new UnsupportedOperationException(url.toString());
        }
    } // end of Command  
}
