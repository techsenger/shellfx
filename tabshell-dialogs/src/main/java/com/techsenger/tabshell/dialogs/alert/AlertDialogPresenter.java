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

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.dialogs.DialogComponents;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.ERROR;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.INFO;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.WARNING;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.icon.Icon;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class AlertDialogPresenter<V extends AlertDialogView> extends AbstractDialogPresenter<V>
        implements AlertDialogPort {

    private final AlertDialogType dialogType;

    private String message;

    private Icon<?> messageIcon;

    public AlertDialogPresenter(V view, AlertDialogParams params) {
        super(view, params);
        this.dialogType = params.getDialogType();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AlertDialogType getDialogType() {
        return dialogType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
        getView().setMessage(message);
    }

    @Override
    public Icon<?> getMessageIcon() {
        return messageIcon;
    }

    @Override
    public void setMessageIcon(Icon<?> messageIcon) {
        this.messageIcon = messageIcon;
        getView().setMessageIcon(messageIcon);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DialogComponents.ALERT_DIALOG);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        switch (dialogType) {
            case INFO:
                setTitle("Info");
                setMessageIcon(DialogIcons.INFO);
            break;
            case ERROR:
                setTitle("Error");
                setMessageIcon(DialogIcons.ERROR);
            break;
            case WARNING:
                setTitle("Warning");
                setMessageIcon(DialogIcons.WARNING);
            break;
            case CONFIRMATION:
                setTitle("Confirm");
                setMessageIcon(DialogIcons.QUESTION);
                break;
            default:
                throw new AssertionError("Unknown type - " + dialogType);
        }
        if (dialogType != AlertDialogType.CONFIRMATION) {
            setRightButtons(AlertDialogButtons.OK);
            setButtonDefault(AlertDialogButtons.OK, true);
        } else {
            setRightButtons(AlertDialogButtons.NO, AlertDialogButtons.YES);
            setButtonDefault(AlertDialogButtons.YES, true);
        }
        setOnResult((result) -> closeSafely());
        setMessage(message);
        setMinWidth(400);
        setMinHeight(140);
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        setWidth(600);
    }


}
