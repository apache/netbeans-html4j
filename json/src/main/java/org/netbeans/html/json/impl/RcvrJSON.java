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
