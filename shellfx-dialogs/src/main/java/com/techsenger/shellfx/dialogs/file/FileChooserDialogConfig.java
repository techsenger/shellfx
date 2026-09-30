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

package com.techsenger.shellfx.dialogs.file;

import com.techsenger.shellfx.core.dialog.DialogConfig;
import com.techsenger.shellfx.material.table.TableColumnInfo;
import com.techsenger.shellfx.material.table.TableConfig;
import com.techsenger.shellfx.storage.FileColumns;
import java.io.Serial;
import java.util.ArrayList;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogConfig extends DialogConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private Mode mode = Mode.LIST;

    private TableConfig table;

    public FileChooserDialogConfig() {
        setWidth(800);
        setHeight(500);
        var columns = new ArrayList<TableColumnInfo>();
        var nameColumn = new TableColumnInfo(FileColumns.NAME);
        nameColumn.setIndex(0);
        nameColumn.setSortIndex(0);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);
        columns.add(nameColumn);

        var sizeColumn = new TableColumnInfo(FileColumns.SIZE);
        sizeColumn.setIndex(1);
        columns.add(sizeColumn);

        var modifiedColumn = new TableColumnInfo(FileColumns.LAST_MODIFIED);
        modifiedColumn.setIndex(2);
        columns.add(modifiedColumn);
        this.table = new TableConfig(columns);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public TableConfig getTable() {
        return table;
    }

    public void setTable(TableConfig table) {
        this.table = table;
    }
}
