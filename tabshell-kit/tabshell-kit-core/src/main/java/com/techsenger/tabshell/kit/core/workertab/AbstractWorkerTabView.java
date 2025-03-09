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

package com.techsenger.tabshell.kit.core.workertab;

import com.techsenger.tabshell.core.CloseScope;
import com.techsenger.tabshell.core.TabShellView;
import com.techsenger.tabshell.kit.core.splittab.AbstractSplitTabView;
import com.techsenger.tabshell.kit.core.style.CoreIcons;
import com.techsenger.tabshell.kit.core.tabmanager.TabManagerView;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import org.controlsfx.control.StatusBar;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWorkerTabView<T extends AbstractWorkerTabViewModel> extends AbstractSplitTabView<T> {

    private final StatusBar statusBar = new StatusBar();

    private final Hyperlink workerCountLink = new Hyperlink("0", new FontIconView(CoreIcons.PROCESS));

    private final TabManagerView bottomTabManager;

    public AbstractWorkerTabView(TabShellView<?> tabShell, T viewModel) {
        super(tabShell, viewModel);
        this.bottomTabManager = new TabManagerView(viewModel.getBottomTabManager());
    }

    @Override
    public boolean doOnCloseRequest(CloseScope scope) {
        getViewModel().cancelAllWorkers();
        return true;
    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        this.bottomTabManager.initialize();
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        this.getBottomPane().getChildren().add(bottomTabManager.getNode());
        workerCountLink.setOnAction((e) -> viewModel.openWorkerReportTab());
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
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        viewModel.workerCountProperty().addListener((ov, oldV, newV) ->
                this.workerCountLink.setText(Integer.toString(newV.intValue())));
    }

    @Override
    protected void postDeinitialize(T viewModel) {
        super.postDeinitialize(viewModel);
        this.bottomTabManager.deinitialize();
    }

    protected StatusBar getStatusBar() {
        return statusBar;
    }

    protected TabManagerView getBottomTabManager() {
        return bottomTabManager;
    }
}
