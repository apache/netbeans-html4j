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
package org.netbeans.html.json.impl;

import java.util.concurrent.Callable;

/** Super type for those who wish to receive JSON messages.
 *
 * @author Jaroslav Tulach
 */
public abstract class RcvrJSON {
    protected void onOpen(MsgEvnt msg) {}
    protected abstract void onMessage(MsgEvnt msg);
    protected void onClose(MsgEvnt msg) {}
    protected abstract void onError(MsgEvnt msg);
    
    public abstract static class MsgEvnt {
        MsgEvnt() {
        }
        
        public Throwable getError() {
            return null;
        }
        
        public final Exception getException() {
            Throwable t = getError();
            if (t instanceof Exception) {
                return (Exception)t;
            }
            if (t == null) {
                return null;
            }
            return new Exception(t);
        }
        
        public Object[] getValues() {
            return null;
        }
        
        public abstract void dispatch(RcvrJSON r);
        
        public static MsgEvnt createError(final Throwable t) {
            return new MsgEvnt() {
                @Override
                public Throwable getError() {
                    return t;
                }

                @Override
                public void dispatch(RcvrJSON r) {
                    r.onError(this);
                }
            };
        }
        
         public static MsgEvnt createMessage(final Object value) {
            return new MsgEvnt() {
                private Object val = value;

                @Override
                public Object[] getValues() {
                    if (val instanceof Callable) {
                        try {
                            val = ((Callable)val).call();
                        } catch (Exception ex) {
                            throw new IllegalStateException("Cannot compute " + val, ex);
                        }
                    }
                    return val instanceof Object[] ? (Object[])val : new Object[] { val };
                }
                
                @Override
                public void dispatch(RcvrJSON r) {
                    r.onMessage(this);
                }
            };
        }
        
        public static MsgEvnt createOpen() {
            return new MsgEvnt() {
                @Override
                public void dispatch(RcvrJSON r) {
                    r.onOpen(this);
                }
            };
        }

        public static MsgEvnt createClose() {
            return new MsgEvnt() {
                @Override
                public void dispatch(RcvrJSON r) {
                    r.onClose(this);
                }
            };
        }
    } }
