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

import com.techsenger.mvvm4fx.core.ComponentViewModel;
import com.techsenger.tabshell.core.dialog.AbstractDialogComposer;
import com.techsenger.tabshell.core.dialog.AbstractDialogView;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultStandardDialogComposer extends AbstractDialogComposer<AbstractDialogView<?>>
            implements StandardDialogComposer<AbstractDialogView<?>> {

    private final class ViewModelComposer extends AbstractDialogComposer.ViewModelComposer
            implements StandardDialogComposer.ViewModelComposer {

        @Override
        public void openAlertDialog(AlertDialogViewModel viewModel) {
            DefaultStandardDialogComposer.this.openAlertDialog(viewModel);
        }

        @Override
        public void openYesNoDialog(YesNoDialogViewModel viewModel) {
            DefaultStandardDialogComposer.this.openYesNoDialog(viewModel);
        }

        @Override
        public void openFileChooserDialog(FileChooserDialogViewModel viewModel) {
            DefaultStandardDialogComposer.this.openFileChooserDialog(viewModel);
        }

    }

    private final DialogManager dialogManager;

    public DefaultStandardDialogComposer(DialogManager dialogManager, AbstractDialogView<?> view) {
        super(view);
        this.dialogManager = dialogManager;
    }

    @Override
    public ViewModelComposer getViewModelComposer() {
        return (ViewModelComposer) super.getViewModelComposer();
    }

    @Override
    public void openDialog(DialogView dialog) {
        dialogManager.openDialog(dialog);
    }

    @Override
    protected ComponentViewModel.Composer createViewModelComposer() {
        return new ViewModelComposer();
    }
}
