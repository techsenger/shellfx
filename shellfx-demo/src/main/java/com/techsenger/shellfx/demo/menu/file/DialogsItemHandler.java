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

package com.techsenger.shellfx.demo.menu.file;

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.demo.dialogs.DialogsDialogFxView;
import com.techsenger.shellfx.demo.dialogs.DialogsDialogParams;
import com.techsenger.shellfx.demo.dialogs.DialogsDialogPresenter;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsItemHandler extends AbstractMenuItemHandler<ShellFxView<?>, ManagedMenuItem> {

    public DialogsItemHandler(ShellFxView<?> component, ManagedMenuItem item) {
        super(component, item);
    }

    @Override
    public void onAction() {
        var shellV = getComponent();
        var shellP = shellV.getPresenter();
        var dialogView = new DialogsDialogFxView();
        var dialogParams = new DialogsDialogParams(
                shellP.getContext().getSettings().getAppearance(),
                shellP.getContext().getHistoryManager());
        var dialogPresenter = new DialogsDialogPresenter(dialogView, dialogParams);
        dialogPresenter.initialize();
        shellV.getComposer().addDialog(dialogView);
    }
}
