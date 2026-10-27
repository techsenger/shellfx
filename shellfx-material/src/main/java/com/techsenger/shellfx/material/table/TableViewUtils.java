/*
 * Copyright 2024-2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.shellfx.material.table;

import java.util.function.Consumer;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Utilities for the rows of a {@link TableView} that are shown.
 *
 * @author Pavel Castornii
 */
public final class TableViewUtils {

    /**
     * Passes the item of every row to {@code action}, without touching the cells, so a caller can prepare the items
     * that are shown (e.g. decorate them).
     *
     * <p>With {@code onlyVisible} the rows are those that exist in the virtual flow, the visible ones and the
     * partially visible ones, and no cell is created; the rows that left the viewport are empty and get their item
     * through {@code updateItem} when they return. It does nothing if the table view has no rows shown yet. Without
     * it every item of the table view is passed.
     *
     * @param <T> the type of items
     * @param tableView the table view whose rows to walk
     * @param onlyVisible whether to walk only the existing rows of the virtual flow or every item
     * @param action what to do with the item of each row
     */
    public static <T> void forEachItem(TableView<T> tableView, boolean onlyVisible, Consumer<? super T> action) {
        if (!onlyVisible) {
            tableView.getItems().forEach(action);
            return;
        }
        if (!(tableView.lookup(".virtual-flow") instanceof VirtualFlow<?> flow)) {
            return;
        }
        var firstCell = flow.getFirstVisibleCell();
        var lastCell = flow.getLastVisibleCell();
        if (firstCell == null || lastCell == null) {
            return;
        }
        var items = tableView.getItems();
        var last = Math.min(lastCell.getIndex(), items.size() - 1);
        for (var index = Math.max(firstCell.getIndex(), 0); index <= last; index++) {
            action.accept(items.get(index));
        }
    }

    private TableViewUtils() {
        // empty
    }
}
