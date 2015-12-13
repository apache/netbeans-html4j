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
package org.netbeans.html.json.impl;

import net.java.html.BrwsrCtx;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Proto;

/**
 *
 * @author Jaroslav Tulach
 */
public abstract class PropertyBindingAccessor {
    private static PropertyBindingAccessor DEFAULT;

    protected PropertyBindingAccessor() {
        if (DEFAULT != null) throw new IllegalStateException();
        DEFAULT = this;
    }

    static {
        JSON.initClass(PropertyBinding.class);
    }

    protected abstract <M> PropertyBinding newBinding(
        Proto.Type<M> access, Bindings<?> bindings, String name, int index, M model, boolean readOnly
    );
    protected abstract JSONCall newCall(
        BrwsrCtx ctx, RcvrJSON callback,
        String headers, String urlBefore, String urlAfter,
        String method, Object data
    );

    protected abstract Bindings bindings(Proto proto, boolean initialize, Object copyFrom);
    protected abstract void notifyChange(Proto proto, int propIndex);
    protected abstract Proto findProto(Proto.Type<?> type, Object object);
    protected abstract <Model> Model cloneTo(Proto.Type<Model> type, Model model, BrwsrCtx c);
    protected abstract Object read(Proto.Type<?> from, BrwsrCtx c, Object data);

    static Bindings getBindings(Proto proto, boolean initialize, Object copyFrom) {
        return DEFAULT.bindings(proto, initialize, copyFrom);
    }

    static void notifyProtoChange(Proto proto, int propIndex) {
        DEFAULT.notifyChange(proto, propIndex);
    }

    static <M> PropertyBinding create(
        Proto.Type<M> access, Bindings<?> bindings, String name, int index, M model , boolean readOnly
    ) {
        return DEFAULT.newBinding(access, bindings, name, index, model, readOnly);
    }
    public static JSONCall createCall(
        BrwsrCtx ctx, RcvrJSON callback,
        String headers, String urlBefore, String urlAfter,
        String method, Object data
    ) {
        return DEFAULT.newCall(ctx, callback, headers, urlBefore, urlAfter, method, data);
    }
    static Proto protoFor(Proto.Type<?> type, Object object) {
        return DEFAULT.findProto(type, object);
    }
    static <Model> Model clone(Proto.Type<Model> type, Model model, BrwsrCtx c) {
        return DEFAULT.cloneTo(type, model, c);
    }
    static Object readFrom(Proto.Type<?> from, BrwsrCtx c, Object data) {
        return DEFAULT.read(from, c, data);
    }
}
