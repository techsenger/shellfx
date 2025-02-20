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

package com.techsenger.tabshell.kit.dialog.alert;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.kit.dialog.AbstractSimpleDialogView;
import static com.techsenger.tabshell.kit.dialog.alert.AlertDialogType.ERROR;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class AlertDialogView<T extends AlertDialogViewModel> extends AbstractSimpleDialogView<T> {

    private final FontIconView messageIconView = new FontIconView();

    private final Label messageLabel = new Label();

    private final HBox messageBox = new HBox(messageIconView, messageLabel);

    public AlertDialogView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        getButtonBox().getChildren().add(getOkButton());
        getContentPane().getStylesheets().add(AlertDialogView.class.getResource("alert.css").toExternalForm());
        this.messageIconView.getStyleClass().addAll(viewModel.getMessageIcon(), "message-icon-view");
        this.messageIconView.setMinWidth(Label.USE_PREF_SIZE);
        switch (viewModel.getDialogType()) {
            case INFO:
                this.messageIconView.getStyleClass().add(Styles.ACCENT);
            break;
            case ERROR:
                this.messageIconView.getStyleClass().add(Styles.DANGER);
            break;
            case WARNING:
                this.messageIconView.getStyleClass().add(Styles.WARNING);
            break;
            default:
                throw new AssertionError();
        }

        this.messageLabel.getStyleClass().add("message-label");
        this.messageLabel.textProperty().bind(viewModel.messageProperty());
        DoubleBinding messageLabelWidth = Bindings.createDoubleBinding(
                () -> messageBox.getWidth() - messageIconView.getWidth() - 2 * SizeConstants.INSET, //0.5 + 0.5 + 1
                messageBox.widthProperty(), messageIconView.widthProperty());
        this.messageLabel.prefWidthProperty().bind(messageLabelWidth);
        this.messageLabel.minWidthProperty().bind(messageLabelWidth);
        this.messageLabel.maxWidthProperty().bind(messageLabelWidth);
        this.messageLabel.setPadding(new Insets(SizeConstants.INSET, 0, 0, 0));

        this.messageBox.getStyleClass().add("message-box");
        this.messageBox.setSpacing(SizeConstants.HALF_INSET);
        this.messageBox.setPadding(new Insets(0, SizeConstants.INSET, 0, SizeConstants.HALF_INSET));
        VBox.setVgrow(messageBox, Priority.ALWAYS);
        getContentPane().getChildren().addAll(messageBox, getButtonBox());
    }

    protected FontIconView getMessageIconView() {
        return messageIconView;
    }

    protected Label getMessageLabel() {
        return messageLabel;
    }

    protected HBox getMessageBox() {
        return messageBox;
    }
}
