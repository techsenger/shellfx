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

package com.techsenger.tabshell.demo.menu.file;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.menu.AbstractMenuItemHandler;
import com.techsenger.tabshell.demo.Density;
import com.techsenger.tabshell.demo.dialogs.DialogsDialogFxView;
import com.techsenger.tabshell.demo.dialogs.DialogsDialogParams;
import com.techsenger.tabshell.demo.dialogs.DialogsDialogPresenter;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsItemHandler extends AbstractMenuItemHandler<ShellFxView<?>> {

    public DialogsItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
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
        dialogView.getNode().getStyleClass().add(Density.STYLE_CLASS); // see Density javadoc
//        StackPane.setAlignment(dialogView.getNode(), Pos.TOP_LEFT);
        shellV.getComposer().addWindow(dialogView);
    }

}
