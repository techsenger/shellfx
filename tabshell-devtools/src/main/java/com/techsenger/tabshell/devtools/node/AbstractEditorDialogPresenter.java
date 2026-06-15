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

package com.techsenger.tabshell.devtools.node;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.style.DevToolsIcons;
import com.techsenger.tabshell.dialogs.alert.AlertDialogParams;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractEditorDialogPresenter<V extends EditorDialogView> extends AbstractDialogPresenter<V>
        implements EditorDialogPort {

    private final EditPropertyTask<?> task;

    private boolean valueUpdated;

    private String propertyName;

    public AbstractEditorDialogPresenter(V view, EditorDialogParams params) {
        super(view, params);
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
        if (Objects.equals(this.propertyName, propertyName)) {
            return;
        }
        this.propertyName = propertyName;
        getView().setPropertyName(propertyName);
    }

    @Override
    public boolean isPropertyUpdated() {
        return this.valueUpdated;
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DevToolsComponents.EDITOR_DIALOG);
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
    protected void applyAppearance() {
        super.applyAppearance();
        setWidth(600);
    }

    protected EditPropertyTask<?> getTask() {
        return task;
    }

    protected String getPropertyName() {
        return propertyName;
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
        var alertDialog = getView().getComposer().openAlertDialog(alertParams);
        alertDialog.setMessage("Failed to apply the value.");
        alertDialog.setOnClosed(() -> requestFocus());
    }
}
