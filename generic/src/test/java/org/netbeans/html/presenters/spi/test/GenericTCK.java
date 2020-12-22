package org.netbeans.html.presenters.spi.test;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.JavaScriptTCK;
import org.netbeans.html.presenters.spi.ProtoPresenter;
import static org.testng.Assert.assertNotNull;

final class GenericTCK extends JavaScriptTCK {
    static final GenericTCK INSTANCE = new GenericTCK();

    private final Map<ProtoPresenter, Testing> MAP = new HashMap<>();
    private GenericTCK() {
    }

    @Override
    public boolean executeNow(String script) throws Exception {
        Testing t = MAP.get(Fn.activePresenter());
        assertNotNull(t, "Testing framework found");
        return t.sync ? t.eng.eval(script) != this : false;
    }

    public static Class[] tests() {
        return testClasses();
    }

    void register(ProtoPresenter presenter, Testing testing) {
        MAP.put(presenter, testing);
    }

}
