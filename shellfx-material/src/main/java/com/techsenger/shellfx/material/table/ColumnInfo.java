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

import com.techsenger.patternfx.core.Name;

/**
 * The persisted-state shape {@link AbstractTableColumnManager#addColumns} needs from a column-info snapshot,
 * shared by {@link TableColumnInfo} and {@link TreeTableColumnInfo} so {@code addColumns} can be implemented
 * once instead of once per column family. Package-private: callers only ever hold the concrete
 * {@code TableColumnInfo}/{@code TreeTableColumnInfo} types, never this interface directly.
 *
 * @param <N> the column-name type
 * @param <ST> the column family's sort-type enum (e.g. {@code TableColumn.SortType})
 * @author Pavel Castornii
 */
interface ColumnInfo<N extends Name, ST> {

    N getName();

    /**
     * Returns this column's position among the control's visible columns, or a negative value if the column is
     * currently hidden. The set of columns a control can show is closed and known upfront (e.g. every enum
     * constant of a given {@link TableColumnName}), so &mdash; unlike the column itself, which is only built
     * while shown &mdash; there is always exactly one info instance per column, for its entire lifetime; hiding
     * a column never discards its info; it only stops the index from being non-negative.
     */
    int getIndex();

    Double getWidth();

    Integer getSortIndex();

    ST getSortType();

    /**
     * Returns whether this column is currently shown, i.e. {@link #getIndex()} is not negative.
     */
    default boolean isVisible() {
        return getIndex() >= 0;
    }
}
