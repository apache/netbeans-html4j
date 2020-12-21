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
package net.java.html.json.tests;

import java.util.List;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Models;
import net.java.html.json.Property;
import org.netbeans.html.json.tck.KOTest;
import static net.java.html.json.tests.Utils.assertEquals;

/** Tests model of a mine field and its behavior in the browser.
 */
@Model(className = "Mines", targetId = "", properties = {
    @Property(name = "state", type = MinesTest.GameState.class),
    @Property(name = "rows", type = Row.class, array = true),
})
public final class MinesTest {
    Mines m;
    @KOTest public void paintTheGridOnClick() throws Throwable {
        if (m == null) {
            BrwsrCtx ctx = Utils.newContext(MinesTest.class);
            Object exp = Utils.exposeHTML(MinesTest.class, """
                <button id='init' data-bind='click: normalSize'></button>
                <table>
                    <tbody id='table'>
                        <!-- ko foreach: rows -->
                        <tr>
                            <!-- ko foreach: columns -->
                            <td data-bind='css: style' >
                                <div data-bind='text: html'></div>
                            </td>
                            <!-- /ko -->
                        </tr>
                        <!-- /ko -->
                    </tbody>
                </table>
            """);
            m = Models.bind(new Mines(), ctx);
            m.applyBindings();
            int cnt = Utils.countChildren(MinesTest.class, "table");
            assertEquals(cnt, 0, "Table is empty: " + cnt);
            Utils.scheduleClick(MinesTest.class, "init", 100);
        }


        int cnt = Utils.countChildren(MinesTest.class, "table");
        if (cnt == 0) {
            throw new InterruptedException();
        }
        assertEquals(cnt, 10, "There is ten rows in the table now: " + cnt);

        Utils.exposeHTML(MinesTest.class, "");
    }

    @KOTest public void countAround() throws Exception {
        Mines mines = new Mines();
        mines.init(5, 5, 0);
        mines.getRows().get(0).getColumns().get(0).setMine(true);
        mines.getRows().get(1).getColumns().get(0).setMine(true);
        mines.getRows().get(0).getColumns().get(1).setMine(true);

        int cnt = around(mines, 1, 1);
        assertEquals(cnt, 3, "There are three mines around. Was: " + cnt);
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

    private static int randIndex;
    private static int[] RANDOM = {
        4, 5, 8, 1, 3, 9, 2, 7, 7, 3, 8, 5, 4, 0,
        2, 7, 5, 3, 2, 9, 8, 8, 5, 3, 5, 8, 1, 5
    };
    private static int random() {
        return RANDOM[randIndex++ % RANDOM.length];
    }

    @ModelOperation static void init(Mines model, int width, int height, int mines) {
        List<Row> rows = Models.asList();
        for (int y = 0; y < height; y++) {
            Square[] columns = new Square[width];
            for (int x = 0; x < width; x++) {
                columns[x] = new Square(SquareType.UNKNOWN, false);
            }
            rows.add(new Row(columns));
        }

        while (mines > 0) {
            int x = random() % width;
            int y = random() % height;
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
        List<Integer> xBombs = Models.asList();
        List<Integer> yBombs = Models.asList();
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
