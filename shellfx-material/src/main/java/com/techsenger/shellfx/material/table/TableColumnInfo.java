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

import javafx.scene.control.TableColumn;

/**
 * The persisted position/width/sort state of one {@code TableView} column, identified by a {@link TableColumnName}.
 * Built and consumed by {@link TableColumnManager}/{@link AbstractTableColumnManager}, and persisted across
 * sessions via {@link TableHistory}.
 *
 * @author Pavel Castornii
 */
public class TableColumnInfo extends AbstractTableColumnInfo
        implements ColumnInfo<TableColumnName, TableColumn.SortType> {

    private Enum<?> name;

    private TableColumn.SortType sortType;

    /**
     * Creates an instance with no name set yet. Intended for deserialization frameworks that require a public
     * no-arg constructor; application code should prefer {@link #TableColumnInfo(Enum)}, which sets the name up
     * front.
     */
    public TableColumnInfo() {

    }

    /**
     * Creates an instance identified by {@code name}, with no position, width or sort state set yet.
     *
     * @param name the column's stable identity
     */
    public <T extends Enum<T> & TableColumnName> TableColumnInfo(T name) {
        this.name = name;
    }

    /**
     * Copies {@code other}'s state into a new, independent instance &mdash; e.g. so a component can seed its own
     * mutable column state from another component's snapshot without the two aliasing the same instances.
     */
    public TableColumnInfo(TableColumnInfo other) {
        this.name = other.name;
        setIndex(other.getIndex());
        setWidth(other.getWidth());
        setSortIndex(other.getSortIndex());
        this.sortType = other.sortType;
    }

    /**
     * Returns this column's sort direction. Only meaningful when {@link #getSortIndex()} is non-null.
     *
     * @return the column's sort direction
     */
    public TableColumn.SortType getSortType() {
        return sortType;
    }

    /**
     * Sets this column's sort direction.
     *
     * @param sortType the column's new sort direction
     */
    public void setSortType(TableColumn.SortType sortType) {
        this.sortType = sortType;
    }

    /**
     * Returns this column's stable identity.
     *
     * @return the column's name
     */
    public TableColumnName getName() {
        return (TableColumnName) name;
    }

    /**
     * Sets this column's stable identity.
     *
     * @param name the column's new name
     */
    public <T extends Enum<T> & TableColumnName> void setName(T name) {
        this.name = name;
    }
}
