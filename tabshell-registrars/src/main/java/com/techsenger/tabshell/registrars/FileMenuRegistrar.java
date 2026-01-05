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

package com.techsenger.tabshell.registrars;

import com.techsenger.tabshell.core.CoreComponentNames;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
import com.techsenger.tabshell.shared.menu.FileMenuNames;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author Pavel Castornii
 */
public class FileMenuRegistrar extends AbstractControlRegistrar {

    public FileMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        registerFileMenu();
        registerDefaultGroup();
        registerFileActionsGroup();
        registerOpenFileItem();
        registerSaveFileItem();
        registerSaveFileAsItem();
        registerExitItem();
    }

    protected void registerFileMenu() {
        ControlFactory<NamedMenu> f = (v) -> {
            return new NamedMenu(FileMenuNames.FILE, "_File", 100);
        };
        addRegistration(getRegistry().registerMenu(CoreComponentNames.SHELL, null, f));
    }

    protected void registerFileActionsGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(FileMenuNames.FILE_ACTIONS, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, FileMenuNames.FILE, f));
    }

    protected void registerDefaultGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(FileMenuNames.DEFAULT, 10000);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, FileMenuNames.FILE, f));
    }

    protected void registerOpenFileItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var shellView = (ShellView<?, ?>) v;
            var item = new NamedMenuItem(FileMenuNames.OPEN, true, true, false, "_Open",
                    new FontIconView(SharedIcons.OPEN), 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var view = shellView.getSelectedTab();
                var helper = view.getMenuItemHelper(item.getName());
                helper.doOnItemAction();
            });
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, FileMenuNames.FILE_ACTIONS, f));
    }

    protected void registerSaveFileItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var shellView = (ShellView<?, ?>) v;
            var item = new NamedMenuItem(FileMenuNames.SAVE, true, true, false, "_Save",
                    new FontIconView(SharedIcons.SAVE), 200);
            item.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var view = shellView.getSelectedTab();
                var helper = view.getMenuItemHelper(item.getName());
                helper.doOnItemAction();

            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, FileMenuNames.FILE_ACTIONS, f));
    }

    protected void registerSaveFileAsItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var shellView = (ShellView<?, ?>) v;
            var item = new NamedMenuItem(FileMenuNames.SAVE_AS, true, true, false, "Sa_ve As",
                    new FontIconView(SharedIcons.SAVE_AS), 300);
            item.setOnAction((e) -> {
                var view = shellView.getSelectedTab();
                var helper = view.getMenuItemHelper(item.getName());
                helper.doOnItemAction();
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, FileMenuNames.FILE_ACTIONS, f));
    }

    protected void registerExitItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var shellView = (ShellView<?, ?>) v;
            var item = new NamedMenuItem(FileMenuNames.EXIT, "E_xit", new FontIconView(SharedIcons.EXIT), 10000);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> ((ShellView<?, ?>) v).getViewModel().requestClose());
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, FileMenuNames.DEFAULT, f));
    }
}
