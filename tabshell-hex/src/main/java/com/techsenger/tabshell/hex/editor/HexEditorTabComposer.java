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

import com.techsenger.tabshell.dialogs.DialogShellTabComposer;
import com.techsenger.tabshell.hex.inspector.DataInspectorTabView;
import com.techsenger.tabshell.hex.inspector.DataInspectorTabViewModel;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import com.techsenger.tabshell.layout.dock.DockLayoutViewModel;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabComposer<T extends HexEditorTabView<?>> extends DialogShellTabComposer<T> {

    protected class Mediator extends DialogShellTabComposer.Mediator implements HexEditorTabMediator {

        public Mediator() {
        }

        @Override
        public HexToolBarViewModel getToolBar() {
            return toolBar.getViewModel();
        }

        @Override
        public DockLayoutViewModel getLayout() {
            return layout.getViewModel();
        }

        @Override
        public HexAreaViewModel getArea() {
            return area.getViewModel();
        }

        @Override
        public DataInspectorTabViewModel getDataInspector() {
            return dataInspector.getViewModel();
        }
    }

    private final HexEditorTabView<?> editor;

    private final HexToolBarView<?> toolBar;

    private final DockLayoutView<?> layout;

    private final HexAreaView<?> area;

    private final DataInspectorTabView<?> dataInspector;

    public HexEditorTabComposer(T view) {
        super(view);
        this.editor = getView();
        this.toolBar = createToolBar();
        this.layout = createLayout();
        this.area = createArea();
        this.dataInspector = createDataInspector();
    }

    public HexToolBarView<?> getToolBar() {
        return toolBar;
    }

    public void addToolBar(VBox content) {
        content.getChildren().add(toolBar.getNode());
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

    public void addLayout(VBox contentPane) {
        contentPane.getChildren().add(layout.getNode());
    }

    @Override
    public void initialize() {
        toolBar.initialize();
        layout.initialize();
        area.initialize();
        dataInspector.initialize();
        this.layout.setMain(this.area);
        var splitSpace = layout.createSplitSpace(Orientation.HORIZONTAL);
        layout.setRoot(splitSpace);
        splitSpace.getChildren().add(this.area);
        var tabDock = layout.createTabDock();
        tabDock.openTab(dataInspector);
        this.layout.addTabDock(tabDock, Side.RIGHT, 300);
    }

    @Override
    public void deinitialize() {
        if (getDataInspector() != null) {
            getDataInspector().deinitialize();
        }
        this.area.deinitialize();
        this.layout.deinitialize();
        this.toolBar.deinitialize();
    }

    @Override
    public HexEditorTabMediator createMediator() {
        return new HexEditorTabComposer.Mediator();
    }

    protected HexToolBarView<?> createToolBar() {
        return new HexToolBarView<>(new HexToolBarViewModel());
    }

    protected DockLayoutView<?> createLayout() {
        HexEditorTabHistory<?> history = (HexEditorTabHistory<?>) editor.getViewModel().getHistoryProvider().provide();
        var vm = new DockLayoutViewModel(history.getDockLayout());
        return new DockLayoutView<>(vm);
    }

    protected HexAreaView<?> createArea() {
        var appearance = editor.getShell().getViewModel().getSettings().getAppearance();
        var areaVM = new HexAreaViewModel(toolBar.getViewModel(), appearance, editor.getViewModel().getDocument());
        return new HexAreaView<>(areaVM);
    }

    protected DataInspectorTabView<?> createDataInspector() {
        var vm = new DataInspectorTabViewModel(editor.getViewModel().getDocument(),
                this.area.getCaret().getViewModel().offsetProperty());
        return new DataInspectorTabView<>(vm);
    }
}
