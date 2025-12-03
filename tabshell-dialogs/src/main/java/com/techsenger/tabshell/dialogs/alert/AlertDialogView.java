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

package com.techsenger.tabshell.dialogs.alert;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogView;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.ERROR;
import com.techsenger.tabshell.dialogs.utils.ViewUtils;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

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
        NodeUtils.requestFocus(getNode());
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        getButtonBox().getChildren().addAll(getCancelButton(), getOkButton());
        getContentPane().getStylesheets().add(AlertDialogView.class.getResource("alert.css").toExternalForm());
        messageIconView.getStyleClass().add("message-icon-view");
        messageIconView.setIcon(viewModel.getMessageIcon());
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
        messageLabel.textProperty().bind(viewModel.messageProperty());
        messageLabel.getStyleClass().add("message-label");
        ViewUtils.buildIconedMessageBox(messageIconView, messageLabel, messageBox);
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

    @Override
    protected void makeEqualButtons() {
        if (getViewModel().isCancelVisible()) {
            ButtonUtils.makeEqualWidthBySize(getCancelButton(), getOkButton());
        }
    }
}
