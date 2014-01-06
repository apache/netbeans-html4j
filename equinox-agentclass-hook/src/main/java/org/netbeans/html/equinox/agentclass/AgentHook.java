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
package org.netbeans.html.equinox.agentclass;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.BundleWatcher;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

public class AgentHook implements HookConfigurator, BundleWatcher, ClassLoadingHook {
    private static final Logger LOG = Logger.getLogger(AgentHook.class.getName());
	private boolean all;
	
	@Override
	public void addHooks(HookRegistry hookRegistry) {
		LOG.info("Agent hook for Equinox initialized!");
		hookRegistry.addWatcher(this);
		hookRegistry.addClassLoadingHook(this);
	}

	@Override
	public void watchBundle(Bundle bundle, int type) {
		if (!all) {
			BundleContext c = bundle.getBundleContext();
			if (c != null) {
				Bundle[] arr = bundle.getBundleContext().getBundles();
				for (Bundle b : arr) {
					agentBundle(b);
				}
				all = true;
			}
		}
		if (type == BundleWatcher.END_ACTIVATION) {
			agentBundle(bundle);
		}
	}

	private void agentBundle(Bundle bundle) {
		String agentClass = (String)bundle.getHeaders().get("Agent-Class");
		if (agentClass != null) {
			Class<?> agent;
			try {
				agent = bundle.loadClass(agentClass);
				NbInstrumentation.registerAgent(agent.getClassLoader(), agent.getName());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	@Override
	public byte[] processClass(String name, byte[] bytes,
			ClasspathEntry ce, BundleEntry entry,
			ClasspathManager manager) {
        final BaseData bd = ce.getBaseData();
        if (bd == null) {
            return bytes;
        }
        final Bundle b = bd.getBundle();
        if (b == null) {
            return bytes;
        }
        BundleWiring w = (BundleWiring)b.adapt(BundleWiring.class);
        if (w == null) {
            return bytes;
        }
        ClassLoader loader = w.getClassLoader();
		try {
			return NbInstrumentation.patchByteCode(loader, name, ce.getDomain(), bytes);
		} catch (IllegalClassFormatException e) {
			return bytes;
		}
	}

	@Override
	public boolean addClassPathEntry(ArrayList cpEntries,
			String cp, ClasspathManager hostmanager, BaseData sourcedata,
			ProtectionDomain sourcedomain) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String findLibrary(BaseData data, String libName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getBundleClassLoaderParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseClassLoader createClassLoader(ClassLoader parent,
			ClassLoaderDelegate delegate, BundleProtectionDomain domain,
			BaseData data, String[] bundleclasspath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initializedClassLoader(BaseClassLoader baseClassLoader,
			BaseData data) {
		// TODO Auto-generated method stub
		
	}
}
