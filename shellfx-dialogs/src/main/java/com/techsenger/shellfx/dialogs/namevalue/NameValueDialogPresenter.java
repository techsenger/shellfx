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

import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogPresenter;
import com.techsenger.shellfx.core.dialog.DialogParams;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class NameValueDialogPresenter<V extends NameValueDialogView> extends AbstractDialogPresenter<V>
        implements NameValueDialogPort {

    private String name;

    private boolean nameEditable;

    private String value;

    private boolean valueEditable;

    public NameValueDialogPresenter(V view, DialogParams params) {
        super(view, params);
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
        return name;
    }

    @Override
    public void setName(String name) {
        if (Objects.equals(this.name, name)) {
            return;
        }
        this.name = name;
        getView().updateName(name);
    }

    @Override
    public boolean isNameEditable() {
        return nameEditable;
    }

    @Override
    public void setNameEditable(boolean nameEditable) {
        if (this.nameEditable == nameEditable) {
            return;
        }
        this.nameEditable = nameEditable;
        getView().updateNameEditable(nameEditable);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        if (Objects.equals(this.value, value)) {
            return;
        }
        this.value = value;
        getView().updateValue(value);
    }

    @Override
    public boolean isValueEditable() {
        return valueEditable;
    }

    @Override
    public void setValueEditable(boolean valueEditable) {
        if (this.valueEditable == valueEditable) {
            return;
        }
        this.valueEditable = valueEditable;
        getView().updateValueEditable(valueEditable);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setRightButtons(NameValueButtons.CANCEL, NameValueButtons.OK);
        setMinWidth(400);
        setMinHeight(250);
    }
}
