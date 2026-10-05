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

package com.techsenger.shellfx.demo.shell;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.registry.AbstractControlRegistrar;
import com.techsenger.shellfx.core.window.WindowArrangement;
import com.techsenger.shellfx.demo.ApplicationType;
import com.techsenger.shellfx.demo.page.PageMenuType;
import com.techsenger.shellfx.material.ControlGroup;
import com.techsenger.shellfx.material.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.material.menu.MenuHandler;
import com.techsenger.shellfx.material.menu.MenuItemHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Registers the control of every menu and item the demo application contributes, gated by
 * {@link ApplicationType}; where the menus and groups sit is set by {@link ShellSlotRegistrar}.
 *
 * @author Pavel Castornii
 */
public class ShellControlRegistrar extends AbstractControlRegistrar {

    private final ApplicationType appType;

    private final ShellView<?> shell;

    public ShellControlRegistrar(ApplicationType appType, ShellView<?> shell) {
        super(shell.getContext().getControlRegistry());

        this.appType = appType;
        this.shell = shell;
    }

    @Override
    public void register() {
        registerMainMenu();
        if (appType != ApplicationType.STYLES_ONLY) {
            registerFileMenu();
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
            registerFooItem();
            registerBarItem();
        }
        if (appType == ApplicationType.MDI) {
            registerWindowMenu();
            registerWindowsItem();
            registerCascadeItem();
            registerTileVerticalItem();
            registerTileHorizontalItem();
            registerTileGridItem();
        }
    }

    protected void registerFileMenu() {
        register(ShellSlots.FileMenu.MENU, v -> new Menu("_File"));
        register(ShellSlots.FileMenu.DEMO_GROUP, v -> new ControlGroup<>());
        register(ShellSlots.FileMenu.APPEARANCE_GROUP, v -> new ControlGroup<>());
        register(ShellSlots.FileMenu.LAST_GROUP, v -> new ControlGroup<>());
    }

    protected void registerMainTabItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 100, v -> {
            var item = new MenuItem("Main Tab");
            MenuItemHandler.setHandler(item, new MainTabItemHandler(shell, item));
            return item;
        });
    }

    protected void registerPageTabItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 200, v -> {
            var item = new MenuItem("Page Tab");
            MenuItemHandler.setHandler(item, new PageItemHandler(shell, item, PageMenuType.FLAT));
            return item;
        });
    }

    protected void registerTreePageTabItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 250, v -> {
            var item = new MenuItem("Tree Page Tab");
            MenuItemHandler.setHandler(item, new PageItemHandler(shell, item, PageMenuType.TREE));
            return item;
        });
    }

    protected void registerDialogsItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 300, v -> {
            var item = new MenuItem("Dialogs");
            MenuItemHandler.setHandler(item, new DialogsItemHandler(shell, item));
            return item;
        });
    }

    protected void registerDevToolsItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 400, v -> {
            var item = new MenuItem("DevTools");
            MenuItemHandler.setHandler(item, new DevToolsItemHandler(shell, item));
            return item;
        });
    }

    protected void registerMainMenu() {
        register(ShellSlots.MAIN_MENU, v -> new MenuBar());
    }

    protected void registerSettingsItem() {
        register(ShellSlots.FileMenu.APPEARANCE_GROUP, 100, v -> {
            var item = new MenuItem("_Settings");
            MenuItemHandler.setHandler(item, new SettingsItemHandler(shell, item));
            return item;
        });
    }

    protected void registerExitItem() {
        register(ShellSlots.FileMenu.LAST_GROUP, 100, v -> {
            var item = new MenuItem("E_xit");
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new ExitItemHandler(shell, item));
            return item;
        });
    }

    protected void registerExtraMenu() {
        register(ShellSlots.ExtraMenu.MENU, v -> {
            var menu = new Menu("_Extra");
            MenuHandler.setHandler(menu, new ExtraMenuHandler(menu, v));
            return menu;
        });
        register(ShellSlots.ExtraMenu.FOO_GROUP, v -> new ControlGroup<>());
        register(ShellSlots.ExtraMenu.BAR_GROUP, v -> new ControlGroup<>());
    }

    /**
     * Foo item will be in the foo group.
     */
    protected void registerFooItem() {
        register(ShellSlots.ExtraMenu.FOO_GROUP, 100, v -> {
            var item = new MenuItem("_Foo");
            item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new FooItemHandler(v, item));
            return item;
        });
    }

    /**
     * Bar item will be in the bar group.
     */
    protected void registerBarItem() {
        register(ShellSlots.ExtraMenu.BAR_GROUP, 100, v -> {
            var item = new MenuItem("_Bar");
            item.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new BarItemHandler(v, item));
            return item;
        });
    }

    protected void registerWindowMenu() {
        register(ShellSlots.WindowMenu.MENU, v -> new Menu("_Window"));
        register(ShellSlots.WindowMenu.DEFAULT_GROUP, v -> new ControlGroup<>());
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, v -> new ControlGroup<>());
    }

    protected void registerWindowsItem() {
        register(ShellSlots.WindowMenu.DEFAULT_GROUP, 0, v -> {
            var item = new MenuItem("Create Windows");
            MenuItemHandler.setHandler(item, new WindowsItemHandler(shell, item));
            return item;
        });
    }

    protected void registerCascadeItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 0, v -> {
            var item = new MenuItem("Cascade");
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellView<?>, MenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.CASCADE);
                }
            });
            return item;
        });
    }

    protected void registerTileVerticalItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 100, v -> {
            var item = new MenuItem("Tile Vertically");
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellView<?>, MenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.TILE_VERTICAL);
                }
            });
            return item;
        });
    }

    protected void registerTileHorizontalItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 200, v -> {
            var item = new MenuItem("Tile Horizontally");
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellView<?>, MenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.TILE_HORIZONTAL);
                }
            });
            return item;
        });
    }

    protected void registerTileGridItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 300, v -> {
            var item = new MenuItem("Tile Grid");
            MenuItemHandler.setHandler(item, new AbstractMenuItemHandler<ShellView<?>, MenuItem>(shell, item) {
                @Override
                public void onAction() {
                    shell.getComposer().arrangeWindows(WindowArrangement.TILE_GRID);
                }
            });
            return item;
        });
    }
}
