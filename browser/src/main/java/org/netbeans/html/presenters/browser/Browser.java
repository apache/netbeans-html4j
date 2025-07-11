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
package org.netbeans.html.presenters.browser;

import org.netbeans.html.presenters.render.Show;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;
import org.netbeans.html.presenters.spi.ProtoPresenter;
import org.netbeans.html.presenters.spi.ProtoPresenterBuilder;
import org.openide.util.lookup.ServiceProvider;

/** Browser based {@link Presenter}. It starts local server and
 * launches browser that connects to it. Use {@link Browser.Config} to
 * configure the actual browser to be started.
 * <p>
 * To use this presenter specify following dependency:
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;org.netbeans.html.browser&lt;/groupId&gt;
 *   &lt;artifactId&gt;browser&lt;/artifactId&gt;
 *   &lt;version&gt;<a target="blank" href="http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.dukescript.presenters%22%20AND%20a%3A%22browser%22">1.x</a>&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * Since 1.8.2 version this presenter uses lightweight HTTP server
 * implementation that eliminates the need for Grizzly dependencies.
 * A <em>compatibility mode</em> can be turned on by including the
 * <code>org.glassfish.grizzly:grizzly-http-server:2.3.19</code>
 * dependency during execution - see
 * <a target="_blank" href="https://github.com/apache/netbeans-html4j/pull/26">PR-26</a> for more details.
 */
@ServiceProvider(service = Presenter.class)
public final class Browser implements Fn.Presenter, Fn.KeepAlive, Flushable,
Executor, Closeable {
    static final Logger LOG = Logger.getLogger(Browser.class.getName());
    private final Map<String,Command> SESSIONS = new HashMap<>();
    private final String app;
    private HttpServer server;
    private Runnable onPageLoad;
    private Command current;
    private final Config config;
    private final Supplier<HttpServer<?, ?, ?, ?>> serverProvider;

    /** Default constructor. Reads configuration from properties. The actual browser to
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
     * In addition to the above properties, it is possible to also enable
     * debugging by setting <code>com.dukescript.presenters.browserDebug=true</code>.
     */
    public Browser() {
        this(new Config());
    }

    /**
     * Browser configured by provided config.
     *
     * @param config the configuration
     */
    public Browser(Config config) {
        this(findCalleeClassName(), config, null);
    }

    Browser(String app, Config config, Supplier<HttpServer<?,?,?, ?>> serverProvider) {
        this.serverProvider = serverProvider != null ? serverProvider : SimpleServer::new;
        this.app = app;
        this.config = new Config(config);
    }

    @Override
    public final void execute(final Runnable r) {
        current.execute(r);
    }

    @Override
    public void close() throws IOException {
        if (server != null) {
            server.shutdownNow();
        }
    }

    HttpServer server() {
        return server;
    }

    static HttpServer findServer(Object obj) {
        Command c;
        if (obj instanceof Command) {
            c = (Command) obj;
        } else if (obj instanceof ProtoPresenter) {
            c = ((ProtoPresenter) obj).lookup(Command.class);
        } else {
            throw new IllegalArgumentException("Cannot find server for " + obj);
        }
        return c.browser.server();
    }

    /** Shows URL in a browser.
     * @param page the page to display in the browser
     * @throws IOException if something goes wrong
     */
    void show(URI page) throws IOException {
        config.getBrowser().accept(page);
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

    private static URI pageURL(String protocol, HttpServer server, final String page) {
        int port = server.getPort();
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
            this.server = serverProvider.get();
            int from = 8080;
            int to = 65535;
            int port = config.getPort();
            if (port != -1) {
                from = to = port;
            }
            server.init(from, to);

            this.server.addHttpHandler(new RootPage(page), "/");
            server.start();

            show(pageURL("http", server, "/"));
        } catch (IOException ex) {
            Logger.getLogger(Browser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static <T extends Exception> T raise(Class<T> aClass, Exception ex) throws T {
        throw (T)ex;
    }

    /** Parameters to configure {@link Browser}.
     * Create an instance and pass it
     * to {@link Browser#Browser(org.netbeans.html.presenters.browser.Browser.Config) }
     * constructor.
     */
    public final static class Config {
        private Consumer<URI> browser;
        Integer port;
        boolean debug = Boolean.getBoolean("com.dukescript.presenters.browserDebug");

        /**
         * Default constructor.
         */
        public Config() {
            command(null);
        }

        private Config(Config copy) {
            this.browser = copy.browser;
            this.port = copy.port;
            this.debug = copy.debug;
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
         * Calling this method replaces any value specified previously or
         * by the {@link #browser(java.util.function.Consumer)} method.
         *
         * @param executable browser to execute
         * @return this instance
         *
         * @see #browser(java.util.function.Consumer)
         */
        public Config command(String executable) {
            this.browser = (page) -> {
                String impl = executable;
                if (impl == null) {
                    impl = System.getProperty("com.dukescript.presenters.browser"); // NOI18N
                }
                if ("none".equalsIgnoreCase(impl)) { // NOI18N
                    return;
                }
                try {
                    Show.show(impl, page);
                } catch (IOException ex) {
                    throw raise(RuntimeException.class, ex);
                }
            };
            return this;
        }

        /** Specifies a callback to handle opening of a URI. The browser
         * presenter sets an internal HTTP server up and then asks the
         * {@code urlOpener} to open the initial page. Calling this
         * method replaces previous openers as well as configuration
         * set by {@link #command(java.lang.String)} method.
         *
         * @param urlOpener callback to handle opening of a URI
         * @return this instance
         * @since 1.7.3
         * @see #command(java.lang.String)
         */
        public Config browser(Consumer<URI> urlOpener) {
            this.browser = urlOpener;
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

        /** Enable or disable debugging. The default value is taken from a property
         * {@code com.dukescript.presenters.browserDebug}. If the property is
         * not specified, then the default value is {@code false}.
         *
         * @param debug true or false
         * @return this instance
         * @since 1.8
         */
        public Config debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        final Consumer<URI> getBrowser() {
            return browser;
        }

        final int getPort() {
            if (port != null) {
                return port;
            }
            String browserPort = System.getProperty("com.dukescript.presenters.browserPort"); // NOI18N
            try {
                return Integer.parseInt(browserPort);
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
    }

    static <Response> void cors(HttpServer<?, Response, ?, ?> s, Response r) {
        s.setCharacterEncoding(r, "UTF-8");
        s.addHeader(r, "Access-Control-Allow-Origin", "*");
        s.addHeader(r, "Access-Control-Allow-Credentials", "true");
        s.addHeader(r, "Access-Control-Allow-Headers", "Content-Type");
        s.addHeader(r, "Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
    }

    private final class RootPage extends HttpServer.Handler {
        private final URL page;

        public RootPage(URL page) {
            this.page = page;
        }

        @Override
        public <Request, Response> void service(HttpServer<Request, Response, ?, ?> server, Request rqst, Response rspns) throws IOException {
            String path = server.getRequestURI(rqst);
            cors(server, rspns);
            if ("OPTIONS".equals(server.getMethod(rqst))) { // NOI18N
                server.setStatus(rspns, 204);
                server.addHeader(rspns, "Allow", "OPTIONS, GET, HEAD, POST, PUT"); // NOI18N
                return;
            }
            if ("/".equals(path) || "index.html".equals(path)) {
                Reader is;
                String prefix = "http://" + server.getServerName(rqst) + ":" + server.getServerPort(rqst) + "/";
                Writer w = server.getWriter(rspns);
                server.setContentType(rspns, "text/html");
                final Command cmd = new Command(server, Browser.this, prefix);
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
                String id = server.getParameter(rqst, "id");
                Command c = SESSIONS.get(id);
                if (c == null) {
                    server.getWriter(rspns).write("No command for " + id);
                    server.setStatus(rspns, 404);
                    return;
                }
                c.service(rqst, rspns);
            } else {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                URL relative = new URL(page, path);
                InputStream is;
                URLConnection conn;
                try {
                    conn = relative.openConnection();
                    is = conn.getInputStream();
                } catch (FileNotFoundException ex) {
                    server.setStatus(rspns, 404);
                    return;
                }
                String found = null;
                if (relative.getProtocol().equals("file")) {
                    try {
                        File file = new File(relative.toURI());
                        found = Files.probeContentType(file.toPath());
                    } catch (URISyntaxException | IOException ignore) {
                    }
                } else {
                    found = conn.getContentType();
                }
                if (found == null || "content/unknown".equals(found)) {
                    if (path.endsWith(".html")) {
                        found = "text/html";
                    }
                    if (path.endsWith(".js")) {
                        found = "text/javascript";
                    }
                    if (path.endsWith(".css")) {
                        found = "text/css";
                    }
                }
                if (found != null) {
                    server.setContentType(rspns, found);
                }
                OutputStream out = server.getOutputStream(rspns);
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
                    + "function waitForCommand(counter) {\n"
                    + "  try {\n"
                    + "    if (waitForCommand.seenError) {\n"
                    + "      console.warn('Disconnected from " + prefix + "');\n"
                    + "      return;\n"
                    + "    };\n"
                    + "    var request = new XMLHttpRequest();\n");
            if (Browser.this.config.debug) {
                w.write(""
                    + "    console.log('GET[' + counter + ']....');\n"
                );
            }
            w.write(""
                    + "    request.open('GET', '" + prefix + "command.js?id=" + id + "', true);\n"
                    + "    request.setRequestHeader('Content-Type', 'text/plain; charset=utf-8');\n"
                    + "    request.onerror = function(ev) {\n"
                    + "      console.warn(ev);\n"
                    + "      waitForCommand.seenError = true;\n"
                    + "    };\n"
                    + "    request.onreadystatechange = function() {\n"
                    + "      if (this.readyState!==4) return;\n"
                    + "      try {\n"
            );
            if (Browser.this.config.debug) {
                w.write(""
                    + "        console.log('...GET[' + counter + '] got something ' + this.responseText.substring(0,80));\n"
                    + "        var cmd = document.getElementById('cmd');\n"
                    + "        if (cmd) cmd.innerHTML = this.responseText.substring(0,80);\n"
                );
            }
            w.write(""
                    + "        (0 || eval)(this.responseText);\n"
                    + "      } catch (e) {\n"
                    + "        console.warn(e); \n"
                    + "      } finally {\n"
                    + "        waitForCommand(counter + 1);\n"
                    + "      }\n"
                    + "    };\n"
                    + "    request.send();\n"
                    + "  } catch (e) {\n"
                    + "    console.warn(e);\n"
                    + "    waitForCommand(counter + 1);\n"
                    + "  }\n"
                    + "}\n"
                    + "waitForCommand(1);\n"
            );
            w.write("  </script>\n");
        }
    }

    String createCallbackFn(String prefix, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("this.toBrwsrSrvr = function(name, a1, a2, a3, a4) {\n"
            + "var url = '").append(prefix).append("command.js?id=").append(id).append("&name=' + name;\n"
            + "var body = 'p0=' + encodeURIComponent(a1);\n"
            + "body += '&p1=' + encodeURIComponent(a2);\n"
            + "body += '&p2=' + encodeURIComponent(a3);\n"
            + "body += '&p3=' + encodeURIComponent(a4);\n"
            + "var request = new XMLHttpRequest();\n"
        );
        if (Browser.this.config.debug) {
            sb.append(""
            + "console.log('PUT ... ' + body.substring(0, 80));\n"
            + "var now = new Date().getTime();\n"
            );
        }
        sb.append(""
            + "var async = name === 'p';\n"
            + "request.open('PUT', url, async);\n"
            + "request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=utf-8');\n"
            + "request.send(body);\n"
            + "if (async) return '';\n"
            + "var txt = request.responseText;\n"
        );
        if (Browser.this.config.debug) {
            sb.append(""
            + "var then = new Date().getTime();\n"
            + "if (txt && txt !== 'null') {\n"
            + "  var cmd = document.getElementById('cmd');\n"
            + "  if (cmd) cmd.innerHTML = txt.substring(0,80);\n"
            + "}\n"
            + "console.log('... PUT [' + (then - now) + 'ms]: ' + txt.substring(0, 80));\n"
            );
        }
        sb.append(""
            + "return txt;\n"
            + "};\n"
        );
        return sb.toString();
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

    private static final class Command<Request, Response, Runner> extends Object
    implements Executor {
        private final HttpServer<Request, Response, ?, Runner> server;
        private final Queue<Object> exec;
        private final Browser browser;
        private final String id;
        private final String prefix;
        private Runner RUNNER;
        private Response suspended;
        private boolean initialized;
        private final ProtoPresenter presenter;

        Command(HttpServer<Request, Response, ?, Runner> s, Browser browser, String prefix) {
            this.server = s;
            this.id = UUID.randomUUID().toString();
            this.RUNNER = s.initializeRunner(this.id);
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
        public final void execute(final Runnable r) {
            server.runSafe(this.RUNNER, r, this.presenter);
        }

        final synchronized void add(Object obj) {
            if (suspended != null) {
                Response rqst = suspended;
                server.resume(rqst, () -> {
                    try (Writer w = server.getWriter(rqst)) {
                        w.write(obj.toString());
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                });
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
            server.suspend(rspns);
            return null;
        }

        private synchronized boolean initialize(Response rspns) {
            if (!initialized) {
                initialized = true;
                suspended = rspns;
                server.suspend(rspns);
                execute(browser.onPageLoad);
                return true;
            }
            return false;
        }

        void service(Request rqst, Response rspns) throws IOException {
            final String methodName = server.getParameter(rqst, "name");
            server.setContentType(rspns, "text/javascript");
            Writer w = server.getWriter(rspns);
            if (methodName == null) {
                if (initialize(rspns)) {
                    return;
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
                List<String> args = new ArrayList<>();
                String body = server.getBody(rqst);
                for (String p : body.split("&")) {
                    if (p.length() >= 3) {
                        args.add(URLDecoder.decode(p.substring(3), "UTF-8"));
                    }
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
            String sb = this.browser.createCallbackFn(prefix, id);
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
            server.runSafe(RUNNER, r, null);
        }


        public void displayPage(URL url, Runnable r) {
            throw new UnsupportedOperationException(url.toString());
        }
    } // end of Command
}
