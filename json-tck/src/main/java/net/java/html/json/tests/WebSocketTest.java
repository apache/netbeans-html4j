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
package net.java.html.json.tests;

import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import org.apidesign.html.json.tck.KOTest;

/** Testing support of WebSocket communication.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "WebSocketik", properties = {
    @Property(name = "fetched", type = Person.class),
    @Property(name = "fetchedCount", type = int.class),
    @Property(name = "open", type = int.class),
    @Property(name = "fetchedResponse", type = String.class),
    @Property(name = "fetchedSex", type = Sex.class, array = true)
})
public final class WebSocketTest {
    private WebSocketik js;
    private String url;
    
    @OnReceive(url = "{url}", data = Sex.class, method = "WebSocket", onError = "error")
    static void querySex(WebSocketik model, Person data) {
        if (data == null) {
            model.setOpen(1);
        } else {
            model.setFetched(data);
        }
    }
    
    @KOTest public void connectUsingWebSocket() throws Throwable {
        if (js == null) {
            url = Utils.prepareURL(
                JSONTest.class, "{'firstName': 'Mitar', 'sex': $0}", 
                "application/javascript",
                "protocol:ws"
            );
            
            js = Models.bind(new WebSocketik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            
            // connects to the server
            js.querySex(url, null);
        }
        
        if (bailOutIfNotSupported(js)) {
            return;
        }
        
        if (js.getOpen() == 0) {
            throw new InterruptedException();
        }
        if (js.getOpen() == 1) {
            // send a query to the server
            js.querySex(url, Sex.FEMALE);
            js.setOpen(2);
        }
    
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Mitar".equals(p.getFirstName()) : "Unexpected: " + p.getFirstName();
        assert Sex.FEMALE.equals(p.getSex()) : "Expecting FEMALE: " + p.getSex();

        if (js.getOpen() == 2) {
            // close the socket
            js.querySex(url, null);
            js.setOpen(3);
        }
        
        if (js.getFetchedResponse() == null) {
            throw new InterruptedException();
        }
        assert "null".equals(js.getFetchedResponse()) : "Should be null: " + js.getFetchedResponse();
    }
    
    @KOTest public void errorUsingWebSocket() throws Throwable {
        js = Models.bind(new WebSocketik(), newContext());
        js.applyBindings();

        js.setFetched(null);
        js.querySex("http://wrong.protocol", null);

        assert js.getFetchedResponse() != null : "Error reported";
    }

    @KOTest public void haveToOpenTheWebSocket() throws Throwable {
        js = Models.bind(new WebSocketik(), newContext());
        js.applyBindings();

        js.setFetched(null);
        try {
            js.querySex("http://wrong.protocol", Sex.MALE);
            assert false : "Should throw an exception";
        } catch (IllegalStateException ex) {
            assert ex.getMessage().contains("not open") : "Expecting 'not open' msg: " + ex.getMessage();
        }
    }
    
    static void error(WebSocketik model, Exception ex) {
        if (ex != null) {
            model.setFetchedResponse(ex.getClass() + ":" + ex.getMessage());
        } else {
            model.setFetchedResponse("null");
        }
    }
    
    private static BrwsrCtx newContext() {
        return Utils.newContext(WebSocketTest.class);
    }

    private boolean bailOutIfNotSupported(WebSocketik js) {
        if (js.getFetchedResponse() == null) {
            return false;
        }
        return js.getFetchedResponse().contains("UnsupportedOperationException") &&
            Utils.canFailWebSockets(WebSocketTest.class);
    }
    
}
