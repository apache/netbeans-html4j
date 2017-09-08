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
