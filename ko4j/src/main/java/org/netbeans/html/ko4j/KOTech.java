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

import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Technology;
import static org.netbeans.html.ko4j.KO4J.LOG;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 *
 * @author Jaroslav Tulach
 */
@Contexts.Id("ko4j")
final class KOTech
implements Technology.BatchInit<Object>, Technology.ValueMutated<Object> {
    private Object[] jsObjects;
    private int jsIndex;

    public KOTech() {
    }
    
    @Override
    public Object wrapModel(Object model, PropertyBinding[] propArr, FunctionBinding[] funcArr) {
        String[] propNames = new String[propArr.length];
        boolean[] propReadOnly = new boolean[propArr.length];
        Object[] propValues = new Object[propArr.length];
        for (int i = 0; i < propNames.length; i++) {
            propNames[i] = propArr[i].getPropertyName();
            propReadOnly[i] = propArr[i].isReadOnly();
            propValues[i] = propArr[i].getValue();
        }
        String[] funcNames = new String[funcArr.length];
        for (int i = 0; i < funcNames.length; i++) {
            funcNames[i] = funcArr[i].getFunctionName();
        }
        Object ret = getJSObject();
        Knockout.wrapModel(new Knockout(model, ret, propArr, funcArr),
            ret, 
            propNames, propReadOnly, propValues,
            funcNames
        );
        return ret;
    }
    
    private Object getJSObject() {
        int len = 64;
        if (jsObjects != null && jsIndex < (len = jsObjects.length)) {
            Object ret = jsObjects[jsIndex];
            jsObjects[jsIndex] = null;
            jsIndex++;
            return ret;
        }
        jsObjects = Knockout.allocJS(len * 2);
        jsIndex = 1;
        Object ret = jsObjects[0];
        jsObjects[0] = null;
        return ret;
    }
    
    @Override
    public Object wrapModel(Object model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(PropertyBinding b, Object model, Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void valueHasMutated(Object data, String propertyName) {
        Knockout.cleanUp();
        Knockout.valueHasMutated(data, propertyName, null, null);
    }
    
    @Override
    public void valueHasMutated(Object data, String propertyName, Object oldValue, Object newValue) {
        Knockout.cleanUp();
        Knockout.valueHasMutated(data, propertyName, oldValue, newValue);
    }

    @Override
    public void expose(FunctionBinding fb, Object model, Object d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyBindings(Object data) {
        Object ko = Knockout.applyBindings(data);
        if (ko instanceof Knockout) {
            ((Knockout)ko).hold();
        }
    }

    @Override
    public Object wrapArray(Object[] arr) {
        return arr;
    }
    
    @Override
    public void runSafe(final Runnable r) {
        LOG.warning("Technology.runSafe has been deprecated. Use BrwsrCtx.execute!");
        r.run();
    }    

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        return modelClass.cast(Knockout.toModel(data));
    }
}
