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
import com.techsenger.tabshell.core.window.WindowPosition;
import com.techsenger.tabshell.core.window.WindowType;
import com.techsenger.tabshell.demo.settings.SettingsDialogFxView;
import com.techsenger.tabshell.demo.settings.SettingsDialogParams;
import com.techsenger.tabshell.demo.settings.SettingsDialogPresenter;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class SettingsItemHandler extends AbstractMenuItemHandler<ShellFxView<?>> {

    public SettingsItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        var appearance = shell.getPresenter().getContext().getSettings().getAppearance();
        var view = new SettingsDialogFxView();
        var params = new SettingsDialogParams(WindowType.NESTED, appearance);
        var presenter = new SettingsDialogPresenter(view, params);
        presenter.initialize();
        shell.getComposer().addWindow(view);
        shell.getComposer().alignWindowToStage(view, WindowPosition.CENTER);
    }
}
