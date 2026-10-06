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
import com.techsenger.shellfx.core.registry.SimpleControlProvider;
import com.techsenger.shellfx.core.registry.SimpleGroupProvider;
import com.techsenger.shellfx.core.window.WindowArrangement;
import com.techsenger.shellfx.demo.ApplicationType;
import com.techsenger.shellfx.demo.main.DemoMenuAwarePort;
import com.techsenger.shellfx.demo.page.PageMenuType;
import com.techsenger.shellfx.material.menu.DynamicMenu;
import java.util.function.Function;
import javafx.beans.value.ObservableValue;
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

    /**
     * Returns the value of a property of the current port if it is a demo port; the result must be unbound by the
     * control that binds to it in {@code deinitialize}.
     */
    private static ObservableValue<Boolean> observePort(ShellView<?> view,
            Function<DemoMenuAwarePort, ObservableValue<Boolean>> property) {
        return view.getComposer().menuAwarePortProperty()
                .flatMap(port -> port instanceof DemoMenuAwarePort demoPort ? property.apply(demoPort) : null);
    }

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
        register(ShellSlots.FileMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_File")));
        register(ShellSlots.FileMenu.DEMO_GROUP, SimpleGroupProvider::new);
        register(ShellSlots.FileMenu.APPEARANCE_GROUP, SimpleGroupProvider::new);
        register(ShellSlots.FileMenu.LAST_GROUP, SimpleGroupProvider::new);
    }

    protected void registerMainTabItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 100, () -> new SimpleControlProvider<>(new MenuItem("Main Tab")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(new MainTabItemHandler(shell));
            }
        });
    }

    protected void registerPageTabItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 200, () -> new SimpleControlProvider<>(new MenuItem("Page Tab")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(new PageItemHandler(shell, PageMenuType.FLAT));
            }
        });
    }

    protected void registerTreePageTabItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 250, () -> new SimpleControlProvider<>(new MenuItem("Tree Page Tab")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(new PageItemHandler(shell, PageMenuType.TREE));
            }
        });
    }

    protected void registerDialogsItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 300, () -> new SimpleControlProvider<>(new MenuItem("Dialogs")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(new DialogsItemHandler(shell));
            }
        });
    }

    protected void registerDevToolsItem() {
        register(ShellSlots.FileMenu.DEMO_GROUP, 400, () -> new SimpleControlProvider<>(new MenuItem("DevTools")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(new DevToolsItemHandler(shell));
            }
        });
    }

    protected void registerMainMenu() {
        register(ShellSlots.MAIN_MENU, () -> new SimpleControlProvider<>(new MenuBar()));
    }

    protected void registerSettingsItem() {
        register(ShellSlots.FileMenu.APPEARANCE_GROUP, 100,
                () -> new SimpleControlProvider<>(new MenuItem("_Settings")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(new SettingsItemHandler(shell));
            }
        });
    }

    protected void registerExitItem() {
        register(ShellSlots.FileMenu.LAST_GROUP, 100, () -> new SimpleControlProvider<>(new MenuItem("E_xit")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
                getControl().setOnAction(new ExitItemHandler(shell));
            }
        });
    }

    protected void registerExtraMenu() {
        // the extra menu is shown while a demo component forms the menu and at least one of its items is visible
        register(ShellSlots.ExtraMenu.MENU, () -> new SimpleControlProvider<ShellView<?>, DynamicMenu>(
                new DynamicMenu("_Extra")) {

            private ObservableValue<Boolean> demoPort;

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                demoPort = v.getComposer().menuAwarePortProperty().map(port -> port instanceof DemoMenuAwarePort);
                getControl().addVisibleCondition(demoPort);
            }

            @Override
            public void deinitialize(ShellView<?> v) {
                super.deinitialize(v);
                getControl().removeVisibleCondition(demoPort);
            }
        });
        register(ShellSlots.ExtraMenu.FOO_GROUP, SimpleGroupProvider::new);
        register(ShellSlots.ExtraMenu.BAR_GROUP, SimpleGroupProvider::new);
    }

    /**
     * Foo item will be in the foo group.
     */
    protected void registerFooItem() {
        register(ShellSlots.ExtraMenu.FOO_GROUP, 100, () -> new SimpleControlProvider<>(new MenuItem("_Foo")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
                getControl().disableProperty().bind(observePort(v, DemoMenuAwarePort::fooDisabledProperty));
                getControl().setOnAction(e -> System.out.println("Foo Item"));
            }

            @Override
            public void deinitialize(ShellView<?> v) {
                super.deinitialize(v);
                getControl().disableProperty().unbind();
            }
        });
    }

    /**
     * Bar item will be in the bar group.
     */
    protected void registerBarItem() {
        register(ShellSlots.ExtraMenu.BAR_GROUP, 100, () -> new SimpleControlProvider<>(new MenuItem("_Bar")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
                getControl().visibleProperty().bind(observePort(v, DemoMenuAwarePort::barIncludedProperty));
                getControl().disableProperty().bind(observePort(v, DemoMenuAwarePort::barDisabledProperty));
                getControl().setOnAction(e -> System.out.println("Bar Item"));
            }

            @Override
            public void deinitialize(ShellView<?> v) {
                super.deinitialize(v);
                getControl().visibleProperty().unbind();
                getControl().disableProperty().unbind();
            }
        });
    }

    protected void registerWindowMenu() {
        register(ShellSlots.WindowMenu.MENU, () -> new SimpleControlProvider<>(new DynamicMenu("_Window")));
        register(ShellSlots.WindowMenu.DEFAULT_GROUP, SimpleGroupProvider::new);
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, SimpleGroupProvider::new);
    }

    protected void registerWindowsItem() {
        register(ShellSlots.WindowMenu.DEFAULT_GROUP, 0,
                () -> new SimpleControlProvider<>(new MenuItem("Create Windows")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(new WindowsItemHandler(shell));
            }
        });
    }

    protected void registerCascadeItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 0,
                () -> new SimpleControlProvider<>(new MenuItem("Cascade")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(e -> shell.getComposer().arrangeWindows(WindowArrangement.CASCADE));
            }
        });
    }

    protected void registerTileVerticalItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 100,
                () -> new SimpleControlProvider<>(new MenuItem("Tile Vertically")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(e -> shell.getComposer().arrangeWindows(WindowArrangement.TILE_VERTICAL));
            }
        });
    }

    protected void registerTileHorizontalItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 200,
                () -> new SimpleControlProvider<>(new MenuItem("Tile Horizontally")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(e -> shell.getComposer().arrangeWindows(WindowArrangement.TILE_HORIZONTAL));
            }
        });
    }

    protected void registerTileGridItem() {
        register(ShellSlots.WindowMenu.ARRANGEMENT_GROUP, 300,
                () -> new SimpleControlProvider<>(new MenuItem("Tile Grid")) {

            @Override
            public void initialize(ShellView<?> v) {
                super.initialize(v);
                getControl().setOnAction(e -> shell.getComposer().arrangeWindows(WindowArrangement.TILE_GRID));
            }
        });
    }
}
