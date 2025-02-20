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

package com.techsenger.tabshell.kit.terminal;

import com.techsenger.tabshell.core.dialog.AbstractDialogHelper;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.kit.dialog.StandardDialogHelper;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabHelper<T extends TerminalTabView> extends AbstractDialogHelper<T>
        implements StandardDialogHelper<T> {

    public TerminalTabHelper(T view) {
        super(view);
    }

    @Override
    public void openDialog(DialogView<?> dialog) {
        getView().getDialogManager().openDialog(dialog);
    }

    public void showFindPane(FindPaneViewModel findViewModel) {
        var findView = new FindPaneView(getView().getWidget(), findViewModel);
        findView.initialize();
        getView().showFind(findView);
    }

    public void hideFindPane() {
        getView().hideFind();
    }

}
