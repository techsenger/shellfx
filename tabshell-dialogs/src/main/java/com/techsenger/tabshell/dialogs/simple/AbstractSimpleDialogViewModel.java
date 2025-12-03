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

package com.techsenger.tabshell.dialogs.simple;

import com.techsenger.tabshell.core.dialog.AbstractDialogViewModel;
import com.techsenger.tabshell.core.dialog.DialogScope;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import com.techsenger.tabshell.dialogs.base.BaseDialogMediator;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSimpleDialogViewModel extends AbstractDialogViewModel {

    private final ObjectProperty<Runnable> okAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty okDisable = new SimpleBooleanProperty();

    private final BooleanProperty okDefault = new SimpleBooleanProperty(true);

    private final StringProperty okText = new SimpleStringProperty("OK");

    private final ObjectProperty<Runnable> cancelAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty cancelDisable = new SimpleBooleanProperty();

    private final BooleanProperty cancelDefault = new SimpleBooleanProperty();

    private final BooleanProperty cancelVisible = new SimpleBooleanProperty();

    private final StringProperty cancelText = new SimpleStringProperty("Cancel");

    public AbstractSimpleDialogViewModel(DialogScope scope, boolean resizable) {
        super(scope, resizable);
    }

    public ObjectProperty<Runnable> okActionProperty() {
        return okAction;
    }

    public Runnable getOkAction() {
        return okAction.get();
    }

    public void setOkAction(Runnable action) {
        this.okAction.set(action);
    }

    public BooleanProperty okDisableProperty() {
        return okDisable;
    }

    public boolean isOkDisable() {
        return okDisable.get();
    }

    public void setOkDisable(boolean disable) {
        this.okDisable.set(disable);
    }

    public BooleanProperty okDefault() {
        return okDefault;
    }

    public boolean isOkDefault() {
        return okDefault.get();
    }

    public void okDefault(boolean okDefault) {
        this.okDefault.set(okDefault);
    }

    public StringProperty okTextProperty() {
        return okText;
    }

    public String getOkText() {
        return okText.get();
    }

    public void setOkText(String text) {
        this.okText.set(text);
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

    public ObjectProperty<Runnable> cancelActionProperty() {
        return cancelAction;
    }

    public Runnable getCancelAction() {
        return cancelAction.get();
    }

    public void setCancelAction(Runnable action) {
        this.cancelAction.set(action);
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

    public BooleanProperty cancelVisibleProperty() {
        return cancelVisible;
    }

    public boolean isCancelVisible() {
        return cancelVisible.get();
    }

    public void setCancelVisible(boolean value) {
        cancelVisible.set(value);
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

    @Override
    public BaseDialogMediator getMediator() {
        return (BaseDialogMediator) super.getMediator();
    }
}
