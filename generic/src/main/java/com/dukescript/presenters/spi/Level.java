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
package com.dukescript.presenters.spi;

/** Logging levels similar to java.util.logging ones. */
enum Level {
    FINE, INFO, WARNING, SEVERE;

    public int intValue() {
        switch (this) {
            case SEVERE: return 1000;
            case WARNING: return 900;
            case INFO: return 800;
            case FINE: return 500;
        }
        return 0;
    }
}
