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

package com.techsenger.tabshell.tabs.workertab;

import com.techsenger.tabshell.core.tab.TabWorker;
import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.CloseScope;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class WorkerReportTabView extends AbstractTabView<WorkerReportTabViewModel> {

    private final TableView<TabWorker<?>> table = new TableView<>();

    public WorkerReportTabView(WorkerReportTabViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        table.requestFocus();
    }

    @Override
    public boolean doOnCloseRequest(CloseScope scope) {
        return true;
    }

    @Override
    protected void build(WorkerReportTabViewModel viewModel) {
        super.build(viewModel);
        this.table.setItems(viewModel.getWorkers());
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.table.getStyleClass().addAll(Styles.DENSE, Styles.BORDERED);
        this.table.setPlaceholder(new Label());
        VBox.setVgrow(table, Priority.ALWAYS);
        this.getContentPane().getChildren().add(table);
        this.buildTableColumns();
    }

    private void buildTableColumns() {
        var titleColumn = new TableColumn<TabWorker<?>, String>("Title");
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        titleColumn.setResizable(true);
        titleColumn.setMaxWidth(300);
        titleColumn.setMinWidth(300);

        var messageColumn = new TableColumn<TabWorker<?>, String>("Message");
        messageColumn.setCellValueFactory(data -> data.getValue().messageProperty());
        messageColumn.setResizable(true);

        var progressColumn = new TableColumn<TabWorker<?>, Double>("Progress");
        progressColumn.setCellValueFactory(data -> (ReadOnlyProperty) data.getValue().progressProperty());
        progressColumn.setCellFactory(ProgressBarTableCell.<TabWorker<?>>forTableColumn());
        progressColumn.setMaxWidth(200);
        progressColumn.setMinWidth(200);
        progressColumn.setResizable(false);
        this.table.getColumns().addAll(titleColumn, messageColumn, progressColumn);
    }
}
