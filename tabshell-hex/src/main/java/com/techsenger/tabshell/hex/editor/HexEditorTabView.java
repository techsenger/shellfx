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

package com.techsenger.tabshell.hex.editor;

import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabView<T extends HexEditorTabViewModel<?>, S extends HexEditorTabComponent<?>>
        extends AbstractShellTabView<T, S> {

    public HexEditorTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void doOnSelected() {
        super.doOnSelected();
        getComponent().getArea().getView().requestFocus();
    }

    void addContent(HexToolBarView<?, ?> toolBar, DockLayoutView<?, ?> layout) {
        VBox.setVgrow(layout.getNode(), Priority.ALWAYS);
        getContentPane().getChildren().addAll(toolBar.getNode(), layout.getNode());
    }

}
