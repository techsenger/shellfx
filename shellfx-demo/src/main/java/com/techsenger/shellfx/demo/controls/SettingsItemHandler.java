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

package com.techsenger.shellfx.demo.controls;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.material.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.settings.SettingsDialogView;
import com.techsenger.shellfx.demo.settings.SettingsDialogParams;
import com.techsenger.shellfx.demo.settings.SettingsDialogViewModel;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class SettingsItemHandler extends AbstractMenuItemHandler<ShellView<?>, ManagedMenuItem> {

    public SettingsItemHandler(ShellView<?> component, ManagedMenuItem item) {
        super(component, item);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        var appearance = shell.getViewModel().getContext().getSettings().getAppearance();
        var params = new SettingsDialogParams(WindowType.NESTED, appearance);
        var viewModel = new SettingsDialogViewModel<>(params);
        var view = new SettingsDialogView<>(viewModel);
        view.initialize();
        shell.getComposer().addDialog(view);
    }
}
