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

package com.techsenger.tabshell.material.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author Pavel Castornii
 */
public final class TableHistoryUtils {

    public static TableHistory createHistory(TableView<?> table) {
        Map<TableColumn<?, ?>, Integer> sortedColumnIndexesByColumn = null;
        if (!table.getSortOrder().isEmpty()) {
            sortedColumnIndexesByColumn = new HashMap<>();
            for (var i = 0; i < table.getSortOrder().size(); i++) {
                var c = table.getSortOrder().get(i);
                sortedColumnIndexesByColumn.put(c, i);
            }
        }
        List<TableColumnInfo> columns = new ArrayList<>();
        for (var column: table.getColumns()) {
            var columnInfo = new TableColumnInfo();
            var namedColumn = (NamedTableColumn<?, ?>) column;
            columnInfo.setName((Enum & TableColumnName) namedColumn.getName());
            columnInfo.setWidth(column.getWidth());
            if (sortedColumnIndexesByColumn != null) {
                var sortIndex = sortedColumnIndexesByColumn.get(column);
                if (sortIndex != null) {
                    columnInfo.setSortType(column.getSortType());
                    columnInfo.setSortIndex(sortIndex);
                }
            }
            columns.add(columnInfo);
        }
        var tableHistory = new TableHistory();
        tableHistory.setColumns(columns);
        return tableHistory;
    }

    public static void restoreTable(TableView<?> table, Function<TableColumnName, TableColumn<?, ?>> columnProvider,
            TableHistory history, boolean widthIncluded) {
        TreeMap<Integer, TableColumn<?, ?>> sortedColumnByIndex = new TreeMap<>();
        for (var historyColumn : history.getColumns()) {
            var column = columnProvider.apply(historyColumn.getName());
            if (widthIncluded) {
                column.setPrefWidth(historyColumn.getWidth());
            }
            if (historyColumn.getSortIndex() != null) {
                sortedColumnByIndex.put(historyColumn.getSortIndex(), column);
                column.setSortType(historyColumn.getSortType());
            }
            table.getColumns().add((TableColumn) column);
        }
        if (!sortedColumnByIndex.isEmpty()) {
            table.getSortOrder().addAll((Collection) sortedColumnByIndex.values());
        }
    }

    private TableHistoryUtils() {
        //empty
    }
}
