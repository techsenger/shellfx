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
public class TableColumnHistory implements Serializable {

    private String name;

    private double width;

    private TableColumn.SortType sortType;

    private Integer sortIndex;

    public TableColumnHistory() {

    }

    public TableColumnHistory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String key) {
        this.name = key;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public TableColumn.SortType getSortType() {
        return sortType;
    }

    public void setSortType(TableColumn.SortType sortType) {
        this.sortType = sortType;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }
}
