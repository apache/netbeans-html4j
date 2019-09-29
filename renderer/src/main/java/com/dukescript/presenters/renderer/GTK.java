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
package com.dukescript.presenters.renderer;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import org.netbeans.html.boot.spi.Fn;

final class GTK extends Show implements InvokeLater {
    private final Fn.Presenter presenter;
    private final Runnable onPageLoad;
    private final Runnable onContext;
    private final boolean headless;

    private OnDestroy onDestroy;
    private OnLoad onLoad;
    private Pending pending;
    private String page;
    private Pointer jsContext;
    private NewWebView newWebView;

    GTK() {
        this(null, null, null, false);
    }

    GTK(Fn.Presenter p, Runnable onPageLoad, Runnable onContext, boolean hl) {
        this.onPageLoad = onPageLoad;
        this.presenter = p;
        this.headless = hl;
        this.onContext = onContext;
    }

    @Override
    public JSC jsc() {
        return INSTANCE.jsc;
    }
    
    @Override
    public Pointer jsContext() {
        return jsContext;
    }

    public interface GLib extends Library {
        void g_idle_add(Callback r, Pointer p);
    }
    public interface G extends Library {
        void g_signal_connect_data(Pointer obj, String signal, Callback callback, Pointer data, Void nul, int flags);
    }

    public interface Gtk extends Library {
        void gtk_init(int cnt, String[] args);

        Pointer gtk_window_new(int windowType);
        Pointer gtk_scrolled_window_new(Pointer ignore, Pointer ignore2);
        void gtk_window_get_position(Pointer window, IntByReference x, IntByReference y);
        void gtk_window_get_size(Pointer window, IntByReference width, IntByReference height);
        void gtk_window_set_default_size(Pointer window, int width, int height);
        void gtk_window_set_title(Pointer window, String title);
        void gtk_widget_show_all(Pointer window);
        void gtk_window_set_gravity(Pointer window, int gravity);
        void gtk_window_move(Pointer window, int x, int y);
        void gtk_container_add(Pointer container, Pointer child);
        void gtk_widget_grab_focus(Pointer window);
        void gtk_widget_destroy(Pointer window);
        void gtk_window_present(Pointer window);
        void gtk_main();
        void gtk_main_quit();

        void gtk_widget_set_size_request(Pointer window, int width, int height);
        void gtk_window_set_position(Pointer window, int type);
        Pointer gtk_button_new_with_label(String label);
        void gtk_container_set_border_width(Pointer window, int size);
    }

    public interface Gdk extends Library {
        int gdk_screen_get_primary_monitor(Pointer screen);
        Pointer gdk_screen_get_default();
        void gdk_screen_get_monitor_geometry(Pointer screen, int monitor, GRectangle geometry);
    }

    public static class GRectangle extends Structure {
        public int x, y;
        public int width, height;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("x", "y", "width", "height");
        }
    }

    public interface WebKit extends Library {
        Pointer webkit_web_view_new();
        void webkit_web_view_load_uri(Pointer webView, String url);
        Pointer webkit_web_page_frame_get_javascript_context_for_script_world(Pointer webFrame, Pointer world);
        int webkit_web_view_get_load_status(Pointer webView);
        Pointer webkit_web_view_get_main_frame(Pointer webView);
        Pointer webkit_web_frame_get_global_context(Pointer webFrame);
        String webkit_web_frame_get_title(Pointer webFrame);
    }

    private static Libs INSTANCE;

    final Libs getInstance(boolean[] initialized) {
        synchronized (GTK.class) {
            if (INSTANCE == null) {
                INSTANCE = new Libs();
                initialized[0] = true;
            }
        }
        return INSTANCE;
    }

    @Override
    public void show(URI url) throws IOException {
        this.page = url.toASCIIString();
        boolean[] justInitialized = {false};
        Gtk gtk;
        try {
            gtk = getInstance(justInitialized).gtk;
        } catch (LinkageError e) {
            throw new IOException(e);
        }
        if (justInitialized[0]) {
            gtk.gtk_init(0, null);
            run();
            gtk.gtk_main();
        } else {
            INSTANCE.glib.g_idle_add(this, null);
        }
    }

    @Override
    public void run() {
        final Libs libs = getInstance(null);
        final Gdk gdk = libs.gdk;
        final Gtk gtk = libs.gtk;
        final WebKit webKit = libs.webKit;
        final G g = libs.g;

        final Pointer screen = gdk.gdk_screen_get_default();
        int primaryMonitor = gdk.gdk_screen_get_primary_monitor(screen);
        GRectangle size = new GRectangle();
        gdk.gdk_screen_get_monitor_geometry(screen, primaryMonitor, size);
        int height = (int) (size.height * 0.9);
        int width = (int) (size.width * 0.9);
        int x = (int) (size.width * 0.05) + size.x;
        int y = (int) (size.height * 0.05) + size.y;

        final Pointer window = gtk.gtk_window_new(0);
        gtk.gtk_window_set_default_size(window, width, height);
        gtk.gtk_window_set_gravity(window, 5);
        gtk.gtk_window_move(window, x, y);

        Pointer scroll = gtk.gtk_scrolled_window_new(null, null);
        gtk.gtk_container_add(window, scroll);

        final Pointer webView = webKit.webkit_web_view_new();
        gtk.gtk_container_add(scroll, webView);
        Pointer frame = webKit.webkit_web_view_get_main_frame(webView);
        Pointer ctx = webKit.webkit_web_frame_get_global_context(frame);
        this.jsContext = ctx;
        if (onContext != null) {
            onContext.run();
        }
        onLoad = new OnLoad(webView, libs, window, onPageLoad);
        g.g_signal_connect_data(webView, "notify::load-status", onLoad, null, null, 0);

        newWebView = new NewWebView(libs, headless);
        g.g_signal_connect_data(webView, "create-web-view", newWebView, window, null, 0);

        webKit.webkit_web_view_load_uri(webView, page);

        gtk.gtk_widget_grab_focus(webView);

        onDestroy = new OnDestroy();
        g.g_signal_connect_data(window, "destroy", onDestroy, null, null, 0);
        pending = new Pending();
        if (!headless) {
            gtk.gtk_widget_show_all(window);
        }
    }

    private static class NewWebView implements Callback {
        private final Libs libs;
        private final boolean headless;
        private final Collection<OnLoad> onLoads = new HashSet<OnLoad>();

        NewWebView(Libs libs, boolean headless) {
            this.libs = libs;
            this.headless = headless;
        }

        public Pointer createWebView(Pointer orig, Pointer frame, Pointer origWindow) {
            IntByReference x = new IntByReference(0);
            IntByReference y = new IntByReference(0);
            IntByReference width = new IntByReference(0);
            IntByReference height = new IntByReference(0);

            Gtk gtk = libs.gtk;

            gtk.gtk_window_get_position(origWindow, x, y);
            gtk.gtk_window_get_size(origWindow, width, height);

            int tenthWidth = width.getValue() / 10;
            int tenthHeight = height.getValue() / 10;

            final Pointer window = gtk.gtk_window_new(0);
            gtk.gtk_window_set_default_size(window, width.getValue() - 2 * tenthWidth, height.getValue() - 2 * tenthHeight);
            gtk.gtk_window_set_gravity(window, 5);
            gtk.gtk_window_move(window, x.getValue() + tenthWidth, y.getValue() + tenthHeight);

            Pointer scroll = gtk.gtk_scrolled_window_new(null, null);
            gtk.gtk_container_add(window, scroll);

            final Pointer webView = libs.webKit.webkit_web_view_new();
            gtk.gtk_container_add(scroll, webView);

            gtk.gtk_widget_grab_focus(webView);

            OnLoad onLoad = new OnLoad(webView, libs, window, null);
            onLoads.add(onLoad);
            libs.g.g_signal_connect_data(webView, "notify::load-status", onLoad, null, null, 0);

            if (!headless) {
                gtk.gtk_widget_show_all(window);
                gtk.gtk_window_present(window);
            }

            return webView;
        }
    }

    private static class OnLoad implements Callback {
        private final Libs libs;
        private final Pointer webView;
        private final Pointer window;
        private final Runnable onPageLoad;
        private Title title;

        public OnLoad(Pointer webView, Libs libs, Pointer window, Runnable onPageLoad) {
            this.webView = webView;
            this.window = window;
            this.libs = libs;
            this.onPageLoad = onPageLoad;
        }

        public void loadStatus() {
            int status = libs.webKit.webkit_web_view_get_load_status(webView);
            if (status == 2) {
                final Pointer frame = libs.webKit.webkit_web_view_get_main_frame(webView);
                if (title == null) {
                    title = new Title(frame);
                    title.updateTitle();
                    libs.g.g_signal_connect_data(frame, "notify::title", title, null, null, 0);
                }
                if (onPageLoad != null) {
                    onPageLoad.run();
                }
            }
        }

        private class Title implements Callback {
            private final Pointer frame;

            public Title(Pointer frame) {
                this.frame = frame;
            }

            public void updateTitle() {
                String title = libs.webKit.webkit_web_frame_get_title(frame);
                if (title == null) {
                    title = "DukeScript Application";
                }
                libs.gtk.gtk_window_set_title(window, title);
            }
        }
    }

    private static class OnDestroy implements Callback {
        public void signal() {
            System.exit(0);
        }
    }

    @Override
    public void execute(Runnable command) {
        pending.queue.add(command);
        if (Fn.activePresenter() == presenter) {
            try {
                pending.process();
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Cannot process " + command, ex);
            }
        } else {
            GLib glib = getInstance(null).glib;
            glib.g_idle_add(pending, null);
        }
    }



    private class Pending implements Callback {
        final Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();

        public void process() throws Exception {
            Closeable c = Fn.activate(presenter);
            try {
                for (;;) {
                    Runnable r = queue.poll();
                    if (r == null) {
                        break;
                    }
                    r.run();
                }
            } finally {
                c.close();
            }
        }
    }

    static final class Libs {
        final Gtk gtk;
        final JSC jsc;
        final G g;
        final GLib glib;
        final Gdk gdk;
        final WebKit webKit;

        Libs() {
            List<Throwable> errors = new ArrayList<Throwable>();
            this.gtk = Libs.loadLibrary(Gtk.class, false, null);
            this.jsc = Libs.loadLibrary(JSC.class, true, errors);
            this.g = Libs.loadLibrary(G.class, false, errors);
            this.glib = Libs.loadLibrary(GLib.class, false, errors);
            this.gdk = Libs.loadLibrary(Gdk.class, false, errors);
            this.webKit = Libs.loadLibrary(WebKit.class, false, errors);

            if (!errors.isEmpty()) {
                throw linkageError(errors);
            }
        }

        static <T> T loadLibrary(Class<T> type, boolean allowObjects, Collection<Throwable> errors) {
            String libName = System.getProperty("com.dukescript.presenters.renderer." + type.getSimpleName());
            if (libName == null) {
                if (type == JSC.class) {
                    libName = "javascriptcoregtk-3.0";
                } else if (type == GTK.GLib.class) {
                    libName = "glib-2.0";
                } else if (type == GTK.G.class) {
                    libName = "gobject-2.0";
                } else if (type == GTK.Gdk.class) {
                    libName = "gtk-3";
                } else if (type == GTK.Gtk.class) {
                    libName = "gtk-3";
                } else if (type == GTK.WebKit.class) {
                    libName = "webkitgtk-3.0";
                }
            }
            try {
                Object lib = Native.loadLibrary(libName, type, Collections.singletonMap(Library.OPTION_ALLOW_OBJECTS, allowObjects));
                return type.cast(lib);
            } catch (LinkageError err) {
                if (errors != null) {
                    errors.add(err);
                    return null;
                } else {
                    throw err;
                }
            }
        }

        private LinkageError linkageError(List<Throwable> errors) {
            StringWriter sw = new StringWriter();
            String libraryPath = System.getProperty("java.library.path");
            sw.append("Java Library Path:");
            if (libraryPath != null) {
                for (String pathElement : libraryPath.split(File.pathSeparator)) {
                    sw.append("\n  Path ").append(pathElement);
                    File pathFile = new File(pathElement);
                    String[] libraries = pathFile.list();
                    if (libraries != null) {
                        for (String lib : libraries) {
                            sw.append("\n    ").append(lib);
                        }
                    }
                }
                sw.append("\n");
            }
            PrintWriter pw = new PrintWriter(sw);
            for (Throwable t : errors) {
                t.printStackTrace(pw);
            }
            sw.append("\nStatus:");
            sw.append("\n  jsc: " + jsc);
            sw.append("\n  g: " + g);
            sw.append("\n  glib: " + glib);
            sw.append("\n  gdk: " + gdk);
            sw.append("\n  webKit: " + webKit);
            return new LinkageError(sw.toString());
        }
    }
}

interface InvokeLater extends Callback {
    public void run();
}
