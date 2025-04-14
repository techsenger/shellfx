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

package com.techsenger.tabshell.core.menu.manager;

import com.techsenger.tabshell.core.DefaultShellView;
import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import com.techsenger.tabshell.material.menu.KeyedMenuItemUpdate;
import com.techsenger.tabshell.material.menu.KeyedMenuUpdate;
import com.techsenger.tabshell.material.menu.MenuKey;
import com.techsenger.toolkit.core.StringUtils;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamically manages menu and menu items using information from current tab.
 *
 * <p>There are two types of menus - menuBar menus and nested menus (that are inside menuBar menus).
 * For all menus states are configured when they are shown on user action. At the same time, as MenuBar menus
 * are always visible their visibility is configured in two cases: when tab is changed (including no tab cases)
 * and when updateMenuBar is invoked.
 *
 * <p>Only {@code initializeMenu(KeyedMenu) } and {@code deinitializeMenu(KeyedMenu)} methods are recursive.
 *
 * @author Pavel Castornii
 */
public class MenuManager {

    private static final Logger logger = LoggerFactory.getLogger(MenuManager.class);

    private final DefaultShellView shellView;

    private final MenuBar menuBar;

    private final Map<MenuKey, MenuUpdateHelper> updateHelpersByMenuKey = new HashMap<>();

    private final ListChangeListener<MenuItem> menuItemsListener =
            (ListChangeListener.Change<? extends MenuItem> c) -> processMenuListChange(c);

    public MenuManager(DefaultShellView shellView, MenuBar menuBar) {
        this.shellView = shellView;
        this.menuBar = menuBar;
        //listener if menus are added/removed dinamically to/from menu bar(!)
        this.menuBar.getMenus().addListener((ListChangeListener.Change<? extends Menu> c)  -> {
            processMenuListChange(c);
        });
    }

    public void updateMenuBar(MenuAware menuAware) {
        StringBuilder logMessageBuilder = null;
        if (logger.isDebugEnabled()) {
            logMessageBuilder = new StringBuilder();
        }
        for (var m : this.menuBar.getMenus()) {
            if (m instanceof KeyedMenu) {
                var keyedMenu = (KeyedMenu) m;
                var updateHelper = this.updateHelpersByMenuKey.get(keyedMenu.getKey());
                this.configureMenu(keyedMenu, updateHelper, menuAware, logMessageBuilder);
            }
        }
        MenuLogger.logComponentMenuMessage("Main", menuAware, logMessageBuilder);
    }

    private <T extends MenuItem> void processMenuListChange(ListChangeListener.Change<T> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                for (var m : c.getAddedSubList()) {
                    if (m instanceof KeyedMenu) {
                        this.initializeMenu((KeyedMenu) m);
                    }
                }
            } else if (c.wasRemoved()) {
                for (var m : c.getRemoved()) {
                    if (m instanceof KeyedMenu) {
                        this.deinitializeMenu((KeyedMenu) m);
                    }
                }
            }
        }
        //logging new version of menu
        MenuLogger.logMenus(this.menuBar.getMenus(), true);
    }

    /**
     * Recursively initializes all menus.
     * @param menu
     */
    private void initializeMenu(KeyedMenu keyedMenu) {
        final var updateHelper = new MenuUpdateHelper();
        var previous = this.updateHelpersByMenuKey.put(keyedMenu.getKey(), updateHelper);
        if (previous != null) {
            throw new RuntimeException(StringUtils.format("Menu {} has a non-unique key", getMenuText(keyedMenu)));
        }
        //listener if menu/items are added/removed dinamically to/from menu(!)
        keyedMenu.getItems().addListener(menuItemsListener);
        keyedMenu.setOnShowing((e) -> this.doOnMenuShowing(keyedMenu, updateHelper));
        keyedMenu.setOnHiding((e) -> this.doOnMenuHiding(keyedMenu, updateHelper));
        keyedMenu.setOnAction(new MenuActionInterceptor(this.shellView, keyedMenu));
        for (var m : keyedMenu.getItems()) {
            if (m instanceof KeyedMenu) {
                this.initializeMenu((KeyedMenu) m);
            }  else if (m instanceof KeyedMenuItem) {
                var item = (KeyedMenuItem) m;
                item.setOnAction(new MenuItemActionInterceptor(this.shellView, keyedMenu, item));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Menu {} initialized", getMenuText(keyedMenu));
        }
    }

    /**
     * Recursively deinitializes menu and all nested menus.
     * @param keyedMenu
     */
    private void deinitializeMenu(KeyedMenu keyedMenu) {
        var updateHelper = this.updateHelpersByMenuKey.remove(keyedMenu.getKey());
        if (updateHelper == null) {
            throw new RuntimeException(StringUtils.format("Menu {} is not initialized", getMenuText(keyedMenu)));
        }
        keyedMenu.getItems().removeListener(menuItemsListener);
        keyedMenu.setOnShowing(null);
        keyedMenu.setOnHiding(null);
        keyedMenu.setOnAction(((ActionInterceptor) keyedMenu.getOnAction()).getAction());
        for (var m : keyedMenu.getItems()) {
            if (m instanceof KeyedMenu) {
                this.deinitializeMenu((KeyedMenu) m);
            } else if (m instanceof KeyedMenuItem) {
                KeyedMenuItem item = (KeyedMenuItem) m;
                item.setOnAction(((ActionInterceptor) item.getOnAction()).getAction());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Menu {} deinitialized", getMenuText(keyedMenu));
        }
    }

    private void doOnMenuShowing(KeyedMenu menu, MenuUpdateHelper updateHelper) {
        var menuAware = this.shellView.getCurrentMenuAware();
        menuAware.doOnMenuShowing(menu.getKey());
        StringBuilder logMessageBuilder = null;
        if (logger.isEnabledForLevel(MenuLogger.CONFIGURED_MENU_LOG_LEVEL)) {
            logMessageBuilder = new StringBuilder();
        }
        boolean visibleItemsPresent = false;
        SeparatorMenuItem previousVisibleSeparator = null;
        for (var item : menu.getItems()) {
            if (item instanceof KeyedMenu) {
                var keyedMenu = (KeyedMenu) item;
                this.configureMenu(keyedMenu, updateHelper, menuAware, logMessageBuilder);
                if (keyedMenu.isVisible()) {
                    visibleItemsPresent = true;
                }
            } else if (item instanceof KeyedMenuItem)  {
                var keyedItem = (KeyedMenuItem) item;
                this.configureMenuItem(menu, updateHelper, menuAware, keyedItem, logMessageBuilder);
                if (keyedItem.isVisible()) {
                    visibleItemsPresent = true;
                }
            } else if (item instanceof SeparatorMenuItem) {
                var separator = (SeparatorMenuItem) item;
                //there are three variants - first separator, somewhere in the middle, last separator
                if (previousVisibleSeparator == null) {
                    //first separator
                    if (!visibleItemsPresent) {
                        separator.setVisible(false);
                    } else {
                        separator.setVisible(true);
                        previousVisibleSeparator = separator;
                        visibleItemsPresent = false;
                    }
                } else {
                    //in the middle
                    if (!visibleItemsPresent) {
                        previousVisibleSeparator.setVisible(false);
                    }
                    separator.setVisible(true);
                    previousVisibleSeparator = separator;
                    visibleItemsPresent = false;
                }
            }
        }
        //last separator - hide if there weren't visible items after separator
        if (previousVisibleSeparator != null && !visibleItemsPresent) {
            previousVisibleSeparator.setVisible(false);
        }
        MenuLogger.logComponentMenuMessage(menu.getText(), menuAware, logMessageBuilder);
    }

    private void doOnMenuHiding(KeyedMenu menu, MenuUpdateHelper updateHelper) {
        var selectedTab = this.shellView.getSelectedTab();
        if (selectedTab != null) {
            selectedTab.doOnMenuHiding(menu.getKey());
        }
        for (var item : menu.getItems()) {
            if (item instanceof KeyedMenu) {
                unconfigureMenu(menu, updateHelper);
            } else if (item instanceof KeyedMenuItem)  {
                unconfigureMenuItem(menu, (KeyedMenuItem) item);
            }
        }
    }

    private void configureMenu(KeyedMenu keyedMenu, MenuUpdateHelper updateHelper,
            MenuAware menuAware, StringBuilder logMessageBuilder) {
        //supported
        KeyedMenuUpdate update = null;
        var helper = menuAware.getMenuHelper(keyedMenu.getKey());
        if (keyedMenu.isOptional() && (helper == null || !Boolean.TRUE.equals(helper.getMenuIncluded()))) {
            keyedMenu.setVisible(false);
        } else {
            //this method can be called to update top menu; so, unconfigureMenu won't be used
            keyedMenu.setVisible(true);
            //valid
            if (keyedMenu.isValidatable() && (helper == null || !Boolean.TRUE.equals(helper.getMenuValid()))) {
                keyedMenu.setDisable(true);
            }
            //update
            if (keyedMenu.isVisible() && keyedMenu.isUpdatable() && helper != null) {
                var state = new MenuElementState(keyedMenu.isVisible(), keyedMenu.isDisable(),
                        keyedMenu.getText(), keyedMenu.getGraphic());
                update = helper.updateMenu(state);
                if (update != null) {
                    updateHelper.applyUpdate(keyedMenu, state, update);
                }
            }
        }
        MenuLogger.buildComponentMenuMessage(keyedMenu, update, logMessageBuilder);
    }

    private void unconfigureMenu(KeyedMenu keyedMenu, MenuUpdateHelper updateHelper) {
        updateHelper.removeUpdates();
        keyedMenu.setVisible(true);
        keyedMenu.setDisable(false);
    }

    private void configureMenuItem(KeyedMenu keyedMenu, MenuUpdateHelper updateHelper, MenuAware menuAware,
            KeyedMenuItem keyedItem, StringBuilder logMessageBuilder) {
        //supported
        KeyedMenuItemUpdate update = null;
        var helper = menuAware.getMenuItemHelper(keyedItem.getKey());
        if (keyedItem.isOptional() && (helper == null || !Boolean.TRUE.equals(helper.getItemIncluded()))) {
            keyedItem.setVisible(false);
        } else {
            //valid
            if (keyedItem.isValidatable() && (helper == null || !Boolean.TRUE.equals(helper.getItemValid()))) {
                keyedItem.setDisable(true);
            }
            //update
            if (keyedItem.isVisible() && keyedItem.isUpdatable() && helper != null) {
                var state = new MenuElementState(keyedItem.isVisible(), keyedItem.isDisable(),
                        keyedItem.getText(), keyedItem.getGraphic());
                update = helper.updateItem(state);
                if (update != null) {
                    updateHelper.applyUpdate(keyedItem, state, update);
                }
            }
        }
        MenuLogger.buildComponentMenuMessage(keyedItem, update, logMessageBuilder);
    }

    private void unconfigureMenuItem(KeyedMenu keyedMenu, KeyedMenuItem keyedItem) {
        keyedItem.setVisible(true);
        keyedItem.setDisable(false);
    }

    private String getMenuText(KeyedMenu keyedMenu) {
        return keyedMenu.getText().replace("_", "");
    }
}
