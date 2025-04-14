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

package com.techsenger.tabshell.registrars;

import com.techsenger.tabshell.core.ShellKey;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.menu.FileMenuKeys;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuGroup;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
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
        ControlFactory<KeyedMenu> f = (v) -> {
            return new KeyedMenu(FileMenuKeys.FILE, "_File", 100);
        };
        addRegistration(getRegistry().registerMenu(ShellKey.INSTANCE, null, f));
    }

    protected void registerFileActionsGroup() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(FileMenuKeys.FILE_ACTIONS, "File Actions", 100);
        };
        addRegistration(getRegistry().registerMenuGroup(ShellKey.INSTANCE, FileMenuKeys.FILE, f));
    }

    protected void registerDefaultGroup() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(FileMenuKeys.DEFAULT, "Default", 10000);
        };
        addRegistration(getRegistry().registerMenuGroup(ShellKey.INSTANCE, FileMenuKeys.FILE, f));
    }

    protected void registerOpenFileItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var shellView = (ShellView<?>) v;
            var item = new KeyedMenuItem(FileMenuKeys.OPEN, true, true, false, "_Open",
                    new FontIconView(CoreIcons.OPEN), 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var view = shellView.getSelectedTab();
                var viewModel = view.getViewModel();
                var helper = viewModel.getMenuItemHelper(item.getKey());
                helper.doOnItemAction();
            });
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerSaveFileItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var shellView = (ShellView<?>) v;
            var item = new KeyedMenuItem(FileMenuKeys.SAVE, true, true, false, "_Save",
                    new FontIconView(CoreIcons.SAVE), 200);
            item.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var view = shellView.getSelectedTab();
                var viewModel = view.getViewModel();
                var helper = viewModel.getMenuItemHelper(item.getKey());
                helper.doOnItemAction();

            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerSaveFileAsItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var shellView = (ShellView<?>) v;
            var item = new KeyedMenuItem(FileMenuKeys.SAVE_AS, true, true, false, "Sa_ve As",
                    new FontIconView(CoreIcons.SAVE_AS), 300);
            item.setOnAction((e) -> {
                var view = shellView.getSelectedTab();
                var viewModel = view.getViewModel();
                var helper = viewModel.getMenuItemHelper(item.getKey());
                helper.doOnItemAction();
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerExitItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var shellView = (ShellView<?>) v;
            var item = new KeyedMenuItem(FileMenuKeys.EXIT, "E_xit", new FontIconView(CoreIcons.EXIT), 10000);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> ((ShellView<?>) v).close());
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.DEFAULT, f));
    }
}
