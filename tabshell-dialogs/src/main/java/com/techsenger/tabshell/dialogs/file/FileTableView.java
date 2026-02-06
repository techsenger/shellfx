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

import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.table.NamedTableColumn;
import com.techsenger.tabshell.material.table.TableHistory;
import com.techsenger.tabshell.material.table.TableHistoryUtils;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.tabshell.storage.FileColumnBuilder;
import com.techsenger.tabshell.storage.FileColumnNames;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 *
 * @author Pavel Castornii
 */
class FileTableView extends TableView<GenericFile> {

    private final FileColumnBuilder columnBuilder;

    private final FileStringConverter stringConverter = new FileStringConverter();

    FileTableView(ObservableList<GenericFile> files, AppearanceSettings settings) {
        super(files);
        this.columnBuilder = new FileColumnBuilder(settings.getRegularFont());
        getStyleClass().addAll(StyleClasses.EXTRA_DENSE, StyleClasses.SAME_SPACING_COLUMN);
        setEditable(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setPlaceholder(new Label(""));
    }

    TableColumn<GenericFile, ?> findNameColumn() {
        for (var c : getColumns()) {
            var keyedColumn = (NamedTableColumn<?, ?>) c;
            if (keyedColumn.getName() == FileColumnNames.NAME) {
                return c;
            }
        }
        return null;
    }

    void restoreHistory(TableHistory history) {
        Function<String, TableColumn<?, ?>> columnProvider = (name) -> {
            if (name.equals(FileColumnNames.TYPE.toString())) {
                var typeColumn = columnBuilder.buildTypeColumn(SharedIcons.DIRECTORY, SharedIcons.FILE);
                typeColumn.setEditable(false);
                typeColumn.getStyleClass().add(StyleClasses.SAME_SPACING_COLUMN_FIRST);
                return typeColumn;
            } else if (name.equals(FileColumnNames.NAME.toString())) {
                var nameColumn = columnBuilder.buildNameColumn();
                nameColumn.setEditable(false);
                nameColumn.setCellFactory(r -> new TextFieldTableCell<>(stringConverter));
                return nameColumn;
            } else if (name.equals(FileColumnNames.SIZE.toString())) {
                var sizeColumn = columnBuilder.buildSizeColumn();
                sizeColumn.setEditable(false);
                return sizeColumn;
            } else if (name.equals(FileColumnNames.LAST_MODIFIED.toString())) {
                var lastModifiedColumn = columnBuilder.buildLastModifiedColumn();
                lastModifiedColumn.setEditable(false);
                lastModifiedColumn.getStyleClass().add(StyleClasses.SAME_SPACING_COLUMN_LAST);
                return lastModifiedColumn;
            }
            throw new AssertionError();
        };
        TableHistoryUtils.restoreTable(this, columnProvider, history, false);
    }

    TableHistory createHistory() {
        return TableHistoryUtils.createHistory(this);
    }

}
