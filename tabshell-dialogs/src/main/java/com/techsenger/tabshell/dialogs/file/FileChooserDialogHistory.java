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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.tabshell.storage.FileColumnKeys;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogHistory;
import com.techsenger.tabshell.material.button.ToggleButtonHistory;
import com.techsenger.tabshell.material.table.TableColumnHistory;
import com.techsenger.tabshell.material.table.TableHistory;
import java.util.ArrayList;
import javafx.scene.control.TableColumn;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogHistory<T extends FileChooserDialogViewModel>
        extends AbstractSimpleDialogHistory<T> {

    private ToggleButtonHistory listButton;

    private ToggleButtonHistory detailsButton;

    private TableHistory table;

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        this.listButton = new ToggleButtonHistory();
        this.listButton.setSelected(true);
        this.detailsButton = new ToggleButtonHistory();

        var columns = new ArrayList<TableColumnHistory>();
        var typeColumn = new TableColumnHistory(FileColumnKeys.TYPE.toString());
        columns.add(typeColumn);
        typeColumn.setSortIndex(0);
        typeColumn.setSortType(TableColumn.SortType.ASCENDING);
        var nameColumn = new TableColumnHistory(FileColumnKeys.NAME.toString());
        columns.add(nameColumn);
        nameColumn.setSortIndex(1);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);
        var sizeColumn = new TableColumnHistory(FileColumnKeys.SIZE.toString());
        columns.add(sizeColumn);
        var modifiedColumn = new TableColumnHistory(FileColumnKeys.LAST_MODIFIED.toString());
        columns.add(modifiedColumn);
        this.table = new TableHistory(columns);

        setWidth(800);
        setHeight(500);
    }

    @Override
    public void saveAppearance(T viewModel) {
        super.saveAppearance(viewModel);
        this.listButton.setSelected(viewModel.listSelectedProperty().get());
        this.detailsButton.setSelected(viewModel.detailsSelectedProperty().get());
        this.table = viewModel.getTableHistory();
    }

    @Override
    public void restoreAppearance(T viewModel) {
        super.restoreAppearance(viewModel);
        viewModel.listSelectedProperty().set(this.listButton.isSelected());
        viewModel.detailsSelectedProperty().set(this.detailsButton.isSelected());
        viewModel.setTableHistory(table);
    }
}
