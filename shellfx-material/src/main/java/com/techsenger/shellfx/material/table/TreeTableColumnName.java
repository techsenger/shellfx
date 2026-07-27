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
 * Identifies a {@code TreeTableView} column independent of its current position, width or sort state &mdash;
 * the {@code N} key type used throughout {@link TreeTableColumnManager}/{@link AbstractTableColumnManager} and
 * {@link TreeTableColumnInfo}. Implementations are, by convention, enum constants (e.g. one enum per logical
 * tree table, with one constant per column it can show), which is what lets a single {@link TreeTableColumnInfo}
 * instance be built straight from the constant (see {@link TreeTableColumnInfo#TreeTableColumnInfo(Enum)}).
 *
 * <p>A separate marker interface from {@link TableColumnName} (rather than one shared name type for both column
 * families) so a {@code TreeTableView}'s columns and a {@code TableView}'s columns can never be mixed up at
 * compile time, e.g. passed to the wrong manager.
 *
 * @author Pavel Castornii
 */
public interface TreeTableColumnName extends Name {

}
