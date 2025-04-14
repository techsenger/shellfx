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

package com.techsenger.tabshell.tabs.splittab;

import com.techsenger.tabshell.core.dialog.AbstractDialogHelper;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.dialog.DialogView;

/**
 *
 * @author Pavel Castornii
 */
public class SplitTabHelper<T extends AbstractSplitTabView<?>> extends AbstractDialogHelper<T> {

    public SplitTabHelper(T view) {
        super(view);
    }

    @Override
    public void openDialog(DialogView<?> dialog) {
        var scope = dialog.getViewModel().getScope();
        if (scope == DialogScope.SHELL) {
            getView().getShell().getDialogManager().openDialog(dialog);
        } else {
            getView().getDialogManager().openDialog(dialog);
        }
    }
}
