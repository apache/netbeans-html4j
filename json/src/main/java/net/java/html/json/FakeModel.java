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
