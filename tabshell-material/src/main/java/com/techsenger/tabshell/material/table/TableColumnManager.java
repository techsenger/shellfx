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

import com.techsenger.annotations.Unmodifiable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 * @author Pavel Castornii
 */
public final class TableColumnManager extends AbstractTableColumnManager<TableColumnName, NamedTableColumn<?, ?>> {

    private final TableView<?> tableView;

    private final Map<TableColumnName, NamedTableColumn<?, ?>> modifiableColumnsByName = new HashMap<>();

    private @Unmodifiable Map<TableColumnName, NamedTableColumn<?, ?>> columnsByName =
            Collections.unmodifiableMap(modifiableColumnsByName);

    public TableColumnManager(TableView<?> tableView) {
        this.tableView = tableView;
        tableView.getColumns().addListener((ListChangeListener<? super TableColumn<?, ?>>) (e) -> {
            if (!isIndexListenerDisabled() && getIndexListener() != null) {
                for (var i = 0; i < tableView.getColumns().size(); i++) {
                    var column = tableView.getColumns().get(i);
                    getIndexListener().accept(((NamedTableColumn<?, ?>) column).getName(), i);
                }
            }
        });
        tableView.getSortOrder().addListener((ListChangeListener<? super TableColumn<?, ?>>) (e) -> {
            if (!isSortIndexListenerDisabled() && getSortIndexListener() != null) {
                for (var i = 0; i < tableView.getSortOrder().size(); i++) {
                    var column = tableView.getSortOrder().get(i);
                    getSortIndexListener().accept(((NamedTableColumn<?, ?>) column).getName(), i);
                }
            }
        });
    }

    private BiConsumer<TableColumnName, TableColumn.SortType> sortTypeListener;

    public BiConsumer<TableColumnName, TableColumn.SortType> getSortTypeListener() {
        return sortTypeListener;
    }

    public void setSortTypeListener(BiConsumer<TableColumnName, TableColumn.SortType> sortTypeListener) {
        this.sortTypeListener = sortTypeListener;
    }

    /**
     * When columns are added via this method listeners are not called.
     *
     * @param infosByName
     */
    public void addColumns(Map<TableColumnName, TableColumnInfo> infosByName) {
        setSortIndexListenerDisabled(true);
        setIndexListenerDisabled(true);
        var orderMap = new HashMap<Integer, NamedTableColumn<?, ?>>();
        var sortMap = new HashMap<Integer, NamedTableColumn<?, ?>>();
        for (var entry : infosByName.entrySet()) {
            var info = entry.getValue();
            var column = createColumn(entry.getKey(), info.getWidth(), info.getSortType());
            modifiableColumnsByName.put(entry.getKey(), column);
            orderMap.put(info.getIndex(), column);
            if (info.getSortIndex() != null) {
                sortMap.put(info.getSortIndex(), column);
            }
        }
        for (var i = 0; i < orderMap.size(); i++) {
            var column = orderMap.get(i);
            Objects.requireNonNull(column, "No column with index " + i);
            tableView.getColumns().add((TableColumn) column);
        }
        for (var i = 0; i < sortMap.size(); i++) {
            var column = sortMap.get(i);
            Objects.requireNonNull(column, "No column with sort index " + i);
            Objects.requireNonNull(column.getSortType(), "No sort type for column " + column.getName());
            tableView.getSortOrder().add((TableColumn) column);
        }
        setSortIndexListenerDisabled(false);
        setIndexListenerDisabled(false);
    }

    /**
     * Returns an unmodifiable map.
     *
     * @return
     */
    public @Unmodifiable Map<TableColumnName, NamedTableColumn<?, ?>> getColumnsByName() {
        return columnsByName;
    }

    protected NamedTableColumn<?, ?> createColumn(TableColumnName name, Double width, TableColumn.SortType sortType) {
        var factory = getColumnFactoriesByName().get(name);
        Objects.requireNonNull(factory, "Factory for " + name + " is not registered");
        var column = factory.create();
        if (width != null) {
            column.setPrefWidth(width);
        }
        column.widthProperty().addListener((ov, oldV, newV) -> getWidthListener().accept(name, newV.doubleValue()));
        if (sortType != null) {
            column.setSortType(sortType);
        }
        column.sortTypeProperty().addListener((ov, oldV, newV) -> getSortTypeListener().accept(name, newV));
        return column;
    }
}
