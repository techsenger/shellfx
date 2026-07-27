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

/**
 * The persisted state shared by every column family's info type ({@link TableColumnInfo},
 * {@link TreeTableColumnInfo}): position, width and sort-order position. What is family-specific &mdash; the
 * column's name and its sort direction, since {@code TableColumn.SortType} and {@code TreeTableColumn.SortType}
 * are unrelated enums &mdash; is left to each concrete subclass.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTableColumnInfo implements Serializable {

    /**
     * This column's position among the control's visible columns, or a negative value if the column is
     * currently hidden. See {@link ColumnInfo#isVisible()}.
     */
    private int index;

    private Double width;

    private Integer sortIndex;

    /**
     * Returns this column's position among the control's visible columns, or a negative value if the column is
     * currently hidden.
     *
     * @return the column's index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets this column's position among the control's visible columns. Set to a negative value to mark the
     * column as currently hidden.
     *
     * @param index the column's new index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns this column's persisted preferred width in pixels, or {@code null} if none was ever recorded.
     *
     * @return the column's width, or {@code null}
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Sets this column's persisted preferred width in pixels.
     *
     * @param width the column's new width, or {@code null} to clear it
     */
    public void setWidth(Double width) {
        this.width = width;
    }

    /**
     * Returns this column's position among the control's sort-order columns, or {@code null} if it is not
     * currently part of the sort.
     *
     * @return the column's sort index, or {@code null}
     */
    public Integer getSortIndex() {
        return sortIndex;
    }

    /**
     * Sets this column's position among the control's sort-order columns.
     *
     * @param sortIndex the column's new sort index, or {@code null} to drop it out of the sort order
     */
    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }
}
