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

package com.techsenger.shellfx.dialogs.namevalue;

import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.window.WindowComposer;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class NameValueDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements FullNameValueDialogPort {

    private final StringProperty name = new SimpleStringProperty();

    private final BooleanProperty nameEditable = new SimpleBooleanProperty();

    private final StringProperty value = new SimpleStringProperty();

    private final BooleanProperty valueEditable = new SimpleBooleanProperty();

    public NameValueDialogViewModel(DialogParams params) {
        super(params);
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
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public boolean isNameEditable() {
        return nameEditable.get();
    }

    @Override
    public void setNameEditable(boolean nameEditable) {
        this.nameEditable.set(nameEditable);
    }

    @Override
    public BooleanProperty nameEditableProperty() {
        return nameEditable;
    }

    @Override
    public String getValue() {
        return value.get();
    }

    @Override
    public void setValue(String value) {
        this.value.set(value);
    }

    @Override
    public StringProperty valueProperty() {
        return value;
    }

    @Override
    public boolean isValueEditable() {
        return valueEditable.get();
    }

    @Override
    public void setValueEditable(boolean valueEditable) {
        this.valueEditable.set(valueEditable);
    }

    @Override
    public BooleanProperty valueEditableProperty() {
        return valueEditable;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setRightButtons(NameValueButtons.CANCEL, NameValueButtons.OK);
        setMinWidth(400);
        setMinHeight(250);
    }
}
