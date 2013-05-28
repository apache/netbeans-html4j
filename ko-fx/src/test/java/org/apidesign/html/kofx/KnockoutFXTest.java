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
package org.apidesign.html.kofx;

import java.util.Map;
import net.java.html.BrwsrCtx;
import netscape.javascript.JSObject;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.tck.KnockoutTCK;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.lookup.ServiceProvider;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = KnockoutTCK.class)
public final class KnockoutFXTest extends KnockoutTCK {
    public KnockoutFXTest() {
    }

    @Factory public static Object[] compatibilityTests() {
        return VMTest.newTests().withClasses(testClasses()).withLaunchers("fxbrwsr").build();
    }

    @Override
    public BrwsrCtx createContext() {
        FXContext fx = new FXContext();
        return Contexts.newBuilder().
            register(Technology.class, fx, 10).
            register(Transfer.class, fx, 10).
            build();
    }

    @Override
    public Object createJSON(Map<String, Object> values) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return json;
    }

    private static JSObject eval;
    @Override
    public Object executeScript(String script, Object[] arguments) {
        if (eval == null) {
            eval = (JSObject) Knockout.web().executeScript("(function(scope) {"
                + "  scope.jko = {};"
                + "  scope.jko.compute = function(s, args) { var f = new Function(s); return f.apply(null, args); }"
                + "})(window); window.jko;");
        }
        return eval.call("compute", script, arguments);
    }
    
}
