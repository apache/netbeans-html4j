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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.html.boot.spi.Fn;

final class SimpleServer extends HttpServer<SimpleServer.ReqRes, SimpleServer.ReqRes, Object, SimpleServer.Context> implements Runnable {
    private final Map<String, Handler> maps = new TreeMap<>((s1, s2) -> {
        if (s1.length() != s2.length()) {
            return s2.length() - s1.length();
        }
        return s2.compareTo(s1);
    });
    private int max;
    private int min;
    private ServerSocketChannel server;
    private Selector connection;
    private Thread processor;

    private static final Pattern PATTERN_GET = Pattern.compile("(OPTIONS|HEAD|GET|POST|PUT|DELETE) */([^ \\?]*)(\\?[^ ]*)?");
    private static final Pattern PATTERN_HOST = Pattern.compile(".*^Host: *(.*):([0-9]+)$", Pattern.MULTILINE);
    private static final Pattern PATTERN_LENGTH = Pattern.compile(".*^Content-Length: ([0-9]+)$", Pattern.MULTILINE);
    static final Logger LOG = Logger.getLogger(SimpleServer.class.getName());

    SimpleServer() {
    }

    @Override
    void addHttpHandler(Handler h, String path) {
        if (!path.startsWith("/")) {
            throw new IllegalStateException("Shall start with /: " + path);
        }
        maps.put(path.substring(1), h);
    }

    @Override
    void init(int from, int to) throws IOException {
        this.connection = Selector.open();
        this.min = from;
        this.max = to;
    }

    @Override
    void start() throws IOException {
        LOG.log(Level.INFO, "Listening for HTTP connections on port {0}", getServer().socket().getLocalPort());
        processor = new Thread(this, "HTTP server");
        processor.start();
    }

    @Override
    String getRequestURI(ReqRes r) {
        return "/" + r.url;
    }

    @Override
    String getServerName(ReqRes r) {
        return r.hostName;
    }

    @Override
    int getServerPort(ReqRes r) {
        return r.hostPort;
    }

    @Override
    String getParameter(ReqRes r, String id) {
        return (String) r.args.get(id);
    }

    @Override
    String getMethod(ReqRes r) {
        return r.method;
    }

    @Override
    String getBody(ReqRes r) {
        if (r.body == null) {
            return "";
        } else {
            return new String(r.body.array(), StandardCharsets.UTF_8);
        }
    }

    static int endOfHeader(String header) {
        return header.indexOf("\r\n\r\n");
    }

    @Override
    String getHeader(ReqRes r, String key) {
        for (String l : r.header.split("\r\n")) {
            if (l.isEmpty()) {
                break;
            }
            if (l.startsWith(key + ":")) {
                return l.substring(key.length() + 1).trim();
            }
        }
        return null;
    }

    @Override
    Writer getWriter(ReqRes r) {
        return r.writer;
    }

    @Override
    void setContentType(ReqRes r, String contentType) {
        r.contentType = contentType;
    }

    @Override
    void setStatus(ReqRes r, int status) {
        r.status = status;
    }

    @Override
    OutputStream getOutputStream(ReqRes r) {
        return r.os;
    }

    @Override
    void suspend(ReqRes r) {
        r.interestOps(0);
        r.suspended = true;
    }

    @Override
    void resume(ReqRes r, Runnable whenReady) {
        whenReady.run();
        r.suspended = false;
        r.interestOps(SelectionKey.OP_WRITE);
        connectionWakeup();
    }

    @Override
    void setCharacterEncoding(ReqRes r, String encoding) {
        if (!encoding.equals("UTF-8")) {
            throw new IllegalStateException(encoding);
        }
    }

    @Override
    void addHeader(ReqRes r, String name, String value) {
        r.headers.put(name, value);
    }

    @Override
    <WebSocket> void send(WebSocket socket, String s) {
    }

    /**
     * @return the port to listen to
     */
    @Override
    public int getPort() {
        try {
            return getServer().socket().getLocalPort();
        } catch (IOException ex) {
            return -1;
        }
    }

    void connectionWakeup() {
        Selector localConnection = this.connection;
        if (localConnection != null) {
            localConnection.wakeup();
        }
    }

    @Override
    public void run() {
        ByteBuffer bb = ByteBuffer.allocate(2048);
        int sleep = 10;

        while (Thread.currentThread() == processor) {
            ServerSocketChannel localServer;
            Selector localConnection;

            SocketChannel toClose = null;
            try {
                synchronized (this) {
                    localServer = this.getServer();
                    localConnection = this.connection;
                }

                LOG.log(Level.FINE, "Before select {0}", localConnection.isOpen());
                LOG.log(Level.FINE, "Server {0}", localServer.isOpen());

                int amount = localConnection.select();

                LOG.log(Level.FINE, "After select: {0}", amount);
                if (amount == 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ex) {
                    }
                    sleep *= 2;
                    if (sleep > 1000) {
                        sleep = 1000;
                    }
                } else {
                    sleep = 10;
                }

                Set<SelectionKey> readyKeys = localConnection.selectedKeys();
                Iterator<SelectionKey> it = readyKeys.iterator();
                PROCESS:
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    LOG.log(Level.FINEST, "Handling key {0}", key.attachment());
                    it.remove();

                    if (key.isAcceptable()) {
                        try {
                            SocketChannel channel = localServer.accept();
                            channel.configureBlocking(false);
                            SelectionKey another = channel.register(
                                    localConnection, SelectionKey.OP_READ
                            );
                        } catch (ClosedByInterruptException ex) {
                            LOG.log(Level.WARNING, "Interrupted while accepting", ex);
                            server.close();
                            server = null;
                            LOG.log(Level.INFO, "Accept server reset");
                        }
                        continue PROCESS;
                    }

                    if (key.isReadable()) {
                        ((Buffer)bb).clear();
                        SocketChannel channel = (SocketChannel) key.channel();
                        toClose = channel;
                        channel.read(bb);
                        if (key.attachment() instanceof ReqRes) {
                            ((Buffer)bb).flip();
                            ReqRes req = (ReqRes) key.attachment();
                            req.bodyToFill().put(bb);
                            if (req.bodyToFill().remaining() == 0) {
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            continue PROCESS;
                        }


                        ((Buffer)bb).flip();
                        String text = new String(bb.array(), 0, bb.limit());
                        int fullHeader = endOfHeader(text);
                        if (channel.isOpen() && fullHeader == -1) {
                            continue PROCESS;
                        }
                        String header = text.substring(0, fullHeader);

                        Matcher m = PATTERN_GET.matcher(header);
                        String url = m.find() ? m.group(2) : null;
                        String args = url != null && m.groupCount() == 3 ? m.group(3) : null;
                        String method = m.group(1);

                        Map<String, String> context;
                        if (args != null) {
                            Map<String, String> c = new HashMap<>();
                            parseArgs(c, args);
                            context = Collections.unmodifiableMap(c);
                        } else {
                            context = Collections.emptyMap();
                        }

                        Matcher length = PATTERN_LENGTH.matcher(header);
                        ByteBuffer body = null;
                        if (length.find()) {
                            int contentLength = Integer.parseInt(length.group(1));
                            body = ByteBuffer.allocate(contentLength);
                            ((Buffer)bb).position(fullHeader + 4);
                            body.put(bb);
                        }

                        ReqRes req = findRequest(url, context, header, method, body);
                        key.attach(req);
                        if (body != null && body.remaining() > 0) {
                            key.interestOps(SelectionKey.OP_READ);
                            continue PROCESS;
                        }
                        key.interestOps(SelectionKey.OP_WRITE);
                        continue PROCESS;
                    }

                    if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        toClose = channel;
                        ReqRes reply = (ReqRes) key.attachment();
                        if (reply == null) {
                            continue PROCESS;
                        }
                        reply.handle(key, channel);
                    }
               }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Exception while handling request", t);
                if (toClose != null) {
                    try {
                        toClose.close();
                    } catch (IOException ioEx) {
                        LOG.log(Level.INFO, "While closing", ioEx);
                    }
                }
            }
        }

        try {
            LOG.fine("Closing connection");
            this.connection.close();
            LOG.fine("Closing server");
            this.getServer().close();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }

        synchronized (this) {
            notifyAll();
        }
        LOG.fine("All notified, exiting server");
    }

    private ReqRes findRequest(
        String url, Map<String, ? extends Object> args, String header,
        String method, ByteBuffer bodyToFill
    ) {
        LOG.log(Level.FINE, "Searching for page {0}", url);
        Matcher hostMatch = PATTERN_HOST.matcher(header);
        String host = null;
        int port = -1;
        if (hostMatch.find()) {
            host = hostMatch.group(1);
            port = Integer.parseInt(hostMatch.group(2));
        }
        if (host != null) {
            LOG.log(Level.FINE, "Host {0}:{1}", new Object[] { host, port });
        }

        for (Map.Entry<String, Handler> entry : maps.entrySet()) {
            if (url.startsWith(entry.getKey())) {
                final Handler h = entry.getValue();
                return new ReqRes(h, url, args, host, port, header, method, bodyToFill);
            }
        }
        throw new IllegalStateException("No mapping for " + url + " among " + maps);
    }

    private static void parseArgs(final Map<String, ? super String> context, final String args) {
        if (args != null) {
            for (String arg : args.substring(1).split("&")) {
                String[] valueAndKey = arg.split("=");

                String key = valueAndKey[1].replaceAll("\\+", " ");
                for (int idx = 0;;) {
                    idx = key.indexOf("%", idx);
                    if (idx == -1) {
                        break;
                    }
                    int ch = Integer.parseInt(key.substring(idx + 1, idx + 3), 16);
                    key = key.substring(0, idx) + (char) ch + key.substring(idx + 3);
                    idx++;
                }

                context.put(valueAndKey[0], key);
            }
        }
    }

    @Override
    public synchronized void shutdownNow() {
        Thread inter = processor;
        if (inter != null) {
            processor = null;
            LOG.fine("Processor cleaned");
            inter.interrupt();
            LOG.fine("Processor interrupted");
            try {
                wait(5000);
            } catch (InterruptedException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
            LOG.fine("After waiting");
        }
    }

    /**
     * Computes todays's date .
     */
    static byte[] date(Date date) {
        return date("Date: ", date != null ? date : new Date());
    }

    static byte[] date(String prefix, Date date) {
        try {
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("GMT")); // NOI18N
            return (prefix + f.format(date)).getBytes("utf-8");
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            return new byte[0];
        }
    }

    /**
     * @return the server
     */
    public ServerSocketChannel getServer() throws IOException {
        if (server == null) {
            ServerSocketChannel s = ServerSocketChannel.open();
            s.configureBlocking(false);

            Random random = new Random();
            for (int i = min; i <= max; i++) {
                int at = min + random.nextInt(max - min + 1);
                InetSocketAddress address = new InetSocketAddress(at);
                try {
                    s.socket().bind(address);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "Cannot bind to " + at, ex);
                    continue;
                }
                server = s;
                break;
            }

            server.register(this.connection, SelectionKey.OP_ACCEPT);
        }
        return server;
    }

    class Context implements ThreadFactory {
        private final String id;
        Executor RUN;
        Thread RUNNER;

        Context(String id) {
            this.id = id;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Processor for " + id);
            RUNNER = t;
            return t;
        }
    }

    @Override
    Context initializeRunner(String id) {
        Context c = new Context(id);
        c.RUN = Executors.newSingleThreadExecutor(c);
        return c;
    }

    @Override
    void runSafe(Context c, Runnable r, Fn.Presenter presenter) {
        class Wrap implements Runnable {
            @Override
            public void run() {
                if (presenter != null) {
                    try (Closeable c = Fn.activate(presenter)) {
                        r.run();
                    } catch (IOException ex) {
                        // go on
                    }
                } else {
                    r.run();
                }
            }
        }
        if (c.RUNNER == Thread.currentThread()) {
            if (presenter != null) {
                Runnable w = new Wrap();
                w.run();
            } else {
                r.run();
            }
        } else {
            Runnable w = new Wrap();
            c.RUN.execute(w);
        }
    }

    final class ReqRes extends SelectionKey {
        private final Handler h;
        final String url;
        final String hostName;
        final int hostPort;
        final Map<String, ? extends Object> args;
        final String header;
        final String method;
        final ByteBuffer body;
        private ByteBuffer bb = ByteBuffer.allocate(8192);
        private SelectionKey delegate;
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        final Map<String,String> headers = new LinkedHashMap<>();
        String contentType;
        int status = 200;
        boolean suspended;

        public ReqRes(
            Handler h,
            String url, Map<String, ? extends Object> args, String host,
            int port, String header, String method, ByteBuffer body
        ) {
            this.h = h;
            this.url = url;
            this.hostName = host;
            this.hostPort = port;
            this.header = header;
            this.args = args;
            this.method = method;
            this.body = body;
        }

        public void handle(SelectionKey key, SocketChannel channel) throws IOException {
            delegate = key;

            if (bb != null) {
                Map<String,String> headerAttrs = Collections.emptyMap();
                String mime;
                h.service(SimpleServer.this, this, this);
                headerAttrs = headers;

                mime = contentType;
                if (mime == null) {
                    mime = "content/unknown"; // NOI18N
                }
                ((Buffer)bb).clear();

                LOG.log(Level.FINE, "Found page request {0}", url); // NOI18N
                ((Buffer)bb).clear();
                bb.put(("HTTP/1.1 " + status + "\r\n").getBytes());
                bb.put("Connection: close\r\n".getBytes());
                bb.put("Server: Browser PReqenter\r\n".getBytes());
                bb.put(date(null));
                bb.put("\r\n".getBytes());
                bb.put(("Content-Type: " + mime + "\r\n").getBytes());
                for (Map.Entry<String,String> entry : headerAttrs.entrySet()) {
                    bb.put((entry.getKey() + ":" + entry.getValue() + "\r\n").getBytes());
                }
                bb.put("Pragma: no-cache\r\nCache-control: no-cache\r\n".getBytes());
                bb.put("\r\n".getBytes());
                ((Buffer)bb).flip();
                channel.write(bb);
                LOG.log(Level.FINER, "Written header, type {0}", mime);
                bb = null;

                if ("HEAD".equals(method) || "OPTIONS".equals(method)) {
                    LOG.fine("Writer flushed and closed, closing channel");
                    channel.close();
                    return;
                }
            }

            try {
                if (attachment() == null) {
                    if (suspended) {
                        channel.write(ByteBuffer.allocate(0));
                        return;
                    }
                    ByteBuffer out = ByteBuffer.wrap(toByteArray());
                    attach(out);
                }
                ByteBuffer bb = (ByteBuffer) attachment();
                if (bb.remaining() > 0) {
                    channel.write(bb);
                } else {
                    channel.close();
                }
            } finally {
                if (!channel.isOpen()) {
                    LOG.log(Level.FINE, "channel not open, closing");
                    key.attach(null);
                    key.cancel();
                }
            }
        }

        byte[] toByteArray() throws IOException {
            writer.close();
            return os.toByteArray();
        }

        public ByteBuffer bodyToFill() {
            return body;
        }

        @Override
        public String toString() {
            return "Request[" + method + ":" + url + "]";
        }

        @Override
        public SelectableChannel channel() {
            return delegate.channel();
        }

        @Override
        public Selector selector() {
            return delegate.selector();
        }

        @Override
        public boolean isValid() {
            return delegate.isValid();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public int interestOps() {
            return delegate.interestOps();
        }

        @Override
        public SelectionKey interestOps(int arg0) {
            return delegate.interestOps(arg0);
        }

        @Override
        public int readyOps() {
            return delegate.readyOps();
        }
    }
}
