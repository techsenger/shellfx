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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.dialog.DialogContainerComponent;
import com.techsenger.tabshell.dialogs.DialogComponentNames;
import com.techsenger.tabshell.dialogs.alert.AlertDialogComponent;
import com.techsenger.tabshell.dialogs.alert.AlertDialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogComponent;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogComponent<T extends FileChooserDialogView<?, ?>>
        extends AbstractSimpleDialogComponent<T> {

    protected class Mediator extends AbstractSimpleDialogComponent.Mediator implements FileChooserDialogMediator {

        @Override
        public void addAlertDialog(AlertDialogViewModel vm) {
            var view = new AlertDialogView<>(vm);
            var component = new AlertDialogComponent<>(view);
            component.initialize();
            dialogContainer.addDialog(component);
        }
    }

    private final DialogContainerComponent<?> dialogContainer;

    public FileChooserDialogComponent(T view, DialogContainerComponent<?> dc) {
        super(view);
        this.dialogContainer = dc;
    }

    @Override
    public Name getName() {
        return DialogComponentNames.FILE_CHOOSER_DIALOG;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }
}
