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

package com.techsenger.tabshell.dialogs.confirmation;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.ActionUtils;
import com.techsenger.tabshell.core.dialog.AbstractDialogView;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.dialogs.StandardDialogHelper;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.dialogs.utils.ViewUtils;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class ConfirmationDialogView extends AbstractDialogView<ConfirmationDialogViewModel> {

    private final FontIconView messageIconView = new FontIconView();

    private final Label messageLabel = new Label();

    private final HBox messageBox = new HBox(messageIconView, messageLabel);

    private final Button confirmButton = new Button();

    private final Button denyButton = new Button();

    private final Button cancelButton = new Button();

    private final HBox buttonBox = new HBox();

    public ConfirmationDialogView(ConfirmationDialogViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(getNode());
    }

    @Override
    protected void build(ConfirmationDialogViewModel viewModel) {
        super.build(viewModel);
        getContentPane().getStylesheets()
                .add(ConfirmationDialogView.class.getResource("confirmation.css").toExternalForm());
        messageIconView.getStyleClass().addAll(DialogIcons.CONFIRMATION, "message-icon-view", Styles.WARNING);
        messageLabel.textProperty().bind(viewModel.messageProperty());
        messageLabel.getStyleClass().add("message-label");

        ViewUtils.buildIconedMessageBox(messageIconView, messageLabel, messageBox);

        this.buttonBox.getStyleClass().add(StyleClasses.CORNERS_BOTTOM);
        this.buttonBox.setPadding(new Insets(SizeConstants.INSET));
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setSpacing(SizeConstants.INSET);
        buttonBox.getChildren().addAll(cancelButton, denyButton, confirmButton);

        getContentPane().getChildren().addAll(messageBox, getButtonBox());
    }

    @Override
    protected void bind(ConfirmationDialogViewModel viewModel) {
        super.bind(viewModel);
        confirmButton.disableProperty().bind(viewModel.confirmDisableProperty());
        confirmButton.defaultButtonProperty().bind(viewModel.confirmDefaultProperty());
        confirmButton.textProperty().bind(viewModel.confirmTextProperty());

        denyButton.disableProperty().bind(viewModel.denyDisableProperty());
        denyButton.defaultButtonProperty().bind(viewModel.denyDefaultProperty());
        denyButton.textProperty().bind(viewModel.denyTextProperty());

        cancelButton.disableProperty().bind(viewModel.cancelDisableProperty());
        cancelButton.defaultButtonProperty().bind(viewModel.cancelDefaultProperty());
        cancelButton.textProperty().bind(viewModel.cancelTextProperty());
        cancelButton.visibleProperty().bind(viewModel.cancelVisibleProperty());
    }

    @Override
    protected void addHandlers(ConfirmationDialogViewModel viewModel) {
        super.addHandlers(viewModel);
        confirmButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.confirmActionProperty()));
        denyButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.denyActionProperty()));
        cancelButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.cancelActionProperty()));
    }

    @Override
    protected StandardDialogHelper<?> createComponentHelper() {
        return new StandardDialogHelper() {
            @Override
            public void openDialog(DialogView dialog) {
                getDialogManager().openDialog(dialog);
            }
        };
    }

    protected Button getConfirmButton() {
        return confirmButton;
    }

    protected Button getDenyButton() {
        return denyButton;
    }

    protected Button getCancelButton() {
        return cancelButton;
    }

    protected HBox getButtonBox() {
        return buttonBox;
    }
}
