/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Oracle. Portions Copyright 2013-2013 Oracle. All Rights Reserved.
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
package org.netbeans.html.archetype.test;

import java.io.IOException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ArchetypeVersionTest {
    private String version;
    
    public ArchetypeVersionTest() {
    }
    
    @BeforeClass public void readCurrentVersion() throws Exception {
        version = findCurrentVersion();
        assertFalse(version.isEmpty(), "There should be some version string");
    }
    

    @Test public void testComparePomDepsVersions() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL r = l.getResource("archetype-resources/pom.xml");
        assertNotNull(r, "Archetype pom found");
        
        final XPathFactory fact = XPathFactory.newInstance();
        XPathExpression xp2 = fact.newXPath().compile(
            "//properties/net.java.html.version/text()"
        );
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.openStream());
        String arch = (String) xp2.evaluate(dom, XPathConstants.STRING);

        assertEquals(arch, version, "net.java.html.json dependency needs to be on latest version");
    }
    
    @Test public void testNbActions() throws Exception {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL r = l.getResource("archetype-resources/nbactions.xml");
        assertNotNull(r, "Archetype nb file found");
        
        final XPathFactory fact = XPathFactory.newInstance();
        XPathExpression xp2 = fact.newXPath().compile(
            "//goal/text()"
        );
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(r.openStream());
        NodeList goals = (NodeList) xp2.evaluate(dom, XPathConstants.NODESET);
        
        for (int i = 0; i < goals.getLength(); i++) {
            String s = goals.item(i).getTextContent();
            if (s.contains("netbeans")) {
                assertFalse(s.matches(".*netbeans.*[0-9].*"), "No numbers: " + s);
            }
        }
    }

    static String findCurrentVersion() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException, XPathFactoryConfigurationException {
        final ClassLoader l = ArchetypeVersionTest.class.getClassLoader();
        URL u = l.getResource("META-INF/maven/org.netbeans.html/knockout4j-archetype/pom.xml");
        assertNotNull(u, "Own pom found: " + System.getProperty("java.class.path"));

        final XPathFactory fact = XPathFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        XPathExpression xp = fact.newXPath().compile("project/version/text()");
        
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(u.openStream());
        return xp.evaluate(dom);
    }
}
