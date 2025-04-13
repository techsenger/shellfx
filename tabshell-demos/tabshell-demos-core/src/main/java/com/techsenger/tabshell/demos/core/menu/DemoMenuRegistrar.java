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

package com.techsenger.tabshell.demos.core.menu;

import com.techsenger.tabshell.core.TabShellKey;
import com.techsenger.tabshell.core.TabShellView;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demos.core.tab.DemoTabView;
import com.techsenger.tabshell.demos.core.tab.DemoTabViewModel;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuGroup;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Groups, menus, items are registered in any order.
 *
 * @author Pavel Castornii
 */
public class DemoMenuRegistrar extends AbstractControlRegistrar {

    public DemoMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        registerDemoMenu();
        registerGroupOne();
        registerGroupTwo();
        registerNewItem();
        registerExitItem();
    }

    protected void registerDemoMenu() {
        ControlFactory<KeyedMenu> f = (v) -> {
            return new KeyedMenu(DemoMenuKeys.DEMO, "_Demo", 100);
        };
        addRegistration(getRegistry().registerMenu(TabShellKey.INSTANCE, null, f));
    }

    protected void registerGroupOne() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(DemoMenuKeys.ONE, "One", 100);
        };
        addRegistration(getRegistry().registerMenuGroup(TabShellKey.INSTANCE, DemoMenuKeys.DEMO, f));
    }

    protected void registerGroupTwo() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(DemoMenuKeys.TWO, "Two", 200);
        };
        addRegistration(getRegistry().registerMenuGroup(TabShellKey.INSTANCE, DemoMenuKeys.DEMO, f));
    }

    /**
     * New item will be in the group one. It is not optional but is validatable.
     */
    protected void registerNewItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoMenuKeys.NEW, false, true, false, "_New", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var tabShellView  = (TabShellView<?>) v;
                var tabViewModel = new DemoTabViewModel(tabShellView.getViewModel());
                var tabView = new DemoTabView(tabShellView, tabViewModel);
                tabView.initialize();
                tabShellView.openTab(tabView);
            });
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, DemoMenuKeys.ONE, f));
    }

    /**
     * Exit item will be in the group two. It is optional and validatable.
     */
    protected void registerExitItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoMenuKeys.EXIT, true, true, false, "E_xit", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> ((TabShellView<?>) v).close());
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, DemoMenuKeys.TWO, f));
    }
}
