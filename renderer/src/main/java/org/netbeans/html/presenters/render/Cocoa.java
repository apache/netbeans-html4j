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
package org.netbeans.html.presenters.render;

import com.sun.jna.Callback;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.FromNativeContext;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.io.Closeable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import org.netbeans.html.boot.spi.Fn;

final class Cocoa extends Show implements Callback {
    private final Fn.Presenter presenter;
    private final Runnable onPageLoad;
    private final Runnable onContext;
    private final JSC jsc;

    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<Runnable>();
    private static Pointer NSApp;
    private static Pointer appDelPtr;
    private static Pointer doMainSelector;
    private static Thread dispatchThread;

    private AppDidStart appDidStart;
    private Ready ready;
    private ContextCreated contextCreated;
    private UIDelegate ui;
    private DialogHandler[] dialogs;
    private Pointer jsContext;
    private String page;
    private Pointer webView;
    private Pointer mainWindow;

    Cocoa() {
        this(null, null, null, false);
    }

    @SuppressWarnings("deprecation")
    Cocoa(Fn.Presenter p, Runnable onPageLoad, Runnable onContext, boolean hl) {
        this.presenter = p;
        this.onPageLoad = onPageLoad;
        this.onContext = onContext;
        this.jsc = (JSC) Native.loadLibrary("JavaScriptCore", JSC.class, Collections.singletonMap(Library.OPTION_ALLOW_OBJECTS, true));
    }

    @Override
    public JSC jsc() {
        return jsc;
    }

    @Override
    public Pointer jsContext() {
        return jsContext;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void show(URI page) {
        this.page = page.toASCIIString();

        ensureHttpAccess(page);

        Native.loadLibrary("WebKit", WebKit.class);

        appDidStart = new AppDidStart();
        contextCreated = new ContextCreated();
        ready = new Ready();
        ui = new UIDelegate();
        dialogs = new DialogHandler[3];
        dialogs[0] = new DialogHandler(0);
        dialogs[1] = new DialogHandler(1);
        dialogs[2] = new DialogHandler(2);

        if (appDelPtr == null) {
            ObjC objC = ObjC.INSTANCE;
            Pointer appDelClass = objC.objc_allocateClassPair(objC.objc_getClass("NSObject"), "AppDelegate", 0);
            objC.class_addMethod(appDelClass, objC.sel_getUid("applicationDidFinishLaunching:"), appDidStart, "i@:@");
            doMainSelector = objC.sel_getUid("doMain");
            Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(false, false, "Cocoa Dispatch Thread"));
            objC.class_addMethod(appDelClass, doMainSelector, this, "i@");
            objC.class_addMethod(appDelClass, objC.sel_getUid("webView:didCreateJavaScriptContext:forFrame:"), contextCreated, "v@:@:@");
            objC.class_addMethod(appDelClass, objC.sel_getUid("webView:didFinishLoadForFrame:"), ready, "v@:@");
            objC.class_addMethod(appDelClass, objC.sel_getUid("webView:createWebViewWithRequest:"), ui, "v@:@");
            objC.class_addMethod(appDelClass, objC.sel_getUid("webView:runJavaScriptAlertPanelWithMessage:initiatedByFrame:"), dialogs[0], "v@:@:@");
            objC.class_addMethod(appDelClass, objC.sel_getUid("webView:runJavaScriptConfirmPanelWithMessage:initiatedByFrame:"), dialogs[1], "v@:@:@");
            objC.class_addMethod(appDelClass, objC.sel_getUid("webView:runJavaScriptTextInputPanelWithPrompt:defaultText:initiatedByFrame:"), dialogs[2], "v@:@:@");
            objC.objc_registerClassPair(appDelClass);

            long appDelObj = send(objC.objc_getClass("AppDelegate"), "alloc");
            appDelPtr = new Pointer(appDelObj);
            send(appDelPtr, "init");

            send(appDelPtr,
                "performSelectorOnMainThread:withObject:waitUntilDone:",
                doMainSelector, null, 1
            );
        } else {
            execute(new Runnable() {
                @Override
                public void run() {
                    appDidStart.callback(appDelPtr);
                }
            });
        }
    }

    @Override
    public void execute(Runnable command) {
        QUEUE.add(command);
        if (Thread.currentThread() == dispatchThread && Fn.activePresenter() == presenter) {
            try {
                process();
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Cannot process " + command, ex);
            }
        } else {
            send(appDelPtr,
                "performSelectorOnMainThread:withObject:waitUntilDone:",
                doMainSelector, null, 0
            );
        }
    }

    private void process() throws Exception {
        Closeable c = presenter == null ? null : Fn.activate(presenter);
        try {
            for (;;) {
                Runnable r = QUEUE.poll();
                if (r == null) {
                    break;
                }
                r.run();
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public interface ObjC extends Library {
        @SuppressWarnings("deprecation")
        public static ObjC INSTANCE = (ObjC) Native.loadLibrary("objc.A", ObjC.class);

        public boolean class_addMethod(Pointer cls, Pointer name, Callback imp, String types);

        public String class_getName(Pointer cls);

        public String object_getClassName(Pointer cls);

        public Pointer class_copyMethodList(Class cls, IntByReference outCount);

        public Pointer objc_allocateClassPair(Pointer cls, String name, int additionalBytes);

        public Pointer objc_getClass(String name);

        public long objc_msgSend(Pointer theReceiver, Pointer theSelector, Object... arguments);

        public Rct objc_msgSend_stret(Pointer theReceiver, Pointer theSelector, Object... arguments);

        public void objc_registerClassPair(Pointer cls);

        public Pointer sel_getUid(String name);
    }

    static long send(Pointer obj, String selector, Object... args) {
        Pointer uid = ObjC.INSTANCE.sel_getUid(selector);
        return ObjC.INSTANCE.objc_msgSend(obj, uid, args);
    }

    public static interface WebKit extends Library {
    }

    public void callback(Pointer self) throws Exception {
        if (NSApp != null) {
            process();
            return;
        }

        ObjC objC = ObjC.INSTANCE;
	long res = send(objC.objc_getClass("NSApplication"), "sharedApplication");
	if (res == 0) {
            System.err.print("Failed to initialized NSApplication...  terminating...\n");
            System.exit(1);
	}
        dispatchThread = Thread.currentThread();
        NSApp = new Pointer(res);
	send(NSApp, "setActivationPolicy:", 0);
	send(NSApp, "setDelegate:", self);
	res = send(NSApp, "run");
        System.err.println("end res: " + res);
    }

    public final class AppDidStart implements Callback {
        AppDidStart() {
        }

        public long callback(Pointer self) {
            ObjC objC = ObjC.INSTANCE;
            mainWindow = new Pointer(send(objC.objc_getClass("NSWindow"), "alloc"));

            Pointer screen = new Pointer(send(objC.objc_getClass("NSScreen"), "mainScreen"));

            Pointer uid = ObjC.INSTANCE.sel_getUid("frame");
            Rct size = ObjC.INSTANCE.objc_msgSend_stret(screen, uid);

            double height = size.height.doubleValue() * 0.9;
            double width = size.width.doubleValue() * 0.9;
            double x = size.width.doubleValue() * 0.05 + size.x.doubleValue();
            double y = size.height.doubleValue() * 0.05 + size.y.doubleValue();

            Rct r = new Rct(x, y, width, height);

            int mode = 15;
            int backingstoreBuffered = 2;

	    send(mainWindow,
                "initWithContentRect:styleMask:backing:defer:",
                r, mode, backingstoreBuffered, false
            );
            send(mainWindow, "setTitle:", nsString("Browser demo"));
            Pointer webViewClass = objC.objc_getClass("WebView");
            long webViewId = send(webViewClass, "alloc");
            webView = new Pointer(webViewId);
            send(webView, "init");

            send(webView, "setFrameLoadDelegate:", self);
            send(webView, "setUIDelegate:", self);

            Pointer frame = new Pointer(send(webView, "mainFrame"));

            Pointer urlClass = objC.objc_getClass("NSURL");
            Pointer url = new Pointer(send(urlClass, "URLWithString:", nsString(page)));
            Pointer requestClass = objC.objc_getClass("NSURLRequest");
            Pointer request = new Pointer(send(requestClass, "alloc"));
            send(request, "initWithURL:", url);

            send(mainWindow, "setContentView:", webView);
            send(frame, "loadRequest:", request);

            send(mainWindow, "becomeFirstResponder");
            send(mainWindow, "makeKeyAndOrderFront:", NSApp);
	    return 1;
        }
    }

    static Pointer nsString(String bd) {
        ObjC objC = ObjC.INSTANCE;
        Pointer stringClass = objC.objc_getClass("NSString");
        Pointer browserDemo = new Pointer(send(stringClass, "stringWithCString:encoding:", bd, 4));
        return browserDemo;
    }

    public final class ContextCreated implements Callback {
        ContextCreated() {
        }

        public void callback(Pointer webView, Pointer ctx, Pointer frame) {
            frame = new Pointer(send(frame, "mainFrame"));
            ctx = new Pointer(send(frame, "globalContext"));

            jsContext = ctx;
            if (onContext != null) {
                onContext.run();
            }
        }
    }

    public final class Ready implements Callback {
        Ready() {
        }

        public void callback(Pointer p1, Pointer frame) {
            send(webView, "stringByEvaluatingJavaScriptFromString:", nsString("1 + 1"));
            if (onPageLoad != null) {
                onPageLoad.run();
            }
        }
    }

    public final class DialogHandler implements Callback {
        private final int type;

        public DialogHandler(int type) {
            this.type = type;
        }

        public boolean alertOrConfirm(Pointer appDelegate, Pointer selector, Pointer webView, Pointer msg, Pointer frame) {
            ObjC objC = ObjC.INSTANCE;
/*
            System.err.println("webView: " + objC.object_getClassName(webView));
            System.err.println("frame: " + objC.object_getClassName(frame));
            System.err.println("msg: " + objC.object_getClassName(msg));

            String text = new Pointer(send(msg, "UTF8String")).getString(0, "UTF-8");
            System.err.println("msg: " + text);
*/
            Pointer alert = new Pointer(send(objC.objc_getClass("NSAlert"), "alloc"));
            send(alert, "init");
            send(alert, "setMessageText:", msg);
            send(alert, "addButtonWithTitle:", nsString("OK"));
            if (type == 1) {
                send(alert, "addButtonWithTitle:", nsString("Cancel"));
            }

            int res = ((int) send(alert, "runModal")) & 1;

            return res == 0;
        }
    }

    public final class UIDelegate implements Callback {
        UIDelegate() {
        }

        public Pointer callback(Pointer appDelegate) {
            ObjC objC = ObjC.INSTANCE;

            Pointer uid = ObjC.INSTANCE.sel_getUid("frame");
            Rct size = ObjC.INSTANCE.objc_msgSend_stret(mainWindow, uid);

            double height = size.height.doubleValue() * 0.9;
            double width = size.width.doubleValue() * 0.9;
            double x = size.width.doubleValue() * 0.05 + size.x.doubleValue();
            double y = size.height.doubleValue() * 0.05 + size.y.doubleValue();

            Pointer window = new Pointer(send(objC.objc_getClass("NSWindow"), "alloc"));

            Rct r = new Rct(x, y, width, height);
            int mode = 15;
            int backingstoreBuffered = 2;

	    send(window,
                "initWithContentRect:styleMask:backing:defer:",
                r, mode, backingstoreBuffered, false
            );
            send(window, "setTitle:", nsString("Browser demo"));
            Pointer webViewClass = objC.objc_getClass("WebView");
            long webViewId = send(webViewClass, "alloc");
            Pointer webView = new Pointer(webViewId);
            send(webView, "init");

            send(window, "setContentView:", webView);
            send(window, "makeKeyAndOrderFront:", (Object) null);
            return webView;
        }
    }

    public static final class Rct extends Structure implements Structure.ByValue {
        public Flt x;
        public Flt y;
        public Flt width;
        public Flt height;

        public Rct() {
        }

        public Rct(double x, double y, double width, double height) {
            this.x = new Flt(x);
            this.y = new Flt(y);
            this.width = new Flt(width);
            this.height = new Flt(height);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("x", "y", "width", "height");
        }
    }

    public static final class Flt extends Number implements NativeMapped {

        private static final boolean SMALL = Native.LONG_SIZE == 4;
        private final double number;

        public Flt() {
            this(0);
        }

        public Flt(double d) {
            number = d;
        }

        @Override
        public float floatValue() {
            return (float) number;
        }

        @Override
        public double doubleValue() {
            return number;
        }

        @Override
        public int intValue() {
            return (int) number;
        }

        @Override
        public long longValue() {
            return (long) number;
        }

        @Override
        public Object fromNative(Object o, FromNativeContext fromNativeContext) {
            return new Flt(((Number) o).doubleValue());
        }

        @Override
        public Object toNative() {
            return SMALL ? floatValue() : number;
        }

        @Override
        public Class<?> nativeType() {
            return SMALL ? Float.class : Double.class;
        }

        @Override
        public String toString() {
            return Double.toString(number);
        }
    }

    private static void ensureHttpAccess(URI page) {
        if (!"http".equals(page.getScheme())) {
            return;
        }
        ObjC objC = ObjC.INSTANCE;

        final Pointer nsBundle = objC.objc_getClass("NSBundle");
        final Pointer nsNumber = objC.objc_getClass("NSNumber");
        final Pointer nsDictionary = objC.objc_getClass("NSMutableDictionary");


        final Pointer mainBundle = new Pointer(send(nsBundle, "mainBundle"));
        final Pointer info = new Pointer(send(mainBundle, "infoDictionary"));

        final Pointer nsAppTransportSecurity = nsString("NSAppTransportSecurity");
        final Pointer nsAllowArbitraryLoads = nsString("NSAllowsArbitraryLoads");
        final long nsTrue = send(nsNumber, "numberWithBool:", 1);

        final long rawDict = send(info, "objectForKey:", nsAppTransportSecurity);
        final Pointer dict;
        if (rawDict == 0) {
            dict = new Pointer(send(nsDictionary, "dictionaryWithCapacity:", 1));
            send(info, "setValue:forKey:", dict, nsAppTransportSecurity);
            send(dict, "setObject:forKey:", nsTrue, nsAllowArbitraryLoads);
        }
    }
}
