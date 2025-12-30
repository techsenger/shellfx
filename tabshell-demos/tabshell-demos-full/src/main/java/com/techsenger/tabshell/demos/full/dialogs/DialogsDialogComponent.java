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

package com.techsenger.tabshell.demos.full.dialogs;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.demos.full.DemoComponentNames;
import com.techsenger.tabshell.dialogs.alert.AlertDialogComponent;
import com.techsenger.tabshell.dialogs.alert.AlertDialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogComponent;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogView;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogComponent;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogComponent;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogView;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogComponent extends AbstractSimpleDialogComponent<DialogsDialogView> {

    protected class Mediator extends AbstractSimpleDialogComponent.Mediator implements DialogsDialogMediator {

        private final DialogsDialogComponent component = DialogsDialogComponent.this;

        @Override
        public void addAlertDialog(AlertDialogViewModel vm) {
            var v = new AlertDialogView<>(vm);
            var c = new AlertDialogComponent<>(v);
            c.initialize();
            component.shell.addDialog(c);
        }

        @Override
        public void addYesNoDialog(YesNoDialogViewModel vm) {
            var v = new YesNoDialogView<>(vm);
            var c = new YesNoDialogComponent<>(v);
            c.initialize();
            component.shell.addDialog(c);
        }

        @Override
        public void addFileChooserDialog(FileChooserDialogViewModel vm) {
            var v = new FileChooserDialogView<>(vm);
            var c = new FileChooserDialogComponent<>(v, component.shell);
            c.initialize();
            component.shell.addDialog(c);
        }

        @Override
        public ShellViewModel<?> getShell() {
            return component.shell.getView().getViewModel();
        }
    }

    private final ShellComponent<?> shell;

    public DialogsDialogComponent(DialogsDialogView view, ShellComponent<?> shell) {
        super(view);
        this.shell = shell;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    @Override
    public Name getName() {
        return DemoComponentNames.DEMO_DIALOGS_DIALOG;
    }

}
