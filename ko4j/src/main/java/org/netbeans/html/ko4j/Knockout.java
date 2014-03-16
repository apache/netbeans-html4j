/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package org.netbeans.html.ko4j;

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import net.java.html.json.Model;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.PropertyBinding;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Provides binding between {@link Model models} and knockout.js running
 * inside a JavaFX WebView. 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@JavaScriptResource("knockout-2.2.1.js")
final class Knockout {
    @JavaScriptBody(args = { "model", "prop", "oldValue", "newValue" }, 
        wait4js = false,
        body =
          "if (model) {\n"
        + "  var koProp = model[prop];\n"
        + "  if (koProp && koProp['valueHasMutated']) {\n"
        + "    if ((oldValue !== null || newValue !== null)) {\n"
        + "      koProp['valueHasMutated'](newValue);\n"
        + "    } else if (koProp['valueHasMutated']) {\n"
        + "      koProp['valueHasMutated']();\n"
        + "    }\n"
        + "  }\n"
        + "}\n"
    )
    public native static void valueHasMutated(
        Object model, String prop, Object oldValue, Object newValue
    );

    @JavaScriptBody(args = { "bindings" }, wait4js = false, body = 
        "ko.applyBindings(bindings);\n"
    )
    native static void applyBindings(Object bindings);
    
    @JavaScriptBody(args = { "cnt" }, body = 
        "var arr = new Array(cnt);\n" +
        "for (var i = 0; i < cnt; i++) arr[i] = new Object();\n" +
        "return arr;\n"
    )
    native static Object[] allocJS(int cnt);
    
    @JavaScriptBody(
        javacall = true,
        wait4js = false,
        args = {"ret", "model", "propNames", "propReadOnly", "propValues", "propArr", "funcNames", "funcArr"},
        body = 
          "ret['ko-fx.model'] = model;\n"
        + "function koComputed(name, readOnly, value, prop) {\n"
        + "  function realGetter() {\n"
        + "    try {\n"
        + "      var v = prop.@org.apidesign.html.json.spi.PropertyBinding::getValue()();\n"
        + "      return v;\n"
        + "    } catch (e) {\n"
        + "      alert(\"Cannot call getValue on \" + model + \" prop: \" + name + \" error: \" + e);\n"
        + "    }\n"
        + "  }\n"
        + "  var activeGetter = function() { return value; };\n"
        + "  var bnd = {\n"
        + "    read: function() {\n"
        + "      var r = activeGetter();\n"
        + "      activeGetter = realGetter;\n"
        + "      return r;\n"
        + "    },\n"
        + "    owner: ret\n"
        + "  };\n"
        + "  if (!readOnly) {\n"
        + "    bnd.write = function(val) {\n"
        + "      prop.@org.apidesign.html.json.spi.PropertyBinding::setValue(Ljava/lang/Object;)(val);\n"
        + "    };\n"
        + "  };\n"
        + "  var cmpt = ko.computed(bnd);\n"
        + "  var vhm = cmpt.valueHasMutated;\n"
        + "  cmpt.valueHasMutated = function(val) {\n"
        + "    if (arguments.length === 1) activeGetter = function() { return val; };\n"
        + "    vhm();\n"
        + "  };\n"
        + "  ret[name] = cmpt;\n"
        + "}\n"
        + "for (var i = 0; i < propNames.length; i++) {\n"
        + "  koComputed(propNames[i], propReadOnly[i], propValues[i], propArr[i]);\n"
        + "}\n"
        + "function koExpose(name, func) {\n"
        + "  ret[name] = function(data, ev) {\n"
        + "    func.@org.apidesign.html.json.spi.FunctionBinding::call(Ljava/lang/Object;Ljava/lang/Object;)(data, ev);\n"
        + "  };\n"
        + "}\n"
        + "for (var i = 0; i < funcNames.length; i++) {\n"
        + "  koExpose(funcNames[i], funcArr[i]);\n"
        + "}\n"
        )
    static native void wrapModel(
        Object ret, Object model,
        String[] propNames, boolean[] propReadOnly, Object propValues, PropertyBinding[] propArr,
        String[] funcNames, FunctionBinding[] funcArr
    );
    
    @JavaScriptBody(args = { "o" }, body = "return o['ko-fx.model'] ? o['ko-fx.model'] : o;")
    private static native Object toModelImpl(Object wrapper);
    static Object toModel(Object wrapper) {
        return toModelImpl(wrapper);
    }
    
    @JavaScriptBody(args = {}, body = "if (window.WebSocket) return true; else return false;")
    static final boolean areWebSocketsSupported() {
        return false;
    }
}
