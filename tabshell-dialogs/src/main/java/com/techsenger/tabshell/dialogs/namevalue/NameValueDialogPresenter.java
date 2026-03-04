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

package com.techsenger.tabshell.dialogs.namevalue;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.dialog.DialogComposer;
import com.techsenger.tabshell.dialogs.DialogComponents;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class NameValueDialogPresenter<V extends NameValueDialogView, C extends DialogComposer>
        extends AbstractDialogPresenter<V, C> implements NameValueDialogPort {

    private String name;

    private boolean nameEditable;

    private String value;

    private boolean valueEditable;

    public NameValueDialogPresenter(V view) {
        super(view);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DialogComponents.NAME_VALUE_DIALOG);
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
        this.name = name;
        getView().setName(name);
    }

    @Override
    public boolean isNameEditable() {
        return nameEditable;
    }

    @Override
    public void setNameEditable(boolean nameEditable) {
        this.nameEditable = nameEditable;
        getView().setNameEditable(valueEditable);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
        getView().setValue(value);
    }

    @Override
    public boolean isValueEditable() {
        return valueEditable;
    }

    @Override
    public void setValueEditable(boolean valueEditable) {
        this.valueEditable = valueEditable;
        getView().setValueEditable(valueEditable);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setRightButtons(NameValueButtons.CANCEL, NameValueButtons.OK);
    }
}
