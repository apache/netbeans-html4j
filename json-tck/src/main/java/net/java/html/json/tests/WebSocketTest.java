/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
