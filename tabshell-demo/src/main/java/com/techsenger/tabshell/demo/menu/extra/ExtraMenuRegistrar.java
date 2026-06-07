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

package com.techsenger.tabshell.demo.menu.extra;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.menu.MenuHandler;
import com.techsenger.tabshell.core.menu.MenuItemHandler;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.material.menu.ManagedMenu;
import com.techsenger.tabshell.material.menu.ManagedMenuGroup;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Groups, menus, items are registered in any order.
 *
 * @author Pavel Castornii
 */
public class ExtraMenuRegistrar extends AbstractControlRegistrar {

    public ExtraMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        registerExtraMenu();
        registerFooGroup();
        registerBarGroup();
        registerFooItem();
        registerBarItem();
    }

    protected void registerExtraMenu() {
        ControlFactory<ShellFxView<?>, ManagedMenu> f = (v) -> {
            var menu = new ManagedMenu(ExtraMenu.NAME, "_Extra", 200);
            MenuHandler.setHandler(menu, new ExtraMenuHandler(menu, v));
            return menu;
        };
        addRegistration(getRegistry().mainMenu().registerMenu(f));
    }

    protected void registerFooGroup() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(ExtraMenu.FOO_GROUP, 0);
        };
        addRegistration(getRegistry().mainMenu().registerMenuGroup(ExtraMenu.NAME, f));
    }

    protected void registerBarGroup() {
        ControlFactory<ShellFxView<?>, ManagedMenuGroup> f = (v) -> {
            return new ManagedMenuGroup(ExtraMenu.BAR_GROUP, 100);
        };
        addRegistration(getRegistry().mainMenu().registerMenuGroup(ExtraMenu.NAME, f));
    }

    /**
     * Foo item will be in the foo group.
     */
    protected void registerFooItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("_Foo", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new FooItemHandler(item, v));
            return item;

        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(ExtraMenu.FOO_GROUP, f));
    }

    /**
     * Bar item will be in the bar group.
     */
    protected void registerBarItem() {
        ControlFactory<ShellFxView<?>, ManagedMenuItem> f = (v) -> {
            var item = new ManagedMenuItem("_Bar", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
            MenuItemHandler.setHandler(item, new BarItemHandler(item, v));
            return item;
        };
        addRegistration(getRegistry().mainMenu().registerMenuItem(ExtraMenu.BAR_GROUP, f));
    }
}
