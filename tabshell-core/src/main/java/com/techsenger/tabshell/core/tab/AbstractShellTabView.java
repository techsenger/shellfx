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

import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.TabShellView;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractShellTabView<T extends AbstractShellTabViewModel> extends AbstractTabView<T>
        implements ShellTabView<T> {

    private final DialogManager dialogManager;

    private final TabShellView<?> tabShell;

    public AbstractShellTabView(TabShellView<?> tabShell, T viewModel) {
        super(viewModel);
        this.tabShell = tabShell;
        this.dialogManager = new DefaultDialogManager(getWrapperPane(), getContentPane(),
                viewModel.dialogCountWrapper());
    }

    @Override
    public DialogManager getDialogManager() {
        return dialogManager;
    }

    @Override
    public TabShellView<?> getTabShell() {
        return this.tabShell;
    }

    @Override
    public void doOnSelected() {
        requestFocus();
    }
}
