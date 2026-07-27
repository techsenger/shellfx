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

import java.util.Objects;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * {@link TableView} instantiation of {@link AbstractTableColumnManager} &mdash; see that class for the shared
 * behavior (wiring, {@code addColumns()}, etc.). This class only supplies what's genuinely {@code TableView}/
 * {@code TableColumn}-specific: building a column from its factory and wiring its width/sort-type listeners.
 *
 * @param <S> the row type of the {@code TableView} this manager was built for, e.g. the {@code Student} in
 *     {@code TableView<Student>}
 * @author Pavel Castornii
 */
public final class TableColumnManager<S>
        extends AbstractTableColumnManager<TableColumnName, NamedTableColumn<S, ?>, TableColumn.SortType> {

    /**
     * Creates a manager wired to {@code tableView}'s own column and sort-order lists.
     *
     * @param tableView the table whose columns this manager will build and track
     */
    @SuppressWarnings("unchecked")
    public TableColumnManager(TableView<S> tableView) {
        super((ObservableList<NamedTableColumn<S, ?>>) (ObservableList<?>) tableView.getColumns(),
                (ObservableList<NamedTableColumn<S, ?>>) (ObservableList<?>) tableView.getSortOrder());
    }

    @Override
    protected NamedTableColumn<S, ?> createColumn(TableColumnName name, Double width, TableColumn.SortType sortType) {
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

    @Override
    protected TableColumnName getName(NamedTableColumn<S, ?> column) {
        return column.getName();
    }
}
