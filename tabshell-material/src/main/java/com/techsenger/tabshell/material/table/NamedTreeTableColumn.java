/*
 * Copyright 2024-2025 Pavel Castornii.
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

import com.techsenger.tabshell.material.Named;
import javafx.scene.control.TreeTableColumn;

/**
 *
 * @author Pavel Castornii
 */
public class NamedTreeTableColumn<S, T> extends TreeTableColumn<S, T> implements Named {

    private final TreeTableColumnName name;

    public NamedTreeTableColumn(TreeTableColumnName name) {
        super();
        this.name = name;
    }

    public NamedTreeTableColumn(TreeTableColumnName name, String string) {
        super(string);
        this.name = name;
    }

    @Override
    public TreeTableColumnName getName() {
        return name;
    }
}
