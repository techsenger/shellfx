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

import java.io.Serializable;
import java.util.List;

/**
 * A serializable snapshot of a table's full column state (one {@link TableColumnInfo} per known column,
 * whether shown or hidden), for persisting and restoring it across application sessions as part of a
 * component's own history.
 *
 * @author Pavel Castornii
 */
public class TableHistory implements Serializable {

    private List<TableColumnInfo> columns;

    /**
     * Creates an instance with no columns set yet. Intended for deserialization frameworks that require a
     * public no-arg constructor; application code should prefer {@link #TableHistory(List)}.
     */
    public TableHistory() {

    }

    /**
     * Creates an instance holding {@code columns}.
     *
     * @param columns the column state to persist
     */
    public TableHistory(List<TableColumnInfo> columns) {
        this.columns = columns;
    }

    /**
     * Returns the persisted column state, or {@code null} if none was ever set.
     *
     * @return the persisted columns, or {@code null}
     */
    public List<TableColumnInfo> getColumns() {
        return columns;
    }

    /**
     * Sets the persisted column state.
     *
     * @param columns the new column state
     */
    public void setColumns(List<TableColumnInfo> columns) {
        this.columns = columns;
    }
}
