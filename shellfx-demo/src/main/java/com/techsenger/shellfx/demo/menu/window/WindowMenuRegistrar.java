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

package com.techsenger.shellfx.demo.menu.window;

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.core.menu.MenuItemHandler;
import com.techsenger.shellfx.core.registry.AbstractControlRegistrar;
import com.techsenger.shellfx.core.registry.ControlFactory;
import com.techsenger.shellfx.core.registry.ControlRegistry;
import com.techsenger.shellfx.core.window.WindowArrangement;
import com.techsenger.shellfx.material.menu.ManagedMenu;
import com.techsenger.shellfx.material.menu.ManagedMenuGroup;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class WindowMenuRegistrar extends AbstractControlRegistrar {

    private final ShellFxView<?> shell;

    public WindowMenuRegistrar(ControlRegistry registry, ShellFxView<?> shell) {
        super(registry);
        this.shell = shell;
    }

    @Override
    public void register() {
        registerMenu();
        registerGroups();
        registerWindowsItem();
        registerCascadeItem();
        registerTileVerticalItem();
        registerTileHorizontalItem();
        registerTileGridItem();
    }

    protected void registerMenu() {
        ControlFactory<ShellFxView<?>, ManagedMenu> f = (v) -> {
            var menu = new ManagedMenu(WindowMenu.NAME, "_Window", 100);
            return menu;
        };
        addRegistration(getRegistry().mainMenu().registerMenu(f));
    }

    protected void registerGroups() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(WindowMenu.DEFAULT_GROUP, 0);
        };
        addRegistration(getRegistry().mainMenu().registerMenuGroup(WindowMenu.NAME, f));
        f = (v) -> new ManagedMenuGroup(WindowMenu.ARRANGE_GROUP, 100);
        addRegistration(getRegistry().mainMenu().registerMenuGroup(WindowMenu.NAME, f));
    }

    protected void registerWindowsItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("Create Windows", 0);
            MenuItemHandler.setHandler(item, new WindowsItemHandler(shell, item));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(WindowMenu.DEFAULT_GROUP, f));
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
        addRegistration(getRegistry().mainMenu().registerMenuItem(WindowMenu.ARRANGE_GROUP, f));
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
        addRegistration(getRegistry().mainMenu().registerMenuItem(WindowMenu.ARRANGE_GROUP, f));
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
        addRegistration(getRegistry().mainMenu().registerMenuItem(WindowMenu.ARRANGE_GROUP, f));
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
        addRegistration(getRegistry().mainMenu().registerMenuItem(WindowMenu.ARRANGE_GROUP, f));
    }
}
