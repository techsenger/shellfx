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

package com.techsenger.shellfx.devtools.node;

import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractEditorDialogViewModel<C extends EditorDialogComposer> extends AbstractDialogViewModel<C>
        implements FullEditorDialogPort {

    private final StringProperty propertyName = new SimpleStringProperty();

    private final EditPropertyTask<?> task;

    private boolean valueUpdated;

    public AbstractEditorDialogViewModel(EditorDialogParams params) {
        super(params);
        this.task = params.getTask();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPropertyName(String propertyName) {
        this.propertyName.set(propertyName);
    }

    @Override
    public StringProperty propertyNameProperty() {
        return propertyName;
    }

    @Override
    public boolean isPropertyUpdated() {
        return this.valueUpdated;
    }

    @Override
    public String getPropertyName() {
        return propertyName.get();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setRightButtons(EditorDialogButtons.CANCEL, EditorDialogButtons.OK);
        setButtonDefault(EditorDialogButtons.OK, true);
        setTitle("Property Editor");
        setIcon(DevToolsIcons.EDIT);
        setPropertyName(task.getAttribute().name());
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        setWidth(600);
    }

    protected EditPropertyTask<?> getTask() {
        return task;
    }

    protected void setValueUpdated(boolean valueUpdated) {
        this.valueUpdated = valueUpdated;
    }

    protected <T> void applyValue(EditPropertyTask<T> task, String text) throws Exception {
        T newValue = task.getConverter().convert(text);
        task.getSetter().accept(newValue);
        setValueUpdated(true);
    }

    void openErrorDialog() {
        var alertParams = new AlertDialogParams(getWindowType(), getAppearanceSettings(), AlertDialogType.ERROR);
        var alertDialog = getComposer().openAlertDialog(alertParams);
        alertDialog.setMessage("Failed to apply the value.");
        alertDialog.setOnClosed(() -> requestFocus());
    }
}
