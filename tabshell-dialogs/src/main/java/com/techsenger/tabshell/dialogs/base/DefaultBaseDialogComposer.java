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

package com.techsenger.tabshell.dialogs.base;

import com.techsenger.tabshell.core.dialog.AbstractDialogComposer;
import com.techsenger.tabshell.core.dialog.AbstractDialogView;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultBaseDialogComposer<T extends AbstractDialogView<?>> extends AbstractDialogComposer<T>
        implements BaseDialogComposer<T> {

    protected class Mediator extends AbstractDialogComposer.Mediator implements BaseDialogMediator {

        @Override
        public void openAlertDialog(AlertDialogViewModel viewModel) {
            DefaultBaseDialogComposer.this.openAlertDialog(viewModel);
        }

        @Override
        public void openYesNoDialog(YesNoDialogViewModel viewModel) {
            DefaultBaseDialogComposer.this.openYesNoDialog(viewModel);
        }

        @Override
        public void openFileChooserDialog(FileChooserDialogViewModel viewModel) {
            DefaultBaseDialogComposer.this.openFileChooserDialog(viewModel);
        }
    }

    public DefaultBaseDialogComposer(T view) {
        super(view);
    }

    @Override
    public void openDialog(DialogView<?> dialog) {
        getView().getDialogManager().openDialog(dialog);
    }

    @Override
    public BaseDialogMediator createMediator() {
        return new DefaultBaseDialogComposer.Mediator();
    }
}
