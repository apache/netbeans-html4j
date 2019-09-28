package com.dukescript.presenters.renderer;

/*
 * #%L
 * Desktop Browser Renderer - a library from the "DukeScript Presenters" project.
 * 
 * Dukehoff GmbH designates this particular file as subject to the "Classpath"
 * exception as provided in the README.md file that accompanies this code.
 * %%
 * Copyright (C) 2015 - 2019 Dukehoff GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.sun.jna.Pointer;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

final class AWT extends Show {
    @Override
    public void show(URI page) throws IOException {
        try {
            LOG.log(Level.FINE, "Trying Desktop.browse on {0} {2} by {1}", new Object[]{
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.vendor"),
                System.getProperty("java.vm.version"),});
            java.awt.Desktop.getDesktop().browse(page);
            LOG.log(Level.FINE, "Desktop.browse successfully finished");
            System.in.read();
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.FINE, "Desktop.browse not supported: {0}", ex.getMessage());
            throw new IOException(ex);
        }
    }

    @Override
    public JSC jsc() {
        return null;
    }

    @Override
    public Pointer jsContext() {
        return null;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
