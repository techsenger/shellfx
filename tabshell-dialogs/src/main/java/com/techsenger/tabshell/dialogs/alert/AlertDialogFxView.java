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

package com.techsenger.tabshell.dialogs.alert;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.ERROR;
import com.techsenger.tabshell.dialogs.utils.FxViewUtils;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class AlertDialogFxView<P extends AlertDialogPresenter<?, ?>> extends AbstractDialogFxView<P>
        implements AlertDialogView {

    private final IconViewBox messageIconViewBox = new IconViewBox();

    private final Label messageLabel = new Label();

    private final HBox messageBox = new HBox(messageIconViewBox, messageLabel);

    private final ResultButton okButton = new ResultButton(AlertDialogButtons.OK, "OK");

    private final ResultButton cancelButton = new ResultButton(AlertDialogButtons.CANCEL, "Cancel");

    private final ResultButton yesButton = new ResultButton(AlertDialogButtons.YES, "Yes");

    private final ResultButton noButton = new ResultButton(AlertDialogButtons.NO, "No");

    public AlertDialogFxView() {
        super();
    }

    @Override
    public void setMessage(String message) {
        this.messageLabel.setText(message);
    }

    @Override
    public void setMessageIcon(Icon<?> icon) {
        this.messageIconViewBox.setIcon(icon);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(getNode());
    }

    @Override
    protected void build() {
        super.build();
        getContentBox().getStylesheets().add(AlertDialogFxView.class.getResource("alert.css").toExternalForm());
        messageIconViewBox.getStyleClass().add("icon-view-box");
        switch (getPresenter().getDialogType()) {
            case INFO:
                this.messageIconViewBox.getStyleClass().add(Styles.ACCENT);
            break;
            case ERROR:
                this.messageIconViewBox.getStyleClass().add(Styles.DANGER);
            break;
            case WARNING:
                this.messageIconViewBox.getStyleClass().add(Styles.WARNING);
            break;
            case CONFIRMATION:
                this.messageIconViewBox.getStyleClass().addAll(Styles.WARNING);
            break;
            default:
                throw new AssertionError();
        }
        messageLabel.getStyleClass().add("message-label");
        FxViewUtils.buildIconedMessageBox(messageIconViewBox, messageLabel, messageBox);
        getContentBox().getChildren().add(messageBox);
        registerButtons(okButton, cancelButton, noButton, yesButton);
    }

    public IconViewBox getMessageIconViewBox() {
        return messageIconViewBox;
    }

    protected Label getMessageLabel() {
        return messageLabel;
    }

    protected HBox getMessageBox() {
        return messageBox;
    }
}
