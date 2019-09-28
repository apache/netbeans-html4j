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
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class ServerTest {
    @Test
    public void useAsAServer() throws Exception {
        final Thread main = Thread.currentThread();
        final int[] loaded = { 0 };

        int serverPort = selectFreePort();

        Browser server = new Browser(
            new Browser.Config()
            .command("NONE")
            .port(serverPort)
        );
        BrowserBuilder builder = BrowserBuilder.newBrowser(server)
            .loadPage("server.html")
            .loadFinished(() -> {
                setLoaded("" + ++loaded[0]);
                closeSoon(5000);
            });
        builder.showAndWait();

        URL connect = new URL("http://localhost:" + serverPort);
        InputStream is = connect.openStream();
        Assert.assertNotNull(is, "Connection opened");
        byte[] arr = new byte[4096];
        int len = is.read(arr);
        is.close();

        final String page = new String(arr, 0, len, "UTF-8");
        assertTrue(page.contains("<h1>Server</h1>"), "Server page loaded OK:\n" + page);

        show(connect.toURI());

        awaitLoaded(1, loaded);
        assertEquals(loaded[0], 1, "Connection has been opened");

        show(connect.toURI());

        awaitLoaded(2, loaded);
        assertEquals(loaded[0], 2, "Second connection has been opened");

        server.close();
        try {
            InputStream unavailable = connect.openStream();
            fail("Stream can no longer be opened: " + unavailable);
        } catch (IOException ex) {
            // OK
        }
    }

    private static int selectFreePort() throws IOException {
        ServerSocket temp = new ServerSocket();
        temp.bind(null);
        int port = temp.getLocalPort();
        temp.close();
        return port;
    }

    private static void awaitLoaded(int expected, int[] counter) throws InterruptedException {
        int cnt = 100;
        while (cnt-- > 0 && counter[0] != expected) {
            Thread.sleep(100);
        }
    }

    @JavaScriptBody(args = { "value" }, body = "document.getElementById('loaded').innerHTML = value;")
    private static native void setLoaded(String value);

    @JavaScriptBody(args = { "ms" }, body = "window.setTimeout(function() { window.close(); }, ms);")
    private static native void closeSoon(int ms);

    private static void show(URI page) throws IOException {
        IOException one, two;
        try {
            String ui = System.getProperty("os.name").contains("Mac")
                    ? "Cocoa" : "GTK";
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
            Show.show(null, page);
        } catch (IOException ex) {
            two.initCause(one);
            ex.initCause(two);
            throw ex;
        }
    }
}
