package org.apidesign.html.boot.impl;

import java.net.URL;
import java.util.Collection;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public interface FindResources {

    public void findResources(String path, Collection<? super URL> results, boolean oneIsEnough);
    
}
