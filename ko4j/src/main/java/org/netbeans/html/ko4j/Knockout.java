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
package org.netbeans.html.ko4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import net.java.html.json.Model;
import org.netbeans.html.boot.spi.Fn;
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
@JavaScriptResource("knockout-3.5.0.js")
final class Knockout  {

    @JavaScriptBody(args = {"object", "property"}, body =
        """
        var ret;
        if (property === null) ret = object;
        else if (object === null) ret = null;
        else ret = object[property];
        if (typeof ret !== 'undefined' && ret !== null) {
          if (typeof ko !== 'undefined' && ko['utils'] && ko['utils']['unwrapObservable']) {
            return ko['utils']['unwrapObservable'](ret);
          }
          return ret;
        }
        return null;
        """
    )
    static Object getProperty(Object object, String property) {
        return null;
    }

    private PropertyBinding[] props;
    private FunctionBinding[] funcs;
    private Object objs;
    private final Object copyFrom;
    private final Object strong;

    public Knockout(Object model, Object copyFrom, PropertyBinding[] props, FunctionBinding[] funcs) {
        this.strong = model;
        this.props = new PropertyBinding[props.length];
        for (int i = 0; i < props.length; i++) {
            this.props[i] = props[i].weak();
        }
        this.funcs = new FunctionBinding[funcs.length];
        for (int i = 0; i < funcs.length; i++) {
            this.funcs[i] = funcs[i].weak();
        }
        this.copyFrom = copyFrom;
    }

    final Object js() {
        final Fn.Presenter c = Fn.activePresenter();
        Object js = MapObjs.get(objs, c);
        if (js == null) {
            js = initObjs(c, copyFrom);
            objs = MapObjs.put(objs, c, js);
        }
        return js;
    }

    private Object initObjs(Fn.Presenter p, Object copyFrom) {
        String[] propNames = new String[props.length];
        Number[] propInfo = new Number[props.length];
        Object[] propValues = new Object[props.length];
        for (int i = 0; i < propNames.length; i++) {
            propNames[i] = props[i].getPropertyName();
            int info
                    = (props[i].isReadOnly() ? 1 : 0)
                    + (props[i].isConstant() ? 2 : 0);
            propInfo[i] = info;
            Object value = props[i].getValue();
            if (value instanceof Enum) {
                value = value.toString();
            }
            propValues[i] = value;
        }
        String[] funcNames = new String[funcs.length];
        for (int i = 0; i < funcNames.length; i++) {
            funcNames[i] = funcs[i].getFunctionName();
        }
        Object ret = CacheObjs.find(p).getJSObject();
        wrapModel(this,ret, copyFrom, propNames, propInfo, propValues, funcNames);
        return ret;
    }

    static void cleanUp() {
        for (;;) {
            Knockout ko = null;
            if (ko == null) {
                return;
            }
            Object[] both = MapObjs.remove(ko.objs, Fn.activePresenter());
            Object js = both[0];
            ko.objs = both[1];
            clean(js);
            ko.props = null;
            ko.funcs = null;
        }
    }

    final void hold() {
    }

    final Object get() {
        return strong;
    }

    final Object getValue(int index) {
        return props[index].getValue();
    }

    final void setValue(int index, Object v) {
        if (v instanceof Knockout k) {
            v = k.get();
        }
        props[index].setValue(v);
    }

    final void call(int index, Object data, Object ev) {
        funcs[index].call(data, ev);
    }

    private static Fn.Presenter getPresenter(Object obj) {
        if (obj instanceof Fn.Presenter p) {
            return p;
        } else {
            if (obj == null) {
                return null;
            } else {
                return ((Fn.Ref) obj).presenter();
            }
        }
    }

    final void valueHasMutated(final String propertyName, Object oldValue, Object newValue) {
        Object[] all = MapObjs.toArray(objs);
        for (int i = 0; i < all.length; i += 2) {
            Fn.Presenter p = getPresenter(all[i]);
            final Object o = all[i + 1];
            if (p != Fn.activePresenter()) {
                if (p instanceof Executor e) {
                    e.execute(() -> {
                        valueHasMutated(o, propertyName, null, null);
                    });
                } else {
                    Closeable c = Fn.activate(p);
                    try {
                        valueHasMutated(o, propertyName, null, null);
                    } finally {
                        try {
                            c.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }
        valueHasMutated(js(), propertyName, oldValue, newValue);
    }

    @JavaScriptBody(args = { "model", "prop", "oldValue", "newValue" },
        wait4js = false,
        body =
          """
          if (model) {
            var koProp = model[prop];
            if (koProp) {
              var koFire = koProp['valueHasMutated'];
              if (koFire) {
                if (oldValue !== null || newValue !== null) {
                  koFire(newValue);
                } else {
                  koFire();
                }
              }
            }
          }
          """
    )
    private native static void valueHasMutated(
        Object model, String prop, Object oldValue, Object newValue
    );

    final Object applyBindings(String id) {
        return applyBindings(id, js());
    }

    @JavaScriptBody(args = { "id", "bindings" }, body =
        """
        var d = window['document'];
        var e = id ? d['getElementById'](id) : d['body'];
        ko['cleanNode'](e);
        ko['applyBindings'](bindings, e);
        return bindings['ko4j'];
        """
    )
    private native static Object applyBindings(String id, Object bindings);

    @JavaScriptBody(args = { "cnt" }, body =
        """
        function createPromise() {
            var fn;
            var p = new Promise(function (resolve, reject) {
                fn = resolve;
            });
            Object.defineProperty(p, 'ko4j', { value : fn, configurable : true });
            return p;
        }

        var arr = new Array(cnt);
        for (var i = 0; i < cnt; i++) {
            arr[i] = createPromise();
        }
        return arr;
        """
    )
    native static Object[] allocJS(int cnt);

    @JavaScriptBody(
        javacall = true,
        keepAlive = false,
        wait4java = false,
        wait4js = false,
        args = { "thiz", "ret", "copyFrom", "propNames", "propInfo", "propValues", "funcNames" },
        body =
          """
          var resolve = ret['ko4j'];
          Object.defineProperty(ret, 'ko4j', { value : thiz, configurable : false });

          function resolvePromises(obj, fn) {
              if (Array.isArray(obj)) {
                  Promise.all(obj).then(fn);
              } else {
                  Promise.resolve(obj).then(fn);
              }
          }

          function normalValue(r) {
            if (r) {
              try {
                var br = r.valueOf();
              } catch (err) {
              }
            }
            return br === undefined ? r: br;
          }
          function koCopyFrom(index, name, readOnly, orig) {
            var bnd = {
              'read': function() {
                var r = orig();
                return normalValue(r);;
              },
              'owner': ret
            };
            function write(val) {
              var self = ret['ko4j'];
              if (!self) {
                  return;
              }
              var model = val ? val['ko4j'] : null;
              // console.log('write with ' + val + ' begins');
              self.@org.netbeans.html.ko4j.Knockout::setValue(ILjava/lang/Object;)(index, model ? model : val);
              // .finally(function() {
              //   console.log('write with ' + val + ' is finished');
              // });
            }
            if (!readOnly) {
              bnd['write'] = write;
              write(orig());
              orig.subscribe(write);
            };
            var cmpt = ko['computed'](bnd);
            cmpt['valueHasMutated'] = function(val) {
              // console.log('on valueHasMutated with ' + val + ' current value ' + orig());
              if (val == orig()) {
                  return;
              }
              resolvePromises(val, function() {
                orig(val);
              });
            };
            ret[name] = cmpt;
          }
          function koComputed(index, name, readOnly, value) {
            function activeGetter() {
                return value;
            }
            var trigger = ko['observable']()['extend']({'notify':'always'});
            var bnd = {
              'read': function() {
                trigger();
                var r = activeGetter();
                return normalValue(r);;
              },
              'owner': ret
            };
            if (!readOnly) {
              function write(val) {
                var self = ret['ko4j'];
                if (!self) return;
                var model = val ? val['ko4j'] : null;
                self.@org.netbeans.html.ko4j.Knockout::setValue(ILjava/lang/Object;)(index, model ? model : val);
              };
              bnd['write'] = write;
            };
            var cmpt = ko['computed'](bnd);
            cmpt['valueHasMutated'] = function(val) {
              if (arguments.length === 1) {
                value = val;
                trigger(val);
              } else {
                var self = ret['ko4j'];
                if (self) {
                  var futureValue = self.@org.netbeans.html.ko4j.Knockout::getValue(I)(index);
                  Promise.resolve(futureValue).then(function(val, e) {
                    if (e) {
                      alert("Cannot call getValue on " + self + " prop: " + name + " error: " + e);
                    } else {
                      if (value != val) {
                        resolvePromises(val, function() {
                            value = val;
                            trigger(val);
                        });
                      }
                    }
                  });
                }
              }
            };
            ret[name] = cmpt;
          }
          for (var i = 0; i < propNames.length; i++) {
            var pName= propNames[i];
            var isConstant = (propInfo[i] & 2) !== 0;
            var isReadOnly = (propInfo[i] & 1) !== 0;

            if (isConstant) {
              ret[pName] = normalValue(propValues[i]);
            } else {
              var orig = copyFrom ? copyFrom[pName] : null;
              if (ko['isObservable'](orig)) {
                koCopyFrom(i, pName, isReadOnly, orig);
              } else {
                koComputed(i, pName, isReadOnly, propValues[i]);
              }
            }
          }
          function koExpose(index, name) {
            ret[name] = function(data, ev) {
              var self = ret['ko4j'];
              if (!self) return;
              self.@org.netbeans.html.ko4j.Knockout::call(ILjava/lang/Object;Ljava/lang/Object;)(index, data, ev);
            };
          }
          for (var i = 0; i < funcNames.length; i++) {
            koExpose(i, funcNames[i]);
          }
          resolve(null);
          """
        )
    private static native void wrapModel(
        Knockout thiz,
        Object ret, Object copyFrom,
        String[] propNames, Number[] propInfo,
        Object propValues,
        String[] funcNames
    );

    @JavaScriptBody(args = { "js" }, wait4js = false, body =
        """
        delete js['ko4j'];
        for (var p in js) {
          delete js[p];
        };

        """
    )
    private static native void clean(Object js);

    @JavaScriptBody(args = { "o" }, body = "return o['ko4j'] ? o['ko4j'] : o;")
    private static native Object toModelImpl(Object o);
    static Object toModel(Object wrapper) {
        Object o = toModelImpl(wrapper);
        if (o instanceof Knockout k) {
            return k.get();
        } else {
            return o;
        }
    }
}
