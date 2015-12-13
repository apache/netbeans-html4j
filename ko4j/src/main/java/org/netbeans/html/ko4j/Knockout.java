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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import net.java.html.json.Model;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Provides binding between {@link Model models} and knockout.js running
 * inside a JavaFX WebView. 
 *
 * @author Jaroslav Tulach
 */
@JavaScriptResource("knockout-3.2.0.debug.js")
final class Knockout extends WeakReference<Object> {
    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue();
    private static final Set<Knockout> active = Collections.synchronizedSet(new HashSet<Knockout>());

    @JavaScriptBody(args = {"object", "property"}, body = 
        "var ret;\n" + 
        "if (property === null) ret = object;\n" + 
        "else if (object === null) ret = null;\n" + 
        "else ret = object[property];\n" + 
        "return ret ? ko.utils.unwrapObservable(ret) : null;"
    )
    static Object getProperty(Object object, String property) {
        return null;
    }
    
    private PropertyBinding[] props;
    private FunctionBinding[] funcs;
    private Object js;
    private Object strong;

    public Knockout(Object model, Object js, PropertyBinding[] props, FunctionBinding[] funcs) {
        super(model, QUEUE);
        this.js = js;
        this.props = new PropertyBinding[props.length];
        for (int i = 0; i < props.length; i++) {
            this.props[i] = props[i].weak();
        }
        this.funcs = new FunctionBinding[funcs.length];
        for (int i = 0; i < funcs.length; i++) {
            this.funcs[i] = funcs[i].weak();
        }
        active.add(this);
    }
    
    static void cleanUp() {
        for (;;) {
            Knockout ko = (Knockout)QUEUE.poll();
            if (ko == null) {
                return;
            }
            active.remove(ko);
            clean(ko.js);
            ko.js = null;
            ko.props = null;
            ko.funcs = null;
        }
    }
    
    final void hold() {
        strong = get();
    }
    
    final Object getValue(int index) {
        return props[index].getValue();
    }
    
    final void setValue(int index, Object v) {
        if (v instanceof Knockout) {
            v = ((Knockout)v).get();
        }
        props[index].setValue(v);
    }
    
    final void call(int index, Object data, Object ev) {
        funcs[index].call(data, ev);
    }
    
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

    @JavaScriptBody(args = { "id", "bindings" }, body = 
        "var d = window['document'];\n" +
        "var e = id ? d['getElementById'](id) : d['body'];\n" +
        "ko['cleanNode'](e);\n" +
        "ko['applyBindings'](bindings, e);\n" +
        "return bindings['ko4j'];\n"
    )
    native static Object applyBindings(String id, Object bindings);
    
    @JavaScriptBody(args = { "cnt" }, body = 
        "var arr = new Array(cnt);\n" +
        "for (var i = 0; i < cnt; i++) arr[i] = new Object();\n" +
        "return arr;\n"
    )
    native static Object[] allocJS(int cnt);
    
    @JavaScriptBody(
        javacall = true,
        keepAlive = false,
        wait4js = false,
        args = { "ret", "copyFrom", "propNames", "propReadOnly", "propValues", "funcNames" },
        body = 
          "Object.defineProperty(ret, 'ko4j', { value : this });\n"
        + "function koComputed(index, name, readOnly, value) {\n"
        + "  var orig = copyFrom ? copyFrom[name] : null;\n"
        + "  if (!ko['isObservable'](orig)) {\n"
        + "    orig = null;\n"
        + "    var trigger = ko['observable']()['extend']({'notify':'always'});\n"
        + "  } else {\n"
        + "    var trigger = orig;\n"
        + "  }\n"
        + "  function realGetter() {\n"
        + "    var self = ret['ko4j'];\n"
        + "    try {\n"
        + "      var v = self ? self.@org.netbeans.html.ko4j.Knockout::getValue(I)(index) : null;\n"
        + "      return v;\n"
        + "    } catch (e) {\n"
        + "      alert(\"Cannot call getValue on \" + self + \" prop: \" + name + \" error: \" + e);\n"
        + "    }\n"
        + "  }\n"
        + "  var activeGetter = orig ? orig : function() { return value; };\n"
        + "  var bnd = {\n"
        + "    'read': function() {\n"
        + "      trigger();\n"
        + "      if (orig) {\n"
        + "        var r = orig();\n"
        + "      } else {\n"
        + "        var r = activeGetter();\n"
        + "        activeGetter = realGetter;\n"
        + "      }\n"
        + "      if (r) try { var br = r.valueOf(); } catch (err) {}\n"
        + "      return br === undefined ? r: br;\n"
        + "    },\n"
        + "    'owner': ret\n"
        + "  };\n"
        + "  if (!readOnly) {\n"
        + "    function write(val) {\n"
        + "      if (orig) orig(val);\n"
        + "      var self = ret['ko4j'];\n"
        + "      if (!self) return;\n"
        + "      var model = val ? val['ko4j'] : null;\n"
        + "      self.@org.netbeans.html.ko4j.Knockout::setValue(ILjava/lang/Object;)(index, model ? model : val);\n"
        + "    };\n"
        + "    bnd['write'] = write;\n"
        + "    if (orig) {\n"
        + "      write(orig());\n"
        + "    }\n"
        + "  };\n"
        + "  var cmpt = ko['computed'](bnd);\n"
        + "  cmpt['valueHasMutated'] = function(val) {\n"
        + "    if (arguments.length === 1) activeGetter = function() { return val; };\n"
        + "    trigger(val);\n"
        + "  };\n"
        + "  ret[name] = cmpt;\n"
        + "}\n"
        + "for (var i = 0; i < propNames.length; i++) {\n"
        + "  koComputed(i, propNames[i], propReadOnly[i], propValues[i]);\n"
        + "}\n"
        + "function koExpose(index, name) {\n"
        + "  ret[name] = function(data, ev) {\n"
        + "    var self = ret['ko4j'];\n"
        + "    if (!self) return;\n"
        + "    self.@org.netbeans.html.ko4j.Knockout::call(ILjava/lang/Object;Ljava/lang/Object;)(index, data, ev);\n"
        + "  };\n"
        + "}\n"
        + "for (var i = 0; i < funcNames.length; i++) {\n"
        + "  koExpose(i, funcNames[i]);\n"
        + "}\n"
        )
    native void wrapModel(
        Object ret, Object copyFrom,
        String[] propNames, Boolean[] propReadOnly, Object propValues,
        String[] funcNames
    );
    
    @JavaScriptBody(args = { "js" }, wait4js = false, body = 
        "delete js['ko4j'];\n" +
        "for (var p in js) {\n" +
        "  delete js[p];\n" +
        "};\n" +
        "\n"
    )
    private static native void clean(Object js);
    
    @JavaScriptBody(args = { "o" }, body = "return o['ko4j'] ? o['ko4j'] : o;")
    private static native Object toModelImpl(Object wrapper);
    static Object toModel(Object wrapper) {
        Object o = toModelImpl(wrapper);
        if (o instanceof Knockout) {
            return ((Knockout)o).get();
        } else {
            return o;
        }
    }
}
