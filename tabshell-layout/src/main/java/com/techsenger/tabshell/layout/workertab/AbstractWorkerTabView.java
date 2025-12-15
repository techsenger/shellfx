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

package com.techsenger.tabshell.layout.workertab;

import com.techsenger.tabshell.layout.splittab.AbstractSplitTabView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import org.controlsfx.control.StatusBar;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWorkerTabView<T extends AbstractWorkerTabViewModel<?>,
        S extends AbstractWorkerTabComponent<?>> extends AbstractSplitTabView<T, S> {

    private final StatusBar statusBar = new StatusBar();

    private final Hyperlink workerCountLink = new Hyperlink("0", new FontIconView(SharedIcons.PROCESS));

    public AbstractWorkerTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        // workerCountLink.setOnAction((e) -> viewModel.openWorkerReportTab());
        statusBar.setText(""); //to remove ok
        var hBox = new HBox(workerCountLink);
        hBox.getStyleClass().add("worker-pane");
        this.getContentPane().getChildren().add(statusBar);
        hBox.setAlignment(Pos.CENTER);
        statusBar.getRightItems().addAll(hBox);
        var css = AbstractWorkerTabView.class.getResource("workertab.css").toExternalForm();
        this.getContentPane().getStylesheets().add(css);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.workerCountLink.textProperty().bind(getViewModel().workerCountProperty().asString());
    }

    protected StatusBar getStatusBar() {
        return statusBar;
    }
}
