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

import com.techsenger.mvvm4fx.core.ComponentView;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogView;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogView;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;

/**
 * This dialog helper is used to open standard dialogs, like alert, info etc.
 *
 * @author Pavel Castornii
 */
public interface StandardDialogComposer<T extends ComponentView<?>> {

    /**
     * Opens a dialog.
     * @param dialog
     */
    void openDialog(DialogView<?> dialog);

    default void openAlertDialog(AlertDialogViewModel viewModel) {
        var view = new AlertDialogView<AlertDialogViewModel>(viewModel);
        view.initialize();
        openDialog(view);
    }

    default void openYesNoDialog(YesNoDialogViewModel viewModel) {
        var view = new YesNoDialogView(viewModel);
        view.initialize();
        openDialog(view);
    }

    default void openFileChooserDialog(FileChooserDialogViewModel viewModel) {
        var view = new FileChooserDialogView(viewModel);
        view.initialize();
        openDialog(view);
    }
}
