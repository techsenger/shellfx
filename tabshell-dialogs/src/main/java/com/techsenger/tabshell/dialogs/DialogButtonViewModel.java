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

package com.techsenger.tabshell.dialogs;

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
public class DialogButtonViewModel {

    private final ObjectProperty<Runnable> action = new SimpleObjectProperty<>();

    private final BooleanProperty disable = new SimpleBooleanProperty(false);

    private final BooleanProperty isDefault = new SimpleBooleanProperty(false);

    private final BooleanProperty visible = new SimpleBooleanProperty(true);

    private final StringProperty text = new SimpleStringProperty();

    public DialogButtonViewModel() {
        this(null);
    }

    public DialogButtonViewModel(String text) {
        this(text, null);
    }

    public DialogButtonViewModel(String text, Runnable action) {
        this(text, action, false);
    }

    public DialogButtonViewModel(String text, Runnable action, boolean isDefault) {
        setText(text);
        setAction(action);
        setDefault(isDefault);
    }

    public BooleanProperty disableProperty() {
        return disable;
    }

    public boolean isDisable() {
        return disable.get();
    }

    public void setDisable(boolean disable) {
        this.disable.set(disable);
    }

    public ObjectProperty<Runnable> actionProperty() {
        return action;
    }

    public Runnable getAction() {
        return action.get();
    }

    public void setAction(Runnable action) {
        this.action.set(action);
    }

    public BooleanProperty defaultProperty() {
        return isDefault;
    }

    public boolean isDefault() {
        return isDefault.get();
    }

    public void setDefault(boolean isDefault) {
        this.isDefault.set(isDefault);
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(boolean value) {
        this.visible.set(value);
    }

    public StringProperty textProperty() {
        return text;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }
}
