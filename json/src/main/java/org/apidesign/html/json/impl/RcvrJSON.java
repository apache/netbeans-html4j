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
package org.apidesign.html.json.impl;

import net.java.html.BrwsrCtx;

/** Super type for those who wish to receive JSON messages.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
        
        public int dataSize() {
            return -1;
        }
        
        public <Data> void dataRead(BrwsrCtx ctx, Class<? extends Data> type, Data[] fillTheArray) {
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
                @Override
                public int dataSize() {
                    if (value instanceof Object[]) {
                        return ((Object[])value).length;
                    } else {
                        return 1;
                    }
                }
                
                @Override
                public <Data> void dataRead(BrwsrCtx context, Class<? extends Data> type, Data[] arr) {
                    if (value instanceof Object[]) {
                        Object[] data = ((Object[]) value);
                        for (int i = 0; i < data.length && i < arr.length; i++) {
                            arr[i] = org.apidesign.html.json.impl.JSON.read(context, type, data[i]);
                        }
                    } else {
                        if (arr.length > 0) {
                            arr[0] = org.apidesign.html.json.impl.JSON.read(context, type, value);
                        }
                    }
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
    } // end MsgEvnt
}
