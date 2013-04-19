package org.apidesign.html.json.impl;

import net.java.html.json.Context;
import org.apidesign.html.json.spi.ContextBuilder;
import org.apidesign.html.json.spi.Technology;

/** Internal communication between API (e.g. {@link Context}), SPI
 * (e.g. {@link ContextBuilder}) and the implementation package.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class ContextAccessor {
    private static ContextAccessor DEFAULT;
    static {
        // run initializers
        Context.EMPTY.getClass();
    }
    
    protected ContextAccessor() {
        if (DEFAULT != null) throw new IllegalStateException();
        DEFAULT = this;
    }
    
    protected abstract Context newContext(Technology<?> t);
    protected abstract Technology<?> technology(Context c);
    
    
    public static Context create(Technology<?> t) {
        return DEFAULT.newContext(t);
    }
    
    static Technology<?> findTechnology(Context c) {
        return DEFAULT.technology(c);
    }
}
