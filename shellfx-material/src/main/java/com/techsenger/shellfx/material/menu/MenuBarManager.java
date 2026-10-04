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

package com.techsenger.shellfx.material.menu;

import com.techsenger.annotations.Nullable;
import java.util.function.Supplier;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamically manages menu and menu items using information from current tab.
 *
 * <p>There are two types of menus - menuBar menus and nested menus (that are inside menuBar menus).
 * For all menus states are configured when they are shown on user action. At the same time, as MenuBar menus
 * are always visible their visibility is configured in two cases: when a focused component changed
 * (including no tab cases) and when updateMenuBar is invoked.
 *
 * <p>The menus are wired once, when the manager is created; the manager never changes the menu structure and is
 * discarded together with its menu bar.
 *
 * <p>Menu items are handled uniformly through {@link MenuItem}. This class has no knowledge of concrete item types
 * ({@code MenuItem}, {@code CheckMenuItem}, {@code RadioMenuItem}, etc.) — any item with a registered
 * {@link MenuItemHandler} is dispatched the same way.
 *
 * @author Pavel Castornii
 */
public class MenuBarManager {

    private static final Logger logger = LoggerFactory.getLogger(MenuBarManager.class);

    private final MenuBar menuBar;

    private final Supplier<@Nullable InputEvent> inputEvent;

    /**
     * Creates a manager of {@code menuBar} and wires action dispatch and visibility handling onto its menus right
     * away. The menu bar must be fully built; the manager is discarded together with it.
     *
     * @param menuBar     the menu bar to manage.
     * @param inputEvent supplies the input event the window of the menu bar is dispatching right now, which tells
     *     an accelerator from a mouse click.
     */
    public MenuBarManager(MenuBar menuBar, Supplier<@Nullable InputEvent> inputEvent) {
        this.menuBar = menuBar;
        this.inputEvent = inputEvent;
        for (var menu : this.menuBar.getMenus()) {
            this.initializeMenu(menu);
        }
    }

    /**
     * Refreshes the visibility of the menu bar menus without showing them; call it when the focused component or
     * its state changes. A menu with a handler is updated by it (an open one is hidden instead), the others derive
     * visibility from their items.
     */
    public void updateMenuBar() {
        for (var m : this.menuBar.getMenus()) {
            if (m instanceof Menu managedMenu) {
                var handler = MenuHandler.getHandler(managedMenu);
                if (handler != null) {
                    if (managedMenu.isShowing()) {
                        managedMenu.hide();
                    } else {
                        handler.onUpdate();
                    }
                } else {
                    // JavaFX checks each item's current isVisible() before opening a Menu's popup at all
                    // (MenuBarSkin#isMenuEmpty), and that check runs before onShowing fires. A menu without its
                    // own MenuHandler otherwise only recomputes item visibility inside onShowing - so once every
                    // item happens to be invisible, JavaFX stops calling show() on it, onShowing never fires
                    // again, and the items can never be recomputed: the menu is stuck empty forever. Updating
                    // here instead, on every focus/selection change, avoids that trap; MenuVisibility#update is
                    // used rather than #resolve since nothing is actually being shown here.
                    MenuVisibility.update(managedMenu);
                }
            }
        }
    }

    /**
     * Recursively initializes all menus.
     *
     * @param managedMenu
     */
    private void initializeMenu(Menu managedMenu) {
        managedMenu.setOnShowing((e) -> this.onMenuShowing(managedMenu));
        managedMenu.setOnHiding((e) -> this.onMenuHiding(managedMenu));
        //managedMenu.setOnAction();
        for (var m : managedMenu.getItems()) {
            if (m instanceof Menu menu) {
                this.initializeMenu(menu);
            } else {
                var handler = MenuItemHandler.getHandler(m);
                if (handler != null) {
                    m.setOnAction(e -> MenuItemDispatcher.dispatch(m, handler, inputEvent));
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Menu {} initialized", getMenuText(managedMenu));
        }
    }

    private void onMenuShowing(Menu menu) {
        MenuVisibility.resolveItems(menu.getItems());
        MenuVisibility.collapseSeparators(menu.getItems());
    }

    private void onMenuHiding(Menu menu) {
        MenuVisibility.fireHiding(menu.getItems());
    }

    private String getMenuText(MenuItem keyedMenu) {
        return keyedMenu.getText().replace("_", "");
    }
}
