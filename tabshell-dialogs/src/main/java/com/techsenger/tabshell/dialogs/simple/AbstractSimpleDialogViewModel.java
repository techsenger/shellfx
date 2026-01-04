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
import com.techsenger.tabshell.core.dialog.DialogMediator;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.dialogs.DialogButtonViewModel;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSimpleDialogViewModel<T extends DialogMediator> extends AbstractDialogViewModel<T> {

    private final DialogButtonViewModel cancel = new DialogButtonViewModel("Cancel", this.closeActionProperty().get());

    private final DialogButtonViewModel ok = new DialogButtonViewModel("OK", this.closeActionProperty().get(), true);

    public AbstractSimpleDialogViewModel(DialogScope scope, boolean resizable) {
        super(scope, resizable);
    }

    public DialogButtonViewModel getCancel() {
        return cancel;
    }

    public DialogButtonViewModel getOk() {
        return ok;
    }
}
