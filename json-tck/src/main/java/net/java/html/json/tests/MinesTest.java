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

package net.java.html.json.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.apidesign.html.json.tck.KOTest;

/** Tests model of a mine field and its behavior in the browser.
 */
@Model(className = "Mines", properties = {
    @Property(name = "state", type = MinesTest.GameState.class),
    @Property(name = "rows", type = Row.class, array = true),
})
public final class MinesTest {
    Mines m;
    @KOTest public void paintTheGridOnClick() throws Throwable {
        if (m == null) {
            BrwsrCtx ctx = Utils.newContext(MinesTest.class);
            Object exp = Utils.exposeHTML(MinesTest.class, 
    "            <button id='init' data-bind='click: normalSize'></button>\n" +
    "            <table>\n" +
    "                <tbody id='table'>\n" +
    "                    <!-- ko foreach: rows -->\n" +
    "                    <tr>\n" +
    "                        <!-- ko foreach: columns -->\n" +
    "                        <td data-bind='css: style' >\n" +
    "                            <div data-bind='text: html'></div>\n" +
    "                        </td>\n" +
    "                        <!-- /ko -->\n" +
    "                    </tr>\n" +
    "                    <!-- /ko -->\n" +
    "                </tbody>\n" +
    "            </table>\n" +
    ""
            );
            m = Models.bind(new Mines(), ctx);
            m.applyBindings();
            int cnt = Utils.countChildren(MinesTest.class, "table");
            assert cnt == 0 : "Table is empty: " + cnt;
            scheduleClick("init", 100);
        }


        int cnt = Utils.countChildren(MinesTest.class, "table");
        if (cnt == 0) {
            throw new InterruptedException();
        }
        assert cnt == 10 : "There is ten rows in the table now: " + cnt;
        
        Utils.exposeHTML(MinesTest.class, "");
    }
    
    @KOTest public void countAround() throws Exception {
        Mines mines = new Mines();
        mines.init(5, 5, 0);
        mines.getRows().get(0).getColumns().get(0).setMine(true);
        mines.getRows().get(1).getColumns().get(0).setMine(true);
        mines.getRows().get(0).getColumns().get(1).setMine(true);
        
        int cnt = around(mines, 1, 1);
        assert cnt == 3 : "There are three mines around. Was: " + cnt;
    }
    
    private static void scheduleClick(String id, int delay) throws Exception {
        String s = "var id = arguments[0]; var delay = arguments[1];"
            + "var e = window.document.getElementById(id);\n "
            + "var f = function() {;\n "
            + "  var ev = window.document.createEvent('MouseEvents');\n "
            + "  ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n "
            + "  e.dispatchEvent(ev);\n"
            + "};\n"
            + "window.setTimeout(f, delay);";
        Utils.executeScript(
            MinesTest.class,
            s, id, delay);
    }
    
    enum GameState {
        IN_PROGRESS, WON, LOST;
    }
    
    @Model(className = "Row", properties = {
        @Property(name = "columns", type = Square.class, array = true)
    })
    static class RowModel {
    }

    @Model(className = "Square", properties = {
        @Property(name = "state", type = SquareType.class),
        @Property(name = "mine", type = boolean.class)
    })
    static class SquareModel {
        @ComputedProperty static String html(SquareType state) {
            if (state == null) return "&nbsp;";
            switch (state) {
                case EXPLOSION: return "&#x2717;";
                case UNKNOWN: return "&nbsp;";
                case DISCOVERED: return "&#x2714;";  
                case N_0: return "&nbsp;";
            }
            return "&#x278" + (state.ordinal() - 1);
        }
        
        @ComputedProperty static String style(SquareType state) {
            return state == null ? null : state.toString();
        }
    }
    
    enum SquareType {
        N_0, N_1, N_2, N_3, N_4, N_5, N_6, N_7, N_8,
        UNKNOWN, EXPLOSION, DISCOVERED;
        
        final boolean isVisible() {
            return name().startsWith("N_");
        }

        final SquareType moreBombsAround() {
            switch (this) {
                case EXPLOSION:
                case UNKNOWN:
                case DISCOVERED:
                case N_8:
                    return this;
            }
            return values()[ordinal() + 1];
        }
    }
    
    @ComputedProperty static boolean fieldShowing(GameState state) {
        return state != null;
    }
    
    @Function static void normalSize(Mines m) {
        m.init(10, 10, 10);
    }
    
    @ModelOperation static void init(Mines model, int width, int height, int mines) {
        List<Row> rows = new ArrayList<Row>(height);
        for (int y = 0; y < height; y++) {
            Square[] columns = new Square[width];
            for (int x = 0; x < width; x++) {
                columns[x] = new Square(SquareType.UNKNOWN, false);
            }
            rows.add(new Row(columns));
        }
        
        Random r = new Random();
        while (mines > 0) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);
            final Square s = rows.get(y).getColumns().get(x);
            if (s.isMine()) {
                continue;
            }
            s.setMine(true);
            mines--;
        }

        model.setState(GameState.IN_PROGRESS);
        model.getRows().clear();
        model.getRows().addAll(rows);
    }
    
    @ModelOperation static void computeMines(Mines model) {
        List<Integer> xBombs = new ArrayList<Integer>();
        List<Integer> yBombs = new ArrayList<Integer>();
        final List<Row> rows = model.getRows();
        boolean emptyHidden = false;
        SquareType[][] arr = new SquareType[rows.size()][];
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            arr[y] = new SquareType[columns.size()];
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                if (sq.isMine()) {
                    xBombs.add(x);
                    yBombs.add(y);
                }
                if (sq.getState().isVisible()) {
                    arr[y][x] = SquareType.N_0;
                } else {
                    if (!sq.isMine()) {
                        emptyHidden = true;
                    }
                }
            }
        }
        for (int i = 0; i < xBombs.size(); i++) {
            int x = xBombs.get(i);
            int y = yBombs.get(i);
            
            incrementAround(arr, x, y);
        }
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                final SquareType newState = arr[y][x];
                if (newState != null && newState != sq.getState()) {
                    sq.setState(newState);
                }
            }
        }
        
        if (!emptyHidden) {
            model.setState(GameState.WON);
            showAllBombs(model, SquareType.DISCOVERED);
        }
    }
    
    private static void incrementAround(SquareType[][] arr, int x, int y) {
        incrementAt(arr, x - 1, y - 1);
        incrementAt(arr, x - 1, y);
        incrementAt(arr, x - 1, y + 1);

        incrementAt(arr, x + 1, y - 1);
        incrementAt(arr, x + 1, y);
        incrementAt(arr, x + 1, y + 1);
        
        incrementAt(arr, x, y - 1);
        incrementAt(arr, x, y + 1);
    }
    
    private static void incrementAt(SquareType[][] arr, int x, int y) {
        if (y >= 0 && y < arr.length) {
            SquareType[] r = arr[y];
            if (x >= 0 && x < r.length) {
                SquareType sq = r[x];
                if (sq != null) {
                    r[x] = sq.moreBombsAround();
                }
            }
        }
    }
    
    static void showAllBombs(Mines model, SquareType state) {
        for (Row row : model.getRows()) {
            for (Square square : row.getColumns()) {
                if (square.isMine()) {
                    square.setState(state);
                }
            }
        }
    }
    
    private static void expandKnown(Mines model, Square data) {
        final List<Row> rows = model.getRows();
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                if (sq == data) {
                    expandKnown(model, x, y);
                    return;
                }
            }
        }
    }
    private static void expandKnown(Mines model, int x , int y) {
        if (y < 0 || y >= model.getRows().size()) {
            return;
        }
        final List<Square> columns = model.getRows().get(y).getColumns();
        if (x < 0 || x >= columns.size()) {
            return;
        }
        final Square sq = columns.get(x);
        if (sq.getState() == SquareType.UNKNOWN) {
            int around = around(model, x, y);
            final SquareType t = SquareType.valueOf("N_" + around);
            sq.setState(t);
            if (sq.getState() == SquareType.N_0) {
                expandKnown(model, x - 1, y - 1);
                expandKnown(model, x - 1, y);
                expandKnown(model, x - 1, y + 1);
                expandKnown(model, x , y - 1);
                expandKnown(model, x, y + 1);
                expandKnown(model, x + 1, y - 1);
                expandKnown(model, x + 1, y);
                expandKnown(model, x + 1, y + 1);
            }
        }
    }
    private static int around(Mines model, int x, int y) {
        return minesAt(model, x - 1, y - 1)
                + minesAt(model, x - 1, y)
                + minesAt(model, x - 1, y + 1)
                + minesAt(model, x, y - 1)
                + minesAt(model, x, y + 1)
                + minesAt(model, x + 1, y - 1)
                + minesAt(model, x + 1, y)
                + minesAt(model, x + 1, y + 1);
    }

    private static int minesAt(Mines model, int x, int y) {
        if (y < 0 || y >= model.getRows().size()) {
            return 0;
        }
        final List<Square> columns = model.getRows().get(y).getColumns();
        if (x < 0 || x >= columns.size()) {
            return 0;
        }
        Square sq = columns.get(x);
        return sq.isMine() ? 1 : 0;
    }
}
