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
package net.java.html.json.tests;

import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;
import org.netbeans.html.json.tck.KOTest;
import static net.java.html.json.tests.Utils.assertEquals;
import static net.java.html.json.tests.Utils.assertNotNull;
import static net.java.html.json.tests.Utils.assertTrue;
import static net.java.html.json.tests.Utils.fail;

/** Testing support of WebSocket communication.
 *
 * @author Jaroslav Tulach
 */
@Model(className = "WebSocketik", targetId="", properties = {
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
                JSONTest.class, "{'firstName': 'Mitar', 'sex': '$0' }", 
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
        
        assertEquals("Mitar", p.getFirstName(), "Unexpected: " + p.getFirstName());
        assertEquals(Sex.FEMALE, p.getSex(), "Expecting FEMALE: " + p.getSex());

        if (js.getOpen() == 2) {
            // close the socket
            js.querySex(url, null);
            js.setOpen(3);
        }
        
        if (js.getFetchedResponse() == null) {
            throw new InterruptedException();
        }
        assertEquals("null", js.getFetchedResponse(), "Should be null: " + js.getFetchedResponse());
    }
    
    @KOTest public void errorUsingWebSocket() throws Throwable {
        if (js == null) {
            js = Models.bind(new WebSocketik(), newContext());
            js.applyBindings();

            js.setFetched(null);
            js.querySex("http://wrong.protocol", null);
        }

        if (js.getFetchedResponse() == null) {
            throw new InterruptedException();
        }

        assertNotNull(js.getFetchedResponse(), "Error reported");
    }

    @KOTest public void haveToOpenTheWebSocket() throws Throwable {
        js = Models.bind(new WebSocketik(), newContext());
        js.applyBindings();

        js.setFetched(null);
        try {
            js.querySex("http://wrong.protocol", Sex.MALE);
            fail("Should throw an exception");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("not open"), "Expecting 'not open' msg: " + ex.getMessage());
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
