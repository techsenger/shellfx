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

package com.techsenger.tabshell.dialogs.yesno;

import atlantafx.base.theme.Styles;
import com.techsenger.mvvm4fx.core.ComponentComposer;
import com.techsenger.tabshell.core.ActionUtils;
import com.techsenger.tabshell.core.dialog.AbstractDialogView;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.dialogs.base.BaseDialogComposer;
import com.techsenger.tabshell.dialogs.base.DefaultBaseDialogComposer;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.dialogs.utils.ViewUtils;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
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
public class YesNoDialogView extends AbstractDialogView<YesNoDialogViewModel> {

    private final FontIconView messageIconView = new FontIconView();

    private final Label messageLabel = new Label();

    private final HBox messageBox = new HBox(messageIconView, messageLabel);

    private final Button yesButton = new Button();

    private final Button noButton = new Button();

    private final Button cancelButton = new Button();

    private final HBox buttonBox = new HBox();

    public YesNoDialogView(YesNoDialogViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(getNode());
    }

    @Override
    public BaseDialogComposer<?> getComposer() {
        return (BaseDialogComposer<?>) super.getComposer();
    }

    @Override
    protected void build(YesNoDialogViewModel viewModel) {
        super.build(viewModel);
        getContentPane().getStylesheets()
                .add(YesNoDialogView.class.getResource("yesno.css").toExternalForm());
        messageIconView.setIcon(DialogIcons.QUESTION);
        messageIconView.getStyleClass().addAll("message-icon-view", Styles.WARNING);
        messageLabel.textProperty().bind(viewModel.messageProperty());
        messageLabel.getStyleClass().add("message-label");

        ViewUtils.buildIconedMessageBox(messageIconView, messageLabel, messageBox);

        this.buttonBox.getStyleClass().add(StyleClasses.CORNERS_BOTTOM);
        this.buttonBox.setPadding(new Insets(SizeConstants.INSET));
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setSpacing(SizeConstants.INSET);
        buttonBox.getChildren().addAll(cancelButton, noButton, yesButton);

        getContentPane().getChildren().addAll(messageBox, getButtonBox());
    }

    @Override
    protected void bind(YesNoDialogViewModel viewModel) {
        super.bind(viewModel);
        yesButton.disableProperty().bind(viewModel.yesDisableProperty());
        yesButton.defaultButtonProperty().bind(viewModel.yesDefaultProperty());
        yesButton.textProperty().bind(viewModel.yesTextProperty());

        noButton.disableProperty().bind(viewModel.noDisableProperty());
        noButton.defaultButtonProperty().bind(viewModel.noDefaultProperty());
        noButton.textProperty().bind(viewModel.noTextProperty());

        cancelButton.disableProperty().bind(viewModel.cancelDisableProperty());
        cancelButton.defaultButtonProperty().bind(viewModel.cancelDefaultProperty());
        cancelButton.textProperty().bind(viewModel.cancelTextProperty());
        cancelButton.visibleProperty().bind(viewModel.cancelVisibleProperty());
    }

    @Override
    protected void addHandlers(YesNoDialogViewModel viewModel) {
        super.addHandlers(viewModel);
        yesButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.yesActionProperty()));
        noButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.noActionProperty()));
        cancelButton.setOnAction(e -> ActionUtils.runIfExists(viewModel.cancelActionProperty()));
    }

    @Override
    protected ComponentComposer<?> createComposer() {
        return new DefaultBaseDialogComposer<>(this);
    }

    protected Button getConfirmButton() {
        return yesButton;
    }

    protected Button getDenyButton() {
        return noButton;
    }

    protected Button getCancelButton() {
        return cancelButton;
    }

    protected HBox getButtonBox() {
        return buttonBox;
    }

    @Override
    protected void makeEqualButtons() {
        if (getViewModel().isCancelVisible()) {
            ButtonUtils.makeEqualWidthBySize(getCancelButton(), getDenyButton(), getConfirmButton());
        } else {
            ButtonUtils.makeEqualWidthBySize(getDenyButton(), getConfirmButton());
        }
    }
}
