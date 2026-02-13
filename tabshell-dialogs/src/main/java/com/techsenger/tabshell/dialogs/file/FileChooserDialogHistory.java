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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.tabshell.core.dialog.DialogHistory;
import com.techsenger.tabshell.material.table.TableHistory;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogHistory extends DialogHistory {

    private boolean listSelected;

    private boolean detailsSelected;

    private TableHistory table;

    public FileChooserDialogHistory() {

    }

    public boolean isListSelected() {
        return listSelected;
    }

    public void setListSelected(boolean listSelected) {
        this.listSelected = listSelected;
    }

    public boolean isDetailsSelected() {
        return detailsSelected;
    }

    public void setDetailsSelected(boolean detailsSelected) {
        this.detailsSelected = detailsSelected;
    }

    public TableHistory getTable() {
        return table;
    }

    public void setTable(TableHistory table) {
        this.table = table;
    }
}
