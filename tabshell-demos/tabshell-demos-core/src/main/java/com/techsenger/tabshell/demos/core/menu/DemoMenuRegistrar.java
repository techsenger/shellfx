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

package com.techsenger.tabshell.demos.core.menu;

import com.techsenger.tabshell.core.CoreComponentNames;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demos.core.tab.DemoTabComponent;
import com.techsenger.tabshell.demos.core.tab.DemoTabView;
import com.techsenger.tabshell.demos.core.tab.DemoTabViewModel;
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
        ControlFactory<NamedMenu> f = (v) -> {
            return new NamedMenu(DemoMenuNames.DEMO, "_Demo", 100);
        };
        addRegistration(getRegistry().registerMenu(CoreComponentNames.SHELL, null, f));
    }

    protected void registerGroupOne() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(DemoMenuNames.ONE, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, DemoMenuNames.DEMO, f));
    }

    protected void registerGroupTwo() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(DemoMenuNames.TWO, 200);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, DemoMenuNames.DEMO, f));
    }

    /**
     * New item will be in the group one. It is not optional but is validatable.
     */
    protected void registerNewItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.NEW, false, true, false, "_New", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var shellView  = (ShellView<?, ?>) v;
                var shellComponent = shellView.getComponent();
                var tabViewModel = new DemoTabViewModel();
                var tabView = new DemoTabView(tabViewModel);
                var tabComponent = new DemoTabComponent(tabView, shellComponent);
                tabComponent.initialize();
                shellComponent.addTab(tabComponent);
            });
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.ONE, f));
    }

    /**
     * Exit item will be in the group two. It is optional and validatable.
     */
    protected void registerExitItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.EXIT, true, true, false, "E_xit", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> ((ShellView<?, ?>) v).getViewModel().close());
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.TWO, f));
    }
}
