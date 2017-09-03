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
package net.java.html.json;

/**
 * Generated for {@link Models}
 */
final class FakeModel implements Cloneable {
    private static Class<Models> modelFor() {
        return Models.class;
    }
    private static final Html4JavaType TYPE = new Html4JavaType();
    private final org.netbeans.html.json.spi.Proto proto;

    private FakeModel(net.java.html.BrwsrCtx context) {
        this.proto = TYPE.createProto(this, context);
    }

    private FakeModel() {
        this(net.java.html.BrwsrCtx.findDefault(FakeModel.class));
    }

    public static Object create() {
        return new FakeModel();
    }

    private static class Html4JavaType extends org.netbeans.html.json.spi.Proto.Type<FakeModel> {

        private Html4JavaType() {
            super(FakeModel.class, Models.class, 0, 0);
        }

        @Override
        public void setValue(FakeModel data, int type, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(FakeModel data, int type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void call(FakeModel model, int type, Object data, Object ev) throws Exception {
            switch (type) {
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public org.netbeans.html.json.spi.Proto protoFor(Object obj) {
            return ((FakeModel) obj).proto;
        }

        @Override
        public void onChange(FakeModel model, int type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onMessage(FakeModel model, int index, int type, Object data, Object[] params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FakeModel read(net.java.html.BrwsrCtx c, Object json) {
            return new FakeModel(c, json);
        }

        @Override
        public FakeModel cloneTo(FakeModel o, net.java.html.BrwsrCtx c) {
            return o;
        }
    }

    private FakeModel(net.java.html.BrwsrCtx c, Object json) {
        this(c);
        Object[] ret = new Object[0];
        proto.extract(json, new String[]{}, ret);
    }

}
