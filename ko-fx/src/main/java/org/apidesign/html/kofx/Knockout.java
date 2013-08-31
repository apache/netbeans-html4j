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

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import net.java.html.json.Model;
import netscape.javascript.JSObject;
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
    static final JSObject KObject;
    static {
        Console.register();
        KObject = (JSObject) kObj();
    }

    static Object toArray(Object[] arr) {
        return KObject.call("array", arr);
    }
    
    @JavaScriptBody(args = { "model", "prop" }, body =
          "if (model) {\n"
        + "  var koProp = model[prop];\n"
        + "  if (koProp && koProp['valueHasMutated']) {\n"
        + "    koProp['valueHasMutated']();\n"
        + "  }\n"
        + "}\n"
    )
    public native static void valueHasMutated(JSObject model, String prop);

    @JavaScriptBody(args = { "bindings" }, body = "ko.applyBindings(bindings);")
    native static void applyBindings(Object bindings);
    
    @JavaScriptBody(args = {}, body =
              "  var k = {};"
            + "  k.array= function() {"
            + "    return Array.prototype.slice.call(arguments);"
            + "  };"
            + "  return k;"
    )
    private static native Object kObj();
        
        
    @JavaScriptBody(
        javacall = true,
        args = {"model", "propNames", "propReadOnly", "propArr", "funcNames", "funcArr"},
        body
        = "var ret = {};\n"
        + "ret['ko-fx.model'] = model;\n"
        + "function koComputed(name, readOnly, prop) {\n"
        + "  var bnd = {"
        + "    read: function() {"
        + "      try {"
        + "        var v = prop.@org.apidesign.html.json.spi.PropertyBinding::getValue()();"
        + "        return v;"
        + "      } catch (e) {"
        + "        alert(\"Cannot call getValue on \" + model + \" prop: \" + name + \" error: \" + e);"
        + "      }"
        + "    },"
        + "    owner: ret\n"
        + "  };\n"
        + "  if (!readOnly) {\n"
        + "    bnd.write = function(val) {\n"
        + "      prop.@org.apidesign.html.json.spi.PropertyBinding::setValue(Ljava/lang/Object;)(val);\n"
        + "    };"
        + "  };"
        + "  ret[name] = ko.computed(bnd);"
        + "}\n"
        + "for (var i = 0; i < propNames.length; i++) {\n"
        + "  koComputed(propNames[i], propReadOnly[i], propArr[i]);\n"
        + "}\n"
        + "function koExpose(name, func) {\n"
        + "  ret[name] = function(data, ev) {\n"
        + "    func.@org.apidesign.html.json.spi.FunctionBinding::call(Ljava/lang/Object;Ljava/lang/Object;)(data, ev);\n"
        + "  };\n"
        + "}\n"
        + "for (var i = 0; i < funcNames.length; i++) {\n"
        + "  koExpose(funcNames[i], funcArr[i]);\n"
        + "}\n"
        + "return ret;\n"
        )
    static native JSObject wrapModel(
        Object model,
        String[] propNames, boolean[] propReadOnly, PropertyBinding[] propArr,
        String[] funcNames, FunctionBinding[] funcArr
    );
}
