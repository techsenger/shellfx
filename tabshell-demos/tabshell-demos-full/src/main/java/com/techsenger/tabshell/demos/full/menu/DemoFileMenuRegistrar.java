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

package com.techsenger.tabshell.demos.full.menu;

import com.techsenger.tabshell.core.CoreComponents;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demos.full.theme.ThemeDialogFxView;
import com.techsenger.tabshell.demos.full.theme.ThemeDialogPresenter;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class DemoFileMenuRegistrar extends AbstractControlRegistrar {

    public DemoFileMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        registerMenu();
        registerDefaultGroup();
        registerThemeItem();
    }

    protected void registerMenu() {
        ControlFactory<NamedMenu> f = (v) -> {
            return new NamedMenu(DemoFileMenu.FILE, "_File", 0);
        };
        addRegistration(getRegistry().registerMenu(CoreComponents.SHELL, null, f));
    }

    protected void registerDefaultGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(DemoFileMenu.DEFAULT, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponents.SHELL, DemoFileMenu.FILE, f));
    }

    protected void registerThemeItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoFileMenu.THEME, "_Theme", 1000);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                var appearance = shell.getPresenter().getSettings().getAppearance();
                var view = new ThemeDialogFxView();
                var presenter = new ThemeDialogPresenter(view, appearance);
                presenter.initialize();
                shell.getComposer().addDialog(view);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, DemoFileMenu.DEFAULT, f));
    }
}
