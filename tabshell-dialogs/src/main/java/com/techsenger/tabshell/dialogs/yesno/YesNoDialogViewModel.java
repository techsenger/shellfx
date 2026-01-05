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

package com.techsenger.tabshell.dialogs.yesno;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogViewModel;
import com.techsenger.tabshell.core.dialog.DialogMediator;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.dialogs.DialogButtonViewModel;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class YesNoDialogViewModel<T extends DialogMediator> extends AbstractDialogViewModel<T> {

    private StringProperty message = new SimpleStringProperty();

    private final DialogButtonViewModel yes = new DialogButtonViewModel("Yes", this.closeActionProperty().get(), true);

    private final DialogButtonViewModel no = new DialogButtonViewModel("No", this.closeActionProperty().get());

    private final DialogButtonViewModel cancel = new DialogButtonViewModel("Cancel", this.closeActionProperty().get());

    public YesNoDialogViewModel(DialogScope scope, String message) {
        super(scope, false);
        this.message.set(message);
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

    public DialogButtonViewModel getYes() {
        return yes;
    }

    public DialogButtonViewModel getNo() {
        return no;
    }

    public DialogButtonViewModel getCancel() {
        return cancel;
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
    protected void initialize() {
        super.initialize();
        prefWidthProperty().set(600);
        setButtonWidthEqual(true);
    }
}
