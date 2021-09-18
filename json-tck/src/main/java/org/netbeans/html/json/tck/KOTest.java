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
package org.netbeans.html.json.tck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotates method that is part of {@link KnockoutTCK visual test compatibility kit}
 * or {@link JavaScriptTCK headless test compatibility kit}
 * and should be executed in appropriate environment. The method annotated by
 * this annotation will be public instance method of its class 
 * with no arguments. A typical way to enumerate such methods is:
 * <p>
 * {@codesnippet net.java.html.boot.script.ScriptEngineJavaScriptTCK}
 * and then one can execute such methods as
 * <p>
 * {@codesnippet net.java.html.boot.script.ScriptEngineCase#run}
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KOTest {
}
