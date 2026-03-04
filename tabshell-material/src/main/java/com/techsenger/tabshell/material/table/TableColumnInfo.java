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

import java.io.Serializable;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Pavel Castornii
 */
public class TableColumnInfo extends AbstractTableColumnInfo implements Serializable {

    private Enum<?> name;

    private TableColumn.SortType sortType;

    public TableColumnInfo() {

    }

    public <T extends Enum<T> & TableColumnName> TableColumnInfo(T name) {
        this.name = name;
    }

    public TableColumn.SortType getSortType() {
        return sortType;
    }

    public void setSortType(TableColumn.SortType sortType) {
        this.sortType = sortType;
    }

    public TableColumnName getName() {
        return (TableColumnName) name;
    }

    public <T extends Enum<T> & TableColumnName> void setName(T name) {
        this.name = name;
    }
}
