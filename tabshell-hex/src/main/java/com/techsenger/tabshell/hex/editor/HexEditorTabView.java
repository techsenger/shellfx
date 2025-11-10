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

package com.techsenger.tabshell.hex.editor;

import com.techsenger.mvvm4fx.core.ComponentMediator;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.hex.data.DataInspectorTabView;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import javafx.geometry.Orientation;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabView<T extends HexEditorTabViewModel> extends AbstractShellTabView<T> {

    private HexToolBarView<?> toolBar;

    private DockLayoutView<?> layout;

    private HexAreaView<?> area;

    private DataInspectorTabView<?> dataInspector;

    public HexEditorTabView(ShellView<?> shell, T viewModel) {
        super(shell, viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void doOnSelected() {
        super.doOnSelected();
        this.area.requestFocus();
    }

    public HexToolBarView<?> getToolBar() {
        return toolBar;
    }

    public DockLayoutView<?> getLayout() {
        return layout;
    }

    public HexAreaView<?> getArea() {
        return area;
    }

    public DataInspectorTabView<?> getDataInspector() {
        return dataInspector;
    }

    @Override
    protected ComponentMediator createMediator() {
        return new HexEditorTabMediator(this);
    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        viewModel.createComponents();
        this.toolBar = createToolBar(viewModel);
        this.toolBar.initialize();
        this.layout = createLayout(viewModel);
        this.layout.initialize();
        this.area = createArea(viewModel);
        this.area.initialize();
        this.dataInspector = createDataInspector();
        this.dataInspector.initialize();
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        var css = HexEditorTabView.class.getResource("hexeditor.css").toExternalForm();
        getContentPane().getStylesheets().add(css);
        getContentPane().getChildren().addAll(this.toolBar.getNode(), this.layout.getNode());
        this.layout.setMain(this.area);
        var splitSpace = layout.createSplitSpace(Orientation.HORIZONTAL);
        layout.setRoot(splitSpace);
        splitSpace.getChildren().add(this.area);
        var tabDock = layout.createTabDock();
        tabDock.openTab(dataInspector);
        this.layout.addTabDock(tabDock, Side.RIGHT, 250);
    }

    @Override
    protected void postInitialize(T viewModel) {
        super.postInitialize(viewModel);
        viewModel.readFile();
    }

    @Override
    protected void postDeinitialize(T viewModel) {
        super.postDeinitialize(viewModel);
        if (getDataInspector() != null) {
            getDataInspector().deinitialize();
        }
        this.area.deinitialize();
        this.layout.deinitialize();
        this.toolBar.deinitialize();
    }

    protected HexToolBarView<?> createToolBar(T viewModel) {
        return new HexToolBarView<>(viewModel.getToolBar());
    }

    protected DockLayoutView<?> createLayout(T viewModel) {
        return new DockLayoutView<>(viewModel.getLayout());
    }

    protected HexAreaView<?> createArea(T viewModel) {
        return new HexAreaView<>(viewModel.getArea());
    }

    protected DataInspectorTabView<?> createDataInspector() {
        return new DataInspectorTabView<>(getViewModel().getDataInspector());
    }

}
