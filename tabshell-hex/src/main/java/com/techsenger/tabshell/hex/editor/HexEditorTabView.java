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

import com.techsenger.mvvm4fx.core.ComponentComposer;
import com.techsenger.mvvm4fx.core.ComponentView;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.hex.inspector.DataInspectorTabView;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabView<T extends HexEditorTabViewModel> extends AbstractShellTabView<T> {

    public interface Composer extends ComponentView.Composer {

        HexToolBarView<?> getToolBar();

        void addToolBar(VBox contentPane);

        void addLayout(VBox contentPane);

        DockLayoutView<?> getLayout();

        HexAreaView<?> getArea();

        DataInspectorTabView<?> getDataInspector();
    }

    public HexEditorTabView(ShellView<?> shell, T viewModel) {
        super(shell, viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void doOnSelected() {
        super.doOnSelected();
        getComposer().getArea().requestFocus();
    }

    @Override
    protected ComponentComposer<?> createComposer() {
        return new HexEditorTabComposer<>(this);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        getComposer().initialize();
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        getComposer().addToolBar(getContentPane());
        getComposer().addLayout(getContentPane());
    }

    @Override
    protected void postInitialize(T viewModel) {
        super.postInitialize(viewModel);
        viewModel.readFile();
    }

    @Override
    protected void postDeinitialize(T viewModel) {
        super.postDeinitialize(viewModel);
        getComposer().deinitialize();
    }


}
