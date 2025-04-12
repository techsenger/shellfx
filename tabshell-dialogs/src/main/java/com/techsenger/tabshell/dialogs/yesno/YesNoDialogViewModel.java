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
public class YesNoDialogViewModel extends AbstractDialogViewModel {

    private StringProperty message = new SimpleStringProperty();

    private final ObjectProperty<Runnable> yesAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty yesDisable = new SimpleBooleanProperty();

    private final BooleanProperty yesDefault = new SimpleBooleanProperty(true);

    private final StringProperty yesText = new SimpleStringProperty("Yes");

    private final ObjectProperty<Runnable> noAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty noDisable = new SimpleBooleanProperty();

    private final BooleanProperty noDefault = new SimpleBooleanProperty();

    private final StringProperty noText = new SimpleStringProperty("No");

    private final ObjectProperty<Runnable> cancelAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty cancelDisable = new SimpleBooleanProperty();

    private final BooleanProperty cancelDefault = new SimpleBooleanProperty();

    private final StringProperty cancelText = new SimpleStringProperty("Cancel");

    private final BooleanProperty cancelVisible = new SimpleBooleanProperty(false);

    public YesNoDialogViewModel(DialogScope scope, String message) {
        super(scope, false);
        this.message.set(message);
        prefWidthProperty().set(600);
        setButtonWidthEqual(true);
    }

    @Override
    public DialogKey getKey() {
        return DialogComponentKeys.YES_NO_DIALOG;
    }

    /* ************************************************* yes button ************************************************* */

    public ObjectProperty<Runnable> yesActionProperty() {
        return yesAction;
    }

    public Runnable getYesAction() {
        return yesAction.get();
    }

    public void setYesAction(Runnable action) {
        this.yesAction.set(action);
    }

    public BooleanProperty yesDisableProperty() {
        return yesDisable;
    }

    public boolean isYesDisable() {
        return yesDisable.get();
    }

    public void setYesDisable(boolean disable) {
        this.yesDisable.set(disable);
    }

    public BooleanProperty yesDefaultProperty() {
        return yesDefault;
    }

    public boolean isYesDefault() {
        return yesDefault.get();
    }

    public void setYesDefault(boolean yesDefault) {
        this.yesDefault.set(yesDefault);
    }

    public StringProperty yesTextProperty() {
        return yesText;
    }

    public String getYesText() {
        return yesText.get();
    }

    public void setYesText(String text) {
        this.yesText.set(text);
    }

    /* ************************************************** no button ************************************************* */

    public ObjectProperty<Runnable> noActionProperty() {
        return noAction;
    }

    public Runnable getNoAction() {
        return noAction.get();
    }

    public void setNoAction(Runnable action) {
        this.noAction.set(action);
    }

    public BooleanProperty noDisableProperty() {
        return noDisable;
    }

    public boolean isNoDisable() {
        return noDisable.get();
    }

    public void setNoDisable(boolean disable) {
        this.noDisable.set(disable);
    }

    public BooleanProperty noDefaultProperty() {
        return noDefault;
    }

    public boolean isNoDefault() {
        return noDefault.get();
    }

    public void setNoDefault(boolean noDefault) {
        this.noDefault.set(noDefault);
    }

    public StringProperty noTextProperty() {
        return noText;
    }

    public String getNoText() {
        return noText.get();
    }

    public void setNoText(String text) {
        this.noText.set(text);
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
