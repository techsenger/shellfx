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

import com.techsenger.shellfx.material.Named;
import javafx.scene.control.TreeTableColumn;

/**
 * A {@link TreeTableColumn} that carries a {@link TreeTableColumnName} identity alongside JavaFX's own header
 * text, so {@link TreeTableColumnManager}/{@link AbstractTableColumnManager} can track and rebuild it (position,
 * width, sort) by that stable identity rather than by header text, which is free-form and may not even be
 * unique.
 *
 * @param <S> the row type of the {@code TreeTableView} this column belongs to
 * @param <T> the cell value type this column displays
 * @author Pavel Castornii
 */
public class NamedTreeTableColumn<S, T> extends TreeTableColumn<S, T> implements Named {

    private final TreeTableColumnName name;

    /**
     * Creates a column identified by {@code name}, with no header text.
     *
     * @param name the column's stable identity
     */
    public <R extends Enum<R> & TreeTableColumnName> NamedTreeTableColumn(R name) {
        super();
        this.name = name;
    }

    /**
     * Creates a column identified by {@code name}, with {@code string} as its header text.
     *
     * @param name the column's stable identity
     * @param string the header text to display
     */
    public <R extends Enum<R> & TreeTableColumnName> NamedTreeTableColumn(R name, String string) {
        super(string);
        this.name = name;
    }

    /**
     * Returns this column's stable identity, as passed to the constructor.
     *
     * @return the column's name
     */
    public TreeTableColumnName getName() {
        return name;
    }
}
