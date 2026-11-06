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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;

/**
 * Makes the state of a {@link TableColumnInfo} observable. The adapter takes the info over: it must be changed only
 * through the adapter, otherwise the properties do not see the change.
 *
 * @author Pavel Castornii
 */
public final class TableColumnInfoAdapter implements ColumnInfo<TableColumnName, TableColumn.SortType> {

    private final TableColumnInfo info;

    private final ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();

    private final ReadOnlyObjectWrapper<Double> width = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<Integer> sortIndex = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<TableColumn.SortType> sortType = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyBooleanWrapper visible = new ReadOnlyBooleanWrapper();

    /**
     * Creates an adapter of {@code info}.
     *
     * @param info the info to adapt, not to be changed directly afterwards
     */
    public TableColumnInfoAdapter(TableColumnInfo info) {
        this.info = info;
        this.index.set(info.getIndex());
        this.width.set(info.getWidth());
        this.sortIndex.set(info.getSortIndex());
        this.sortType.set(info.getSortType());
        this.visible.bind(index.greaterThanOrEqualTo(0));
    }

    /**
     * Returns the adapted info with the current state, to be copied for persisting.
     */
    public TableColumnInfo getInfo() {
        return info;
    }

    @Override
    public TableColumnName getName() {
        return info.getName();
    }

    @Override
    public int getIndex() {
        return info.getIndex();
    }

    public void setIndex(int index) {
        info.setIndex(index);
        this.index.set(index);
    }

    public ReadOnlyIntegerProperty indexProperty() {
        return index.getReadOnlyProperty();
    }

    @Override
    public Double getWidth() {
        return info.getWidth();
    }

    public void setWidth(Double width) {
        info.setWidth(width);
        this.width.set(width);
    }

    public ReadOnlyObjectProperty<Double> widthProperty() {
        return width.getReadOnlyProperty();
    }

    @Override
    public Integer getSortIndex() {
        return info.getSortIndex();
    }

    public void setSortIndex(Integer sortIndex) {
        info.setSortIndex(sortIndex);
        this.sortIndex.set(sortIndex);
    }

    public ReadOnlyObjectProperty<Integer> sortIndexProperty() {
        return sortIndex.getReadOnlyProperty();
    }

    @Override
    public TableColumn.SortType getSortType() {
        return info.getSortType();
    }

    public void setSortType(TableColumn.SortType sortType) {
        info.setSortType(sortType);
        this.sortType.set(sortType);
    }

    public ReadOnlyObjectProperty<TableColumn.SortType> sortTypeProperty() {
        return sortType.getReadOnlyProperty();
    }

    /**
     * Returns the property that is true while the column is shown, that is while its index is not negative.
     */
    public ReadOnlyBooleanProperty visibleProperty() {
        return visible.getReadOnlyProperty();
    }
}
