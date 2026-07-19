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

package com.techsenger.shellfx.core.menu.manager;

import com.techsenger.shellfx.core.DefaultShellFxView;
import com.techsenger.shellfx.core.MenuAwarePort;
import com.techsenger.shellfx.core.menu.Handler;
import com.techsenger.shellfx.core.menu.MenuHandler;
import com.techsenger.shellfx.core.menu.MenuItemHandler;
import com.techsenger.shellfx.material.menu.ManagedItem;
import com.techsenger.shellfx.material.menu.ManagedMenu;
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
 * are always visible their visibility is configured in two cases: when a focused component changed
 * (including no tab cases) and when updateMenuBar is invoked.
 *
 * <p>Only {@code initializeMenu(ManagedMenu) } and {@code deinitializeMenu(ManagedMenu)} methods are recursive.
 *
 * <p>Menu items are handled uniformly through {@link MenuItem} and {@link ManagedItem}. This class has no
 * knowledge of concrete managed item types ({@code ManagedMenuItem}, {@code ManagedCheckMenuItem},
 * {@code ManagedRadioMenuItem}, etc.) — any item that is both a {@link MenuItem} and a {@link ManagedItem} with a
 * registered {@link MenuItemHandler} is dispatched the same way.
 *
 * @author Pavel Castornii
 */
public class MenuManager {

    private static final Logger logger = LoggerFactory.getLogger(MenuManager.class);

    private final DefaultShellFxView<?> shellView;
    private final MenuBar menuBar;
    private final ListChangeListener<MenuItem> menuItemsListener =
            (ListChangeListener.Change<? extends MenuItem> c) -> processMenuListChange(c);
    private long lastKeyPressedTime;
    private long lastMouseClickTime;

    public MenuManager(DefaultShellFxView<?> shellView, MenuBar menuBar) {
        this.shellView = shellView;
        this.menuBar = menuBar;
        //listener if menus are added/removed dinamically to/from menu bar(!)
        this.menuBar.getMenus().addListener((ListChangeListener.Change<? extends Menu> c)  -> {
            processMenuListChange(c);
        });
    }

    public void updateMenuBar(MenuAwarePort menuAware) {
        for (var m : this.menuBar.getMenus()) {
            if (m instanceof ManagedMenu managedMenu) {
                var handler = MenuHandler.getHandler(managedMenu);
                if (handler != null) {
                    handler.onHiding();
                    handler.onShowing();
                    handler.onUpdate();
                }
            }
        }
    }

    public void setLastKeyPressedTime(long lastKeyPressedTime) {
        this.lastKeyPressedTime = lastKeyPressedTime;
    }

    public void setLastMouseClickTime(long lastMouseClickTime) {
        this.lastMouseClickTime = lastMouseClickTime;
    }

    private <T extends MenuItem> void processMenuListChange(ListChangeListener.Change<T> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                for (var m : c.getAddedSubList()) {
                    if (m instanceof ManagedMenu) {
                        this.initializeMenu((ManagedMenu) m);
                    }
                }
            } else if (c.wasRemoved()) {
                for (var m : c.getRemoved()) {
                    if (m instanceof ManagedMenu) {
                        this.deinitializeMenu((ManagedMenu) m);
                    }
                }
            }
        }
        //logging new version of menu
        MenuLogger.logMenus(this.menuBar.getMenus(), true);
    }

    /**
     * Recursively initializes all menus.
     *
     * @param managedMenu
     */
    private void initializeMenu(ManagedMenu managedMenu) {
        //listener if menu/items are added/removed dinamically to/from menu(!)
        managedMenu.getItems().addListener(menuItemsListener);
        managedMenu.setOnShowing((e) -> this.onMenuShowing(managedMenu));
        managedMenu.setOnHiding((e) -> this.onMenuHiding(managedMenu));
        //managedMenu.setOnAction();
        for (var m : managedMenu.getItems()) {
            if (m instanceof ManagedMenu menu) {
                this.initializeMenu(menu);
            } else if (m instanceof MenuItem item && item instanceof ManagedItem) {
                var managedItem = (MenuItem & ManagedItem) item;
                var handler = MenuItemHandler.getHandler(managedItem);
                if (handler != null) {
                    item.setOnAction(e -> {
                        if (lastMouseClickTime > lastKeyPressedTime) {
                            handler.onAction();
                        } else {
                            handler.onUpdate();
                            if (!item.isDisable() && item.isVisible()) {
                                handler.onAction();
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Event for '{}' accelerator was dispatched", getMenuText(item));
                                }
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Event for '{}' accelerator ignored; disabled: {}, visible: {}",
                                        getMenuText(item), item.isDisable(), item.isVisible());
                                }
                            }
                        }
                    });
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Menu {} initialized", getMenuText(managedMenu));
        }
    }

    /**
     * Recursively deinitializes menu and all nested menus.
     *
     * @param managedMenu
     */
    private void deinitializeMenu(ManagedMenu managedMenu) {
        managedMenu.getItems().removeListener(menuItemsListener);
        managedMenu.setOnShowing(null);
        managedMenu.setOnHiding(null);
        managedMenu.setOnAction(null);
        for (var m : managedMenu.getItems()) {
            if (m instanceof ManagedMenu menu) {
                this.deinitializeMenu(menu);
            } else if (m instanceof MenuItem item) {
                item.setOnAction(null);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Menu {} deinitialized", getMenuText(managedMenu));
        }
    }

    private void onMenuShowing(ManagedMenu menu) {
        MenuAwarePort menuAware = (MenuAwarePort) this.shellView.getComposer().getMenuAware().getPresenter();
        boolean visibleItemsPresent = false;
        SeparatorMenuItem previousVisibleSeparator = null;
        for (var item : menu.getItems()) {
            if (item instanceof ManagedMenu managedMenu) {
                var handler =  MenuHandler.getHandler(managedMenu);
                if (handler != null) {
                    handler.onShowing();
                    handler.onUpdate();
                }
                if (managedMenu.isVisible()) {
                    visibleItemsPresent = true;
                }
            } else if (item instanceof SeparatorMenuItem separator) {
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
            } else if (item instanceof ManagedItem) {
                var managedItem = (MenuItem & ManagedItem) item;
                var handler = MenuItemHandler.getHandler(managedItem);
                if (handler != null) {
                    handler.onShowing();
                    handler.onUpdate();
                }
                if (item.isVisible()) {
                    visibleItemsPresent = true;
                }
            }
        }
        //last separator - hide if there weren't visible items after separator
        if (previousVisibleSeparator != null && !visibleItemsPresent) {
            previousVisibleSeparator.setVisible(false);
        }
    }

    private void onMenuHiding(ManagedMenu menu) {
        var menuAware = (MenuAwarePort) this.shellView.getComposer().getMenuAware().getPresenter();
        for (var item : menu.getItems()) {
            Handler handler = null;
            if (item instanceof ManagedMenu managedMenu) {
                handler = MenuHandler.getHandler(managedMenu);
            } else if (item instanceof MenuItem mi && mi instanceof ManagedItem) {
                handler = MenuItemHandler.getHandler((MenuItem & ManagedItem) mi);
            }
            if (handler != null) {
                handler.onHiding();
            }
        }
    }

    private String getMenuText(MenuItem keyedMenu) {
        return keyedMenu.getText().replace("_", "");
    }
}
