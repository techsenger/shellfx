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
import com.techsenger.tabshell.core.menu.MenuItemHandler;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demo.WorkspaceType;
import com.techsenger.tabshell.demo.page.PageMenuType;
import com.techsenger.tabshell.material.menu.ManagedMenu;
import com.techsenger.tabshell.material.menu.ManagedMenuGroup;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author Pavel Castornii
 */
public class FileMenuRegistrar extends AbstractControlRegistrar {

    private final WorkspaceType workspaceType;

    private final ShellFxView<?> shell;

    public FileMenuRegistrar(ControlRegistry registry, WorkspaceType workspaceType, ShellFxView<?> shell) {
        super(registry);
        this.workspaceType = workspaceType;
        this.shell = shell;
    }

    @Override
    public void register() {
        registerMenu();
        registerGroups();
        if (workspaceType != WorkspaceType.MDI) {
            registerMainTabItem();
            registerPageTabItem();
            registerTreePageTabItem();
        }
        registerDialogsItem();
        registerDevToolsItem();
        registerThemeItem();
        registerStylesTabItem();
        registerExitItem();
    }

    protected void registerMenu() {
        ControlFactory<ShellFxView<?>, ManagedMenu> f = (v) -> {
            var menu = new ManagedMenu(FileMenu.NAME, "_File", 0);
            return menu;
        };
        addRegistration(getRegistry().mainMenu().registerMenu(f));
    }

    protected void registerGroups() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(FileMenu.DEMO_GROUP, 0);
        };
        addRegistration(getRegistry().mainMenu().registerMenuGroup(FileMenu.NAME, f));
        f = (v) -> new ManagedMenuGroup(FileMenu.APPEARANCE_GROUP, 100);
        addRegistration(getRegistry().mainMenu().registerMenuGroup(FileMenu.NAME, f));
        f = (v) -> new ManagedMenuGroup(FileMenu.LAST_GROUP, 200);
        addRegistration(getRegistry().mainMenu().registerMenuGroup(FileMenu.NAME, f));
    }

    protected void registerMainTabItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Main Tab", 100);
            MenuItemHandler.setHandler(item, new MainTabItemHandler(item, shell));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.DEMO_GROUP, f));
    }

    protected void registerPageTabItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Page Tab", 200);
            MenuItemHandler.setHandler(item, new PageItemHandler(item, shell, PageMenuType.FLAT));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.DEMO_GROUP, f));
    }

    protected void registerTreePageTabItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Tree Page Tab", 250);
            MenuItemHandler.setHandler(item, new PageItemHandler(item, shell, PageMenuType.TREE));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.DEMO_GROUP, f));
    }

    protected void registerDialogsItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Dialogs", 300);
            MenuItemHandler.setHandler(item, new DialogsItemHandler(item, shell));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.DEMO_GROUP, f));
    }

    protected void registerDevToolsItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("DevTools", 400);
            MenuItemHandler.setHandler(item, new DevToolsItemHandler(item, shell));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.DEMO_GROUP, f));
    }

    protected void registerThemeItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("_Theme", 100);
            MenuItemHandler.setHandler(item, new ThemeItemHandler(item, shell));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.APPEARANCE_GROUP, f));
    }

    protected void registerStylesTabItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Styles Tab", 200);
            MenuItemHandler.setHandler(item, new StylesItemHandler(item, shell));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.APPEARANCE_GROUP, f));
    }

    protected void registerExitItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("E_xit", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new ExitItemHandler(item, shell));
            return item;

        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(FileMenu.LAST_GROUP, f));
    }
}
