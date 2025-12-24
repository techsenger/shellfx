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

package com.techsenger.tabshell.core.tab;

import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.dialog.DialogComponent;
import com.techsenger.tabshell.core.dialog.DialogScope;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractShellTabComponent<T extends AbstractShellTabView<?, ?>> extends AbstractTabComponent<T>
        implements ShellTabComponent<T> {

    protected class Mediator extends AbstractTabComponent.Mediator implements ShellTabMediator {

        @Override
        public ShellViewModel<?> getShell() {
            return shell.getView().getViewModel();
        }
    }

    private final ShellComponent<?> shell;

    public AbstractShellTabComponent(T view, ShellComponent<?> shell) {
        super(view);
        this.shell = shell;
    }

    @Override
    public ShellComponent<?> getShell() {
        return this.shell;
    }

    @Override
    public void addDialog(DialogComponent<?> dialog) {
        var scope = dialog.getView().getViewModel().getScope();
        if (scope == getSupportedDialogScope()) {
            getView().getDialogManager().showDialog(dialog.getView());
            getModifiableChildren().add(dialog);
        } else {
            shell.addDialog(dialog);
        }
    }


    @Override
    public void removeDialog(DialogComponent<?> dialog) {
        var scope = dialog.getView().getViewModel().getScope();
        if (scope == getSupportedDialogScope()) {
            getView().getDialogManager().hideDialog(dialog.getView());
            getModifiableChildren().remove(dialog);
            dialog.deinitializeTree();
        } else {
            shell.removeDialog(dialog);
        }
    }

    @Override
    public DialogScope getSupportedDialogScope() {
        return DialogScope.TAB;
    }

    @Override
    public List<? extends DialogComponent<?>> getDialogs() {
        return getView().getDialogManager().getDialogs().stream().map(d -> d.getComponent()).toList();
    }

    @Override
    protected abstract Mediator createMediator();
}

