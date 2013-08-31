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

import java.util.logging.Level;
import java.util.logging.Logger;
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
public final class Knockout {
    private static final Logger LOG = Logger.getLogger(Knockout.class.getName());
    /** used by tests */
    static Knockout next;
    private final Object model;

    Knockout(Object model) {
        this.model = model == null ? this : model;
    }
    
    public Object koData() {
        return model;
    }

    static Object toArray(Object[] arr) {
        return InvokeJS.KObject.call("array", arr);
    }
    
    private static int cnt;
    public static <M> Knockout createBinding(Object model) {
        Object bindings = InvokeJS.create(model, ++cnt);
        return new Knockout(bindings);
    }

    static JSObject wrapModel(
        Object model, 
        String[] propNames, boolean[] propReadOnly, PropertyBinding[] propArr, 
        String[] funcNames, FunctionBinding[] funcArr
    ) {
        return InvokeJS.wrapModel(model, propNames, propReadOnly, propArr, funcNames, funcArr);
    }
    

    public void valueHasMutated(String prop) {
        valueHasMutated((JSObject) model, prop);
    }
    public static void valueHasMutated(JSObject model, String prop) {
        LOG.log(Level.FINE, "property mutated: {0}", prop);
        try {
            if (model != null) {
            Object koProp = model.getMember(prop);
                if (koProp instanceof JSObject) {
                    ((JSObject)koProp).call("valueHasMutated");
                }
            }
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "valueHasMutated failed for " + model + " prop: " + prop, t);
        }
    }

    static void bind(
        Object bindings, Object model, PropertyBinding pb, boolean primitive, boolean array
    ) {
        final String prop = pb.getPropertyName();
        try {
            InvokeJS.bind(bindings, pb, prop, "getValue", pb.isReadOnly() ? null : "setValue", primitive, array);
            
            LOG.log(Level.FINE, "binding defined for {0}: {1}", new Object[]{prop, ((JSObject)bindings).getMember(prop)});
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "binding failed for {0} on {1}", new Object[]{prop, bindings});
        }
    }
    static void expose(Object bindings, FunctionBinding f) {
        final String prop = f.getFunctionName();
        try {
            InvokeJS.expose(bindings, f, prop, "call");
        } catch (Throwable ex) {
            LOG.log(Level.SEVERE, "Cannot define binding for " + prop + " in model " + f, ex);
        }
    }
    
    static void applyBindings(Object bindings) {
        InvokeJS.applyBindings(bindings);
    }
    
    private static final class InvokeJS {
        static final JSObject KObject;

        static {
            Console.register();
            KObject = (JSObject) kObj();
        }
        
        @JavaScriptBody(args = { "s" }, body = "return eval(s);")
        private static native Object exec(String s);
        
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
        
        
        @JavaScriptBody(args = { "value", "cnt " }, body =
                  "    var ret = {};"
/*              + "    ret.toString = function() { return 'KObject' + cnt + ' value: ' + value + ' props: ' + Object.keys(this); }; " */
                + "    ret['ko-fx.model'] = value;"
                + "    return ret;"
        )
        static native Object create(Object value, int cnt);
        
        @JavaScriptBody(args = { "bindings", "model", "prop", "sig" }, body = 
                "    bindings[prop] = function(data, ev) {"
              //            + "         console.log(\"  callback on prop: \" + prop);"
              + "      model[sig](data, ev);"
              + "    };"
        )
        static native Object expose(Object bindings, Object model, String prop, String sig);

        
        @JavaScriptBody(args = { "bindings", "model", "prop", "getter", "setter", "primitive", "array" }, body = 
                  "    var bnd = {"
                + "      read: function() {"
                + "      try {"
                + "        var v = model[getter]();"
        //        + "      console.log(\" getter value \" + v + \" for property \" + prop);"
        //        + "      try { v = v.koData(); } catch (ignore) {"
        //        + "        console.log(\"Cannot convert to koData: \" + ignore);"
        //        + "      };"
        //        + "      console.log(\" getter ret value \" + v);"
        //        + "      for (var pn in v) {"
        //        + "         console.log(\"  prop: \" + pn + \" + in + \" + v + \" = \" + v[pn]);"
        //        + "         if (typeof v[pn] == \"function\") console.log(\"  its function value:\" + v[pn]());"
        //        + "      }"
        //        + "      console.log(\" all props printed for \" + (typeof v));"
                + "        return v;"
                + "      } catch (e) {"
                + "        alert(\"Cannot call \" + getter + \" on \" + model + \" error: \" + e);"
                + "      }"
                + "    },"
                + "    owner: bindings"
        //        + "  ,deferEvaluation: true"
                + "    };"
                + "    if (setter != null) {"
                + "      bnd.write = function(val) {"
                + "        model[setter](primitive ? new Number(val) : val);"
                + "      };"
                + "    };"
                + "    bindings[prop] = ko.computed(bnd);"
        )
        static native void bind(Object binding, Object model, String prop, String getter, String setter, boolean primitive, boolean array);

        @JavaScriptBody(args = { "bindings" }, body = "ko.applyBindings(bindings);")
        private static native void applyBindings(Object bindings);
    }
}
