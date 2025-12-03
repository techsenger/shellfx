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

import com.techsenger.tabshell.core.tab.AbstractShellTabComposer;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DialogShellTabComposer<T extends AbstractShellTabView<?>>
        extends AbstractShellTabComposer<T> implements StandardDialogComposer<T> {

    protected class Mediator extends AbstractShellTabComposer.Mediator implements DialogShellTabMediator {

        @Override
        public void openAlertDialog(AlertDialogViewModel viewModel) {
            DialogShellTabComposer.this.openAlertDialog(viewModel);
        }

        @Override
        public void openYesNoDialog(YesNoDialogViewModel viewModel) {
            DialogShellTabComposer.this.openYesNoDialog(viewModel);
        }

        @Override
        public void openFileChooserDialog(FileChooserDialogViewModel viewModel) {
            DialogShellTabComposer.this.openFileChooserDialog(viewModel);
        }
    }

    public DialogShellTabComposer(T view) {
        super(view);
    }

    @Override
    public DialogShellTabMediator getMediator() {
        return (DialogShellTabMediator) super.getMediator();
    }

    @Override
    protected DialogShellTabMediator createMediator() {
        return new DialogShellTabComposer.Mediator();
    }
}

