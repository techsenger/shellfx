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

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.tab.AbstractShellTabComponent;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogComponent;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogView;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.tabshell.hex.HexComponentNames;
import com.techsenger.tabshell.hex.inspector.DataInspectorTabComponent;
import com.techsenger.tabshell.hex.inspector.DataInspectorTabView;
import com.techsenger.tabshell.hex.inspector.DataInspectorTabViewModel;
import com.techsenger.tabshell.layout.dock.DockLayoutComponent;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import com.techsenger.tabshell.layout.dock.DockLayoutViewModel;
import javafx.geometry.Orientation;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabComponent<T extends HexEditorTabView<?, ?>> extends AbstractShellTabComponent<T> {

    protected class Mediator extends AbstractShellTabComponent.Mediator implements HexEditorTabMediator {

        private final HexEditorTabComponent<?> component = HexEditorTabComponent.this;

        @Override
        public HexToolBarViewModel getToolBar() {
            return component.getToolBar().getView().getViewModel();
        }

        @Override
        public DockLayoutViewModel<?> getLayout() {
            return component.getLayout().getView().getViewModel();
        }

        @Override
        public HexAreaViewModel<?> getArea() {
            return component.getArea().getView().getViewModel();
        }

        @Override
        public DataInspectorTabViewModel getDataInspector() {
            return component.getDataInspector().getView().getViewModel();
        }

        @Override
        public void addFileChooserDialog(FileChooserDialogViewModel<?> viewModel) {
            var v = new FileChooserDialogView<>(viewModel);
            var shell = component.getShell();
            var c = new FileChooserDialogComponent<>(v, shell.getSettings().getAppearance(),
                    shell.getHistoryManager(), component);
            c.initialize();
            shell.addDialog(c);
        }
    }

    private final HexToolBarComponent<?> toolBar;

    private final DockLayoutComponent<?> layout;

    private final HexAreaComponent<?> area;

    private final DataInspectorTabComponent<?> dataInspector;

    public HexEditorTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
        setHistoryProvider(() -> shell.getHistoryManager()
                .getOrCreateHistory(HexEditorTabHistory.class, HexEditorTabHistory:: new));
        this.toolBar = createToolBar();
        getModifiableChildren().add(this.toolBar);
        this.layout = createLayout();
        getModifiableChildren().add(this.layout);
        this.area = createArea();
        getModifiableChildren().add(this.area);
        this.dataInspector = createDataInspector();
        getModifiableChildren().add(this.dataInspector);
    }

    public HexToolBarComponent<?> getToolBar() {
        return toolBar;
    }

    public DockLayoutComponent<?> getLayout() {
        return layout;
    }

    public HexAreaComponent<?> getArea() {
        return area;
    }

    public DataInspectorTabComponent<?> getDataInspector() {
        return dataInspector;
    }

    @Override
    public ComponentName getName() {
        return HexComponentNames.HEX_EDITOR_TAB;
    }

    @Override
    public HexEditorTabHistory<?> getHistory() {
        return (HexEditorTabHistory<?>) super.getHistory();
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        this.toolBar.initialize();
        this.layout.initialize();
        this.area.initialize();
        this.dataInspector.initialize();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getView().addContent(toolBar.getView(), layout.getView());
        this.layout.setMain(this.area);
        var splitSpace = layout.createSplitSpace(Orientation.HORIZONTAL);
        layout.setRoot(splitSpace);
        splitSpace.addChild(this.area);
        var tabDock = layout.createTabDock();
        tabDock.addTab(dataInspector);
        this.layout.addTabDock(tabDock, Side.RIGHT, 350);
    }

    protected HexToolBarComponent<?> createToolBar() {
        var v = new HexToolBarView<>(new HexToolBarViewModel());
        var c = new HexToolBarComponent<>(v);
        return c;
    }

    protected DockLayoutComponent<?> createLayout() {
        var vm = new DockLayoutViewModel();
        var v = new DockLayoutView<>(vm);
        var c = new DockLayoutComponent<>(v, () -> getHistory().getDockLayout());
        return c;
    }

    protected HexAreaComponent<?> createArea() {
        var appearance = getShell().getSettings().getAppearance();
        var vm = new HexAreaViewModel<>(appearance, getView().getViewModel().getDocument());
        var v = new HexAreaView<>(vm);
        var c = new HexAreaComponent<>(v, toolBar);
        return c;
    }

    protected DataInspectorTabComponent<?> createDataInspector() {
        var vm = new DataInspectorTabViewModel(getView().getViewModel().getDocument(),
                this.area.getCaret().getView().getViewModel().offsetProperty());
        var v = new DataInspectorTabView<>(vm);
        var c = new DataInspectorTabComponent<>(v);
        return c;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }
}
