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
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
package org.netbeans.html.json.spi;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import net.java.html.BrwsrCtx;
import net.java.html.json.Function;
import net.java.html.json.Model;

/** Describes a function provided by the {@link Model} and 
 * annotated by {@link Function} annotation.
 *
 * @author Jaroslav Tulach
 */
public abstract class FunctionBinding {
    FunctionBinding() {
    }
    
    /** Returns name of the function.
     * @return function name
     */
    public abstract String getFunctionName();
    
    /**
     * Calls the function provided data associated with current element, as well
     * as information about the event that triggered the event.
     *
     * @param data data associated with selected element
     * @param ev event (with additional properties) that triggered the event
     */
    public abstract void call(Object data, Object ev);
    
    /** Returns identical version of the binding, but one that holds on the
     * original model object via weak reference.
     * 
     * @return binding that uses weak reference
     * @since 1.1
     */
    public abstract FunctionBinding weak();

    static <M> FunctionBinding registerFunction(String name, int index, M model, Proto.Type<M> access) {
        return new Impl<M>(model, name, index, access);
    }
    
    private static abstract class AImpl<M> extends FunctionBinding {
        final String name;
        final Proto.Type<M> access;
        final int index;

        public AImpl(String name, int index, Proto.Type<M> access) {
            this.name = name;
            this.index = index;
            this.access = access;
        }
        
        protected abstract M model();

        @Override
        public String getFunctionName() {
            return name;
        }

        @Override
        public void call(final Object data, final Object ev) {
            final M model = model();
            if (model == null) {
                return;
            }
            BrwsrCtx ctx = access.protoFor(model).getContext();
            class Dispatch implements Runnable {
                @Override
                public void run() {
                    try {
                        access.call(model, index, data, ev);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
            ctx.execute(new Dispatch());
        }
    }
    
    private static final class Impl<M> extends AImpl<M> {
        private final M model;

        public Impl(M model, String name, int index, Proto.Type<M> access) {
            super(name, index, access);
            this.model = model;
        }

        @Override
        protected M model() {
            return model;
        }

        @Override
        public FunctionBinding weak() {
            return new Weak(model, name, index, access);
        }
    }
    
    private static final class Weak<M> extends AImpl<M> {
        private final Reference<M> ref;
        
        public Weak(M model, String name, int index, Proto.Type<M> access) {
            super(name, index, access);
            this.ref = new WeakReference<M>(model);
        }

        @Override
        protected M model() {
            return ref.get();
        }

        @Override
        public FunctionBinding weak() {
            return this;
        }
    }
}
