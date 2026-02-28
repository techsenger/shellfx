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

package com.techsenger.tabshell.demo.menu;

import com.techsenger.tabshell.core.CoreComponents;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
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
        ControlFactory<NamedMenu> f = (v) -> {
            // this menu is optional!
            return new NamedMenu(ExtraMenu.NAME, true, false, false, "_Extra", 100);
        };
        addRegistration(getRegistry().registerMenu(CoreComponents.SHELL, null, f));
    }

    protected void registerFooGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(ExtraMenu.FOO_GROUP, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponents.SHELL, ExtraMenu.NAME, f));
    }

    protected void registerBarGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(ExtraMenu.BAR_GROUP, 200);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponents.SHELL, ExtraMenu.NAME, f));
    }

    /**
     * Foo item will be in the foo group. It is not optional but is validatable.
     */
    protected void registerFooItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(ExtraMenu.FOO_ITEM, false, true, false, "_Foo", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> System.out.println("Foo Item"));
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, ExtraMenu.FOO_GROUP, f));
    }

    /**
     * Bar item will be in the bar group. It is optional and validatable.
     */
    protected void registerBarItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(ExtraMenu.BAR_ITEM, true, true, false, "_Bar", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> System.out.println("Bar Item"));
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, ExtraMenu.BAR_GROUP, f));
    }
}
