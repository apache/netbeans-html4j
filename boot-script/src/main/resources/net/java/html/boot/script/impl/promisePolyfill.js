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

(function (PromisePolyfillClass) {
    var global = (0 || eval)('this');
    var promisePolyfill = PromisePolyfillClass.static;

    var Promise = function (resolver) {
        if (promisePolyfill.isFuture(resolver)) {
            this._future = resolver;
        } else {
            var future = promisePolyfill.create();
            resolver(function(value) {
                future.complete(value);
            }, function(error) {
                future.completeExceptionally(error);
            });
            this._future = future;
        }
    };
    
    Promise.resolve = function (value) {
        if (value instanceof Promise) {
            return value;
        } else {
            return new Promise(function(success) {
                success(value);
            });
        }
    };

    Promise.prototype.then = function (success, error) {
        var future = this._future;
        if (success) {
            future = future.thenApply(success);
        }
        if (error) {
            future = future.exceptionally(error);
        }
        return new Promise(future);
    };

    global.Promise = Promise;
})
