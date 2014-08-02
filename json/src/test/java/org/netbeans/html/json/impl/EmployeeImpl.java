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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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

import java.util.List;
import net.java.html.json.Model;
import net.java.html.json.OnReceive;
import net.java.html.json.Person;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "Employee", properties = {
    @Property(name = "person", type = Person.class),
    @Property(name = "employer", type = Employer.class),
    @Property(name = "call", type = Call.class)
})
public class EmployeeImpl {
    @OnReceive(url = "some/url")
    static void changePersonality(Employee e, Person p) {
        e.setPerson(p);
    }

    private static void callChangePers(Employee e) {
        Person per = new Person();
        e.changePersonalities(10, 3.14, "Ahoj", per);
        e.updatePersonalities("kuk", new Person(), 1, 2, "3", new Person());
        e.socketPersonalities("where", null);
    }

    @OnReceive(url = "some/other/url")
    static void changePersonalities(Employee e, List<Person> data, int i, double d, String s, Person o) {
        e.setCall(new Call(i, d, s, o, data.toArray(new Person[0])));
    }

    @OnReceive(url = "{url}", method = "PUT", data = Person.class)
    static void updatePersonalities(Employee e, List<Person> p, int i, double d, String s, Person o) {
        e.setPerson(p.get(0));
    }

    @OnReceive(url = "{url}", method = "WebSocket", data = Person.class)
    static void socketPersonalities(Employee e, List<Person> p) {
        e.setPerson(p.get(0));
    }
    @OnReceive(url = "{url}", method = "WebSocket", data = Person.class)
    static void socketArrayPersonalities(Employee e, Person[] p) {
        e.setPerson(p[0]);
    }
    
    @Model(className="Call", properties = {
        @Property(name = "i", type=int.class),
        @Property(name = "d", type=double.class),
        @Property(name = "s", type=String.class),
        @Property(name = "p", type=Person.class),
        @Property(name = "data", type=Person.class, array = true)
    })
    static class CallModel {
    }
}
