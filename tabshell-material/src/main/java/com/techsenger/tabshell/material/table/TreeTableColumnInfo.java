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

import javafx.scene.control.TreeTableColumn;

/**
 *
 * @author Pavel Castornii
 */
public class TreeTableColumnInfo extends AbstractTableColumnInfo {

    private Enum<?> name;

    private TreeTableColumn.SortType sortType;

    public TreeTableColumnInfo() {

    }

    public <T extends Enum<T> & TreeTableColumnName> TreeTableColumnInfo(T name) {
        this.name = name;
    }

    public TreeTableColumn.SortType getSortType() {
        return sortType;
    }

    public void setSortType(TreeTableColumn.SortType sortType) {
        this.sortType = sortType;
    }

    public TreeTableColumnName getName() {
        return (TreeTableColumnName) name;
    }

    public <T extends Enum<T> & TreeTableColumnName> void setName(T name) {
        this.name = name;
    }
}
