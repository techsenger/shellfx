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

import com.techsenger.tabshell.core.dialog.DialogKey;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogViewModel;
import com.techsenger.tabshell.dialogs.DialogComponentKeys;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class AlertDialogViewModel extends AbstractSimpleDialogViewModel {

    private final AlertDialogType dialogType;

    private StringProperty message = new SimpleStringProperty();

    private final String messageIcon;

    public AlertDialogViewModel(DialogScope scope, AlertDialogType dialogType, String message) {
        super(scope, false);
        this.dialogType = dialogType;
        switch (dialogType) {
            case INFO:
                this.titleProperty().set("Info");
                this.messageIcon = DialogIcons.INFO;
            break;
            case ERROR:
                this.titleProperty().set("Error");
                this.messageIcon = DialogIcons.ERROR;
            break;
            case WARNING:
                this.titleProperty().set("Warning");
                this.messageIcon = DialogIcons.WARNING;
            break;
            default:
                throw new AssertionError("Unknown type - " + dialogType);
        }
        prefWidthProperty().set(600);
        this.message.set(message);
        setCancelVisible(false);
        setButtonWidthEqual(true);
    }

    @Override
    public DialogKey getKey() {
        return DialogComponentKeys.ALERT_DIALOG;
    }

    public AlertDialogType getDialogType() {
        return dialogType;
    }

    public StringProperty messageProperty() {
        return this.message;
    }

    public String getMessage() {
        return this.message.get();
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public String getMessageIcon() {
        return messageIcon;
    }
}
