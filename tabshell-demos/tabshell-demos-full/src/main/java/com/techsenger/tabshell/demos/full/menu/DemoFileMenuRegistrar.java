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

package com.techsenger.tabshell.demos.full.menu;

import com.techsenger.tabshell.core.CoreComponentNames;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demos.full.theme.ThemeDialogComponent;
import com.techsenger.tabshell.demos.full.theme.ThemeDialogView;
import com.techsenger.tabshell.demos.full.theme.ThemeDialogViewModel;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
import com.techsenger.tabshell.registrars.FileMenuRegistrar;

/**
 *
 * @author Pavel Castornii
 */
public class DemoFileMenuRegistrar extends FileMenuRegistrar {

    public DemoFileMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        super.register();
        registerThemeItem();
    }

    protected void registerThemeItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoFileMenuNames.THEME, "_Theme", 1000);
            item.setOnAction((e) -> {
                var shell = (ShellView<?, ?>) v;
                var appearance = shell.getViewModel().getSettings().getAppearance();
                var dialogViewModel = new ThemeDialogViewModel(appearance.getTheme());
                dialogViewModel.getOk().setAction(() -> {
                    appearance.themeProperty().set(dialogViewModel.themeProperty().get());
                    dialogViewModel.requestClose();
                });
                var dialogView = new ThemeDialogView(dialogViewModel);
                var dialogComponent = new ThemeDialogComponent(dialogView);
                dialogComponent.initialize();
                shell.getComponent().addDialog(dialogComponent);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoFileMenuNames.FILE_ACTIONS, f));
    }
}
