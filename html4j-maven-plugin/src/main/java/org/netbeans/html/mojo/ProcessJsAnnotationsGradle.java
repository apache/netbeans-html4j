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

package org.netbeans.html.mojo;

import java.util.Set;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public final class ProcessJsAnnotationsGradle implements Plugin<Project> {

    @Override
    public void apply(final Project p) {
        final ProcessJsAnnotationsTask process = p.getTasks().create("process-js-annotations", ProcessJsAnnotationsTask.class, new Action<ProcessJsAnnotationsTask>() {
            @Override
            public void execute(ProcessJsAnnotationsTask process) {
            }
        });
        p.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project p) {
                Set<? extends Task> tasks = (Set<? extends Task>) p.findProperty("tasks");
                for (Task task : tasks) {
                    if (task.getName().startsWith("compile")) {
                        process.dependsOn(task);
                    }
                    if (
                            task.getName().equals("test") ||
                            task.getName().equals("run") ||
                            task.getName().equals("jar")
                    ) {
                        if (task instanceof DefaultTask) {
                            ((DefaultTask)task).dependsOn(process);
                        }
                    }
                }
                process.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task t) {
                        process.processJsAnnotations(p);
                    }
                });
            }
        });
    }
}
