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

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.registry.AbstractControlRegistrar;
import com.techsenger.shellfx.core.registry.ControlFactory;
import com.techsenger.shellfx.core.window.WindowArrangement;
import com.techsenger.shellfx.demo.ApplicationType;
import com.techsenger.shellfx.demo.ShellControls;
import com.techsenger.shellfx.demo.page.PageMenuType;
import com.techsenger.shellfx.material.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.material.menu.ManagedMenu;
import com.techsenger.shellfx.material.menu.ManagedMenuGroup;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;
import com.techsenger.shellfx.material.menu.MenuHandler;
import com.techsenger.shellfx.material.menu.MenuItemHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Registers every menu, group, and item the demo application contributes, gated by {@link ApplicationType}.
 *
 * @author Pavel Castornii
 */
public class ModuleControlRegistrar extends AbstractControlRegistrar {

    private final ApplicationType appType;

    private final ShellFxView<?> shell;

    public ModuleControlRegistrar(ApplicationType appType, ShellFxView<?> shell) {
        super(shell.getControlRegistry());

        this.appType = appType;
        this.shell = shell;
    }

    @Override
    public void register() {
        if (appType != ApplicationType.STYLES_ONLY) {
            registerFileMenu();
            registerFileGroups();
            if (appType != ApplicationType.MDI) {
                registerMainTabItem();
                registerPageTabItem();
                registerTreePageTabItem();
            }
            registerDialogsItem();
            registerDevToolsItem();
            registerSettingsItem();
            registerExitItem();
        }
        if (appType == ApplicationType.BROWSER || appType == ApplicationType.IDE) {
            registerExtraMenu();
            registerFooGroup();
            registerBarGroup();
            registerFooItem();
            registerBarItem();
        }
        if (appType == ApplicationType.MDI) {
            registerWindowMenu();
            registerWindowGroups();
            registerWindowsItem();
            registerCascadeItem();
            registerTileVerticalItem();
            registerTileHorizontalItem();
            registerTileGridItem();
        }
    }

    protected void registerFileMenu() {
        ControlFactory<ShellFxView<?>, ManagedMenu> f = (v) -> {
            var menu = new ManagedMenu(ShellControls.FileMenu.NAME, "_File", 0);
            return menu;
        };
        addRegistration(getRegistry().registerMenu(ShellControls.MAIN_MENU_GROUP, f));
    }

    protected void registerFileGroups() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(ShellControls.FileMenu.DEMO_GROUP, 0);
        };
        addRegistration(getRegistry().registerMenuGroup(ShellControls.FileMenu.NAME, f));
        f = (v) -> new ManagedMenuGroup(ShellControls.FileMenu.APPEARANCE_GROUP, 100);
        addRegistration(getRegistry().registerMenuGroup(ShellControls.FileMenu.NAME, f));
        f = (v) -> new ManagedMenuGroup(ShellControls.FileMenu.LAST_GROUP, 200);
        addRegistration(getRegistry().registerMenuGroup(ShellControls.FileMenu.NAME, f));
    }

    protected void registerMainTabItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Main Tab", 100);
            MenuItemHandler.setHandler(item, new MainTabItemHandler(shell, item));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.FileMenu.DEMO_GROUP, f));
    }

    protected void registerPageTabItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Page Tab", 200);
            MenuItemHandler.setHandler(item, new PageItemHandler(shell, item, PageMenuType.FLAT));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.FileMenu.DEMO_GROUP, f));
    }

    protected void registerTreePageTabItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Tree Page Tab", 250);
            MenuItemHandler.setHandler(item, new PageItemHandler(shell, item, PageMenuType.TREE));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.FileMenu.DEMO_GROUP, f));
    }

    protected void registerDialogsItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Dialogs", 300);
            MenuItemHandler.setHandler(item, new DialogsItemHandler(shell, item));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.FileMenu.DEMO_GROUP, f));
    }

    protected void registerDevToolsItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("DevTools", 400);
            MenuItemHandler.setHandler(item, new DevToolsItemHandler(shell, item));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.FileMenu.DEMO_GROUP, f));
    }

    protected void registerSettingsItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("_Settings", 100);
            MenuItemHandler.setHandler(item, new SettingsItemHandler(shell, item));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.FileMenu.APPEARANCE_GROUP, f));
    }

    protected void registerExitItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("E_xit", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new ExitItemHandler(shell, item));
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.FileMenu.LAST_GROUP, f));
    }

    protected void registerExtraMenu() {
        ControlFactory<ShellFxView<?>, ManagedMenu> f = (v) -> {
            var menu = new ManagedMenu(ShellControls.ExtraMenu.NAME, "_Extra", 200);
            MenuHandler.setHandler(menu, new ExtraMenuHandler(menu, v));
            return menu;
        };
        addRegistration(getRegistry().registerMenu(ShellControls.MAIN_MENU_GROUP, f));
    }

    protected void registerFooGroup() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(ShellControls.ExtraMenu.FOO_GROUP, 0);
        };
        addRegistration(getRegistry().registerMenuGroup(ShellControls.ExtraMenu.NAME, f));
    }

    protected void registerBarGroup() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(ShellControls.ExtraMenu.BAR_GROUP, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(ShellControls.ExtraMenu.NAME, f));
    }

    /**
     * Foo item will be in the foo group.
     */
    protected void registerFooItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("_Foo", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new FooItemHandler(v, item));
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.ExtraMenu.FOO_GROUP, f));
    }

    /**
     * Bar item will be in the bar group.
     */
    protected void registerBarItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("_Bar", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new BarItemHandler(v, item));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.ExtraMenu.BAR_GROUP, f));
    }

    protected void registerWindowMenu() {
        ControlFactory<ShellFxView<?>, ManagedMenu> f = (v) -> {
            var menu = new ManagedMenu(ShellControls.WindowMenu.NAME, "_Window", 100);
            return menu;
        };
        addRegistration(getRegistry().registerMenu(ShellControls.MAIN_MENU_GROUP, f));
    }

    protected void registerWindowGroups() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(ShellControls.WindowMenu.DEFAULT_GROUP, 0);
        };
        addRegistration(getRegistry().registerMenuGroup(ShellControls.WindowMenu.NAME, f));
        f = (v) -> new ManagedMenuGroup(ShellControls.WindowMenu.ARRANGEMENT_GROUP, 100);
        addRegistration(getRegistry().registerMenuGroup(ShellControls.WindowMenu.NAME, f));
    }

    protected void registerWindowsItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Create Windows", 0);
            MenuItemHandler.setHandler(item, new WindowsItemHandler(shell, item));
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.WindowMenu.DEFAULT_GROUP, f));
    }

    protected void registerCascadeItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Cascade", 0);
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellFxView<?>, ManagedMenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.CASCADE);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.WindowMenu.ARRANGEMENT_GROUP, f));
    }

    protected void registerTileVerticalItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Tile Vertically", 100);
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellFxView<?>, ManagedMenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.TILE_VERTICAL);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.WindowMenu.ARRANGEMENT_GROUP, f));
    }

    protected void registerTileHorizontalItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Tile Horizontally", 200);
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellFxView<?>, ManagedMenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.TILE_HORIZONTAL);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.WindowMenu.ARRANGEMENT_GROUP, f));
    }

    protected void registerTileGridItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Tile Grid", 300);
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellFxView<?>, ManagedMenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.TILE_GRID);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellControls.WindowMenu.ARRANGEMENT_GROUP, f));
    }
}
