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

import com.techsenger.tabshell.core.dialog.AbstractDialogViewModel;
import com.techsenger.tabshell.core.dialog.DialogKey;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.dialogs.DialogComponentKeys;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class ConfirmationDialogViewModel extends AbstractDialogViewModel {

    private StringProperty message = new SimpleStringProperty();

    private final ObjectProperty<Runnable> confirmAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty confirmDisable = new SimpleBooleanProperty();

    private final BooleanProperty confirmDefault = new SimpleBooleanProperty(true);

    private final StringProperty confirmText = new SimpleStringProperty("Confirm");

    private final ObjectProperty<Runnable> denyAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty denyDisable = new SimpleBooleanProperty();

    private final BooleanProperty denyDefault = new SimpleBooleanProperty();

    private final StringProperty denyText = new SimpleStringProperty("Deny");

    private final ObjectProperty<Runnable> cancelAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty cancelDisable = new SimpleBooleanProperty();

    private final BooleanProperty cancelDefault = new SimpleBooleanProperty();

    private final StringProperty cancelText = new SimpleStringProperty("Cancel");

    private final BooleanProperty cancelVisible = new SimpleBooleanProperty(false);

    public ConfirmationDialogViewModel(DialogScope scope, String message) {
        super(scope, false);
        this.message.set(message);
        prefWidthProperty().set(600);
        this.titleProperty().set("Confirmation");
    }

    @Override
    public DialogKey getKey() {
        return DialogComponentKeys.CONFIRMATION_DIALOG;
    }

    /* *********************************************** confirm button *********************************************** */

    public ObjectProperty<Runnable> confirmActionProperty() {
        return confirmAction;
    }

    public Runnable getConfirmAction() {
        return confirmAction.get();
    }

    public void setConfirmAction(Runnable action) {
        this.confirmAction.set(action);
    }

    public BooleanProperty confirmDisableProperty() {
        return confirmDisable;
    }

    public boolean isConfirmDisable() {
        return confirmDisable.get();
    }

    public void setConfirmDisable(boolean disable) {
        this.confirmDisable.set(disable);
    }

    public BooleanProperty confirmDefaultProperty() {
        return confirmDefault;
    }

    public boolean isConfirmDefault() {
        return confirmDefault.get();
    }

    public void setConfirmDefault(boolean confirmDefault) {
        this.confirmDefault.set(confirmDefault);
    }

    public StringProperty confirmTextProperty() {
        return confirmText;
    }

    public String getConfirmText() {
        return confirmText.get();
    }

    public void setConfirmText(String text) {
        this.confirmText.set(text);
    }

    /* ************************************************* deny button ************************************************ */

    public ObjectProperty<Runnable> denyActionProperty() {
        return denyAction;
    }

    public Runnable getDenyAction() {
        return denyAction.get();
    }

    public void setDenyAction(Runnable action) {
        this.denyAction.set(action);
    }

    public BooleanProperty denyDisableProperty() {
        return denyDisable;
    }

    public boolean isDenyDisable() {
        return denyDisable.get();
    }

    public void setDenyDisable(boolean disable) {
        this.denyDisable.set(disable);
    }

    public BooleanProperty denyDefaultProperty() {
        return denyDefault;
    }

    public boolean isDenyDefault() {
        return denyDefault.get();
    }

    public void setDenyDefault(boolean denyDefault) {
        this.denyDefault.set(denyDefault);
    }

    public StringProperty denyTextProperty() {
        return denyText;
    }

    public String getDenyText() {
        return denyText.get();
    }

    public void setDenyText(String text) {
        this.denyText.set(text);
    }

    /* *********************************************** cancel button ************************************************ */

    public ObjectProperty<Runnable> cancelActionProperty() {
        return cancelAction;
    }

    public Runnable getCancelAction() {
        return cancelAction.get();
    }

    public void setCancelAction(Runnable action) {
        this.cancelAction.set(action);
    }

    public BooleanProperty cancelDisableProperty() {
        return cancelDisable;
    }

    public boolean isCancelDisable() {
        return cancelDisable.get();
    }

    public void setCancelDisable(boolean disable) {
        this.cancelDisable.set(disable);
    }

    public BooleanProperty cancelDefaultProperty() {
        return cancelDefault;
    }

    public boolean isCancelDefault() {
        return cancelDefault.get();
    }

    public void setCancelDefault(boolean cancelDefault) {
        this.cancelDefault.set(cancelDefault);
    }

    public StringProperty cancelTextProperty() {
        return cancelText;
    }

    public String getCancelText() {
        return cancelText.get();
    }

    public void setCancelText(String text) {
        this.cancelText.set(text);
    }

    public BooleanProperty cancelVisibleProperty() {
        return cancelVisible;
    }

    public boolean isCancelVisible() {
        return cancelVisible.get();
    }

    public void setCancelVisible(boolean value) {
        cancelVisible.set(value);
    }

    /* *************************************************** other **************************************************** */

    public StringProperty messageProperty() {
        return this.message;
    }
}
