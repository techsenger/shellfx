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

package com.techsenger.shellfx.demo.shared;

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.tab.AbstractTabFxView;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DockableTabFxView extends AbstractTabFxView<DockableTabPresenter> {

    private final Label label = new Label();

    private final StackPane stackPane = new StackPane(label);

    public DockableTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        getContentBox().getChildren().add(stackPane);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
    }

    @Override
    protected void bind() {
        super.bind();
        label.textProperty().bind(getNode().textProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // when a Pane inside a Tab requests focus, it receives it, but immediately loses it because
        // TabPane captures the focus. To fix it we request focus on next pulse.
        this.stackPane.setOnMouseClicked(e -> Platform.runLater(() -> this.stackPane.requestFocus()));
    }
}
