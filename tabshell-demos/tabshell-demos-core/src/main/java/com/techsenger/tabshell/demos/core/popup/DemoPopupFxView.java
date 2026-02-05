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

package com.techsenger.tabshell.demos.core.popup;

import com.techsenger.tabshell.core.popup.AbstractPopupFxView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 *
 * @author Pavel Castornii
 */
public class DemoPopupFxView extends AbstractPopupFxView<DemoPopupPresenter> implements DemoPopupView {

    private final Label label = new Label("Popup!");

    private final Button closeButton = new Button("Close");

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        getContentBox().getChildren().addAll(label, closeButton);
        getContentBox().setSpacing(20);
        getContentBox().setStyle("-fx-border-width: 2px; -fx-border-color: -color-accent-7; "
                + "-fx-padding: 20; -fx-background-color: -color-bg-default");
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        closeButton.setOnAction(e -> getPresenter().requestClose());
    }
}
