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

package com.techsenger.tabshell.demos.full.dock;

import com.techsenger.tabshell.core.tab.AbstractTabView;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DockableTabView extends AbstractTabView<DockableTabViewModel, DockableTabComponent> {

    private final Label label = new Label();

    private final StackPane stackPane = new StackPane(label);

    public DockableTabView(DockableTabViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        getContentPane().getChildren().add(stackPane);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
    }

    @Override
    protected void bind() {
        super.bind();
        label.textProperty().bind(getViewModel().titleProperty());
    }
}
